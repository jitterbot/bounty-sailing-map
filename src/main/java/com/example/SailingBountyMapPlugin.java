package com.example;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

@Slf4j
@PluginDescriptor(
		name = "Bounty Sailing Map",
		description = "Shows Sailing bounty creature locations on the world map"
)
public class SailingBountyMapPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "sailingBountyMap";
	private static final String ACTIVE_BOUNTIES_KEY = "activeBounties";

	@Inject
	private Client client;

	@Inject
	private SailingBountyMapConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	/*
	 * Every displayed marker for each bounty creature.
	 */
	private final Map<BountyLocation, List<WorldMapPoint>> markers =
			new HashMap<>();

	/*
	 * Creatures currently detected in Captain's Log -> Port Tasks,
	 * or restored from the last saved task list.
	 */
	private final Set<String> activeCreatures =
			new HashSet<>();

	private String lastTaskSignature = "";

	@Override
	protected void startUp()
	{
		activeCreatures.clear();
		lastTaskSignature = "";

		loadSavedBounties();
		updateMarkers();

		log.info(
				"Sailing Bounty Map started with saved creatures: {}",
				activeCreatures
		);
	}

	@Override
	protected void shutDown()
	{
		removeAllMarkers();

		/*
		 * Clear only the in-memory state.
		 * The saved bounty list is intentionally kept in ConfigManager
		 * so it survives plugin toggles and RuneLite restarts.
		 */
		activeCreatures.clear();
		lastTaskSignature = "";

		log.info("Sailing Bounty Map stopped");
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		scanPortTasks();
	}

	/*
	 * Immediately recreate active markers when the user changes
	 * one of this plugin's colour settings.
	 */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		/*
		 * Saving the bounty list also creates a ConfigChanged event.
		 * There is no need to rebuild markers for that hidden value,
		 * because scanPortTasks() will update them immediately after saving.
		 */
		if (ACTIVE_BOUNTIES_KEY.equals(event.getKey()))
		{
			return;
		}

		refreshActiveMarkers();
	}

	private void scanPortTasks()
	{
		Widget[] roots = client.getWidgetRoots();

		if (roots == null)
		{
			return;
		}

		PortTaskScan scan = new PortTaskScan();

		for (Widget root : roots)
		{
			scanWidget(root, scan);
		}

		/*
		 * Only update bounty state while Port Tasks is genuinely open.
		 */
		if (!scan.portTasksOpen)
		{
			return;
		}

		String signature =
				String.join(
						"|",
						new java.util.TreeSet<>(scan.creatures)
				);

		if (signature.equals(lastTaskSignature))
		{
			return;
		}

		lastTaskSignature = signature;

		activeCreatures.clear();
		activeCreatures.addAll(scan.creatures);

		/*
		 * Persist the newest task list so it can be restored next time
		 * RuneLite or the plugin starts.
		 */
		saveActiveBounties();

		updateMarkers();

		log.info(
				"Active bounty creatures: {}",
				activeCreatures
		);
	}

	private void scanWidget(
			Widget widget,
			PortTaskScan scan)
	{
		if (widget == null)
		{
			return;
		}

		String text = widget.getText();

		if (text != null &&
				text.contains("Captain's Log - Port Tasks"))
		{
			scan.portTasksOpen = true;
		}

		String name = widget.getName();

		if (name != null)
		{
			String lower =
					stripTags(name)
							.toLowerCase();

			for (BountyLocation bounty :
					BountyLocation.values())
			{
				String creature =
						bounty.getCreature()
								.toLowerCase();

				if (lower.contains(
						creature + " bounty"))
				{
					scan.creatures.add(
							bounty.getCreature()
					);
				}
			}
		}

		scanChildren(
				widget.getDynamicChildren(),
				scan
		);

		scanChildren(
				widget.getStaticChildren(),
				scan
		);

		scanChildren(
				widget.getNestedChildren(),
				scan
		);
	}

	private void scanChildren(
			Widget[] children,
			PortTaskScan scan)
	{
		if (children == null)
		{
			return;
		}

		for (Widget child : children)
		{
			scanWidget(
					child,
					scan
			);
		}
	}

	/*
	 * Add or remove complete creature spawn sets depending
	 * on which bounty creatures are active.
	 */
	private void updateMarkers()
	{
		for (BountyLocation bounty :
				BountyLocation.values())
		{
			boolean shouldShow =
					config.showAllBounties()
							|| activeCreatures.stream()
							.anyMatch(creature ->
									creature.equalsIgnoreCase(
											bounty.getCreature()
									)
							);

			if (shouldShow)
			{
				addMarkers(bounty);
			}
			else
			{
				removeMarkers(bounty);
			}
		}
	}

	/*
	 * Rebuild all currently active markers.
	 *
	 * This is used when a colour is changed in the RuneLite
	 * configuration panel so the change is visible immediately.
	 */
	private void refreshActiveMarkers()
	{
		removeAllMarkers();
		updateMarkers();
	}

	/*
	 * Add every known spawn tile for one creature.
	 */
	private void addMarkers(
			BountyLocation bounty)
	{
		if (markers.containsKey(bounty))
		{
			return;
		}

		List<WorldMapPoint> creatureMarkers =
				new ArrayList<>();

		/*
		 * Reuse one icon image for every spawn of this creature.
		 */
		BufferedImage icon =
				createIcon(
						bounty.getMarkerLetter(),
						getMarkerColor(bounty)
				);

		for (WorldPoint worldPoint :
				bounty.getWorldPoints())
		{
			WorldMapPoint marker =
					new WorldMapPoint(
							worldPoint,
							icon
					);

			marker.setTooltip(
					bounty.getCreature()
							+ " Bounty"
			);

			worldMapPointManager.add(marker);

			creatureMarkers.add(marker);
		}

		markers.put(
				bounty,
				creatureMarkers
		);

		log.info(
				"Added {} {} bounty markers",
				creatureMarkers.size(),
				bounty.getCreature()
		);
	}

	/*
	 * Remove every map marker for one creature.
	 */
	private void removeMarkers(
			BountyLocation bounty)
	{
		List<WorldMapPoint> creatureMarkers =
				markers.remove(bounty);

		if (creatureMarkers == null)
		{
			return;
		}

		for (WorldMapPoint marker :
				creatureMarkers)
		{
			worldMapPointManager.remove(marker);
		}

		log.info(
				"Removed {} {} bounty markers",
				creatureMarkers.size(),
				bounty.getCreature()
		);
	}

	private void removeAllMarkers()
	{
		for (List<WorldMapPoint> creatureMarkers :
				markers.values())
		{
			for (WorldMapPoint marker :
					creatureMarkers)
			{
				worldMapPointManager.remove(marker);
			}
		}

		markers.clear();
	}

	/*
	 * Save the currently active bounty creatures into RuneLite config.
	 */
	private void saveActiveBounties()
	{
		String value =
				String.join(
						",",
						new java.util.TreeSet<>(activeCreatures)
				);

		configManager.setConfiguration(
				CONFIG_GROUP,
				ACTIVE_BOUNTIES_KEY,
				value
		);

		log.info(
				"Saved active bounty creatures: {}",
				activeCreatures
		);
	}

	/*
	 * Restore the last known bounty creatures when the plugin starts.
	 */
	private void loadSavedBounties()
	{
		String value =
				configManager.getConfiguration(
						CONFIG_GROUP,
						ACTIVE_BOUNTIES_KEY
				);

		if (value == null || value.trim().isEmpty())
		{
			return;
		}

		String[] creatures = value.split(",");

		for (String creature : creatures)
		{
			String trimmed = creature.trim();

			if (!trimmed.isEmpty())
			{
				/*
				 * Only restore creature names that still exist in
				 * BountyLocation. This prevents stale/bad saved values
				 * from creating phantom state later.
				 */
				for (BountyLocation bounty : BountyLocation.values())
				{
					if (bounty.getCreature().equalsIgnoreCase(trimmed))
					{
						activeCreatures.add(bounty.getCreature());
						break;
					}
				}
			}
		}

		lastTaskSignature =
				String.join(
						"|",
						new java.util.TreeSet<>(activeCreatures)
				);

		log.info(
				"Loaded saved active bounty creatures: {}",
				activeCreatures
		);
	}

	/*
	 * Return the user-selected colour for a creature.
	 */
	private Color getMarkerColor(
			BountyLocation bounty)
	{
		switch (bounty)
		{
			case TIGER_SHARK:
				return config.tigerSharkColor();

			case STINGRAY:
				return config.stingrayColor();

			case ALBATROSS:
				return config.albatrossColor();

			case FRIGATEBIRD:
				return config.frigatebirdColor();

			case EAGLE_RAY:
				return config.eagleRayColor();

			case SPINED_KRAKEN:
				return config.spinedKrakenColor();

			case GREAT_WHITE_SHARK:
				return config.greatWhiteSharkColor();

			case ARMOURED_KRAKEN:
				return config.armouredKrakenColor();

			case BULL_SHARK:
				return config.bullSharkColor();

			case BUTTERFLY_RAY:
				return config.butterflyRayColor();

			case HAMMERHEAD_SHARK:
				return config.hammerheadSharkColor();

			case MANTA_RAY:
				return config.mantaRayColor();

			case MOGRE:
				return config.mogreColor();

			case NARWHAL:
				return config.narwhalColor();

			case ORCA:
				return config.orcaColor();

			case OSPREY:
				return config.ospreyColor();

			case PYGMY_KRAKEN:
				return config.pygmyKrakenColor();

			case TERN:
				return config.ternColor();

			case VAMPYRE_KRAKEN:
				return config.vampyreKrakenColor();

			case VEILED_KRAKEN:
				return config.veiledKrakenColor();

			default:
				return Color.RED;
		}
	}

	private String stripTags(String text)
	{
		if (text == null)
		{
			return "";
		}

		return text.replaceAll(
				"<[^>]*>",
				""
		);
	}

	/*
	 * Build the coloured circular map icon.
	 */
	private BufferedImage createIcon(
			String letter,
			Color color)
	{
		BufferedImage image =
				new BufferedImage(
						20,
						20,
						BufferedImage.TYPE_INT_ARGB
				);

		Graphics2D graphics =
				image.createGraphics();

		graphics.setColor(color);

		graphics.fillOval(
				0,
				0,
				19,
				19
		);

		graphics.setColor(Color.WHITE);

		int fontSize;

		if (letter.length() >= 3)
		{
			fontSize = 10;
		}
		else if (letter.length() == 2)
		{
			fontSize = 11;
		}
		else
		{
			fontSize = 12;
		}

		graphics.setFont(
				graphics.getFont().deriveFont(
						java.awt.Font.BOLD,
						(float) fontSize
				)
		);

		java.awt.FontMetrics metrics =
				graphics.getFontMetrics();

		int textWidth = metrics.stringWidth(letter);

		int x =
				(image.getWidth() - textWidth) / 2;

		int y =
				((image.getHeight() - metrics.getHeight()) / 2)
						+ metrics.getAscent();

		graphics.drawString(
				letter,
				x,
				y
		);

		graphics.dispose();

		return image;
	}

	@Provides
	SailingBountyMapConfig provideConfig(
			ConfigManager configManager)
	{
		return configManager.getConfig(
				SailingBountyMapConfig.class
		);
	}

	private static class PortTaskScan
	{
		private boolean portTasksOpen =
				false;

		private final Set<String> creatures =
				new HashSet<>();
	}
}