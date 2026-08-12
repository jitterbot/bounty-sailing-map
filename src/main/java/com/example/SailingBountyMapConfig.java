package com.example;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("sailingBountyMap")
public interface SailingBountyMapConfig extends Config
{
	@ConfigItem(
			keyName = "instructions",
			name = "How to use",
			description = "Instructions for loading bounty creature locations",
			position = 0
	)
	default String instructions()
	{
		return "Right-click your Captain's Log and select View-tasks to update your active bounty creature locations on the world map.";
	}

	@ConfigItem(
			keyName = "showAllBounties",
			name = "Show all creatures",
			description = "Show all known bounty creature locations regardless of your active tasks",
			position = 1
	)
	default boolean showAllBounties()
	{
		return false;
	}

	@ConfigSection(
			name = "Marker colours",
			description = "Choose the colour used for each bounty creature on the world map.",
			position = 2
	)
	String markerColoursSection = "markerColoursSection";
	@ConfigItem(
			keyName = "tigerSharkColor",
			name = "Tiger shark",
			description = "Colour used for Tiger shark bounty markers",
			position = 1,
			section = markerColoursSection
	)
	default Color tigerSharkColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "stingrayColor",
			name = "Stingray",
			description = "Colour used for Stingray bounty markers",
			position = 2,
			section = markerColoursSection
	)
	default Color stingrayColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "albatrossColor",
			name = "Albatross",
			description = "Colour used for Albatross bounty markers",
			position = 3,
			section = markerColoursSection
	)
	default Color albatrossColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "frigatebirdColor",
			name = "Frigatebird",
			description = "Colour used for Frigatebird bounty markers",
			position = 4,
			section = markerColoursSection
	)
	default Color frigatebirdColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "eagleRayColor",
			name = "Eagle ray",
			description = "Colour used for Eagle ray bounty markers",
			position = 5,
			section = markerColoursSection
	)
	default Color eagleRayColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "spinedKrakenColor",
			name = "Spined kraken",
			description = "Colour used for Spined kraken bounty markers",
			position = 6,
			section = markerColoursSection
	)
	default Color spinedKrakenColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "greatWhiteSharkColor",
			name = "Great white shark",
			description = "Colour used for Great white shark bounty markers",
			position = 7,
			section = markerColoursSection
	)
	default Color greatWhiteSharkColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "armouredKrakenColor",
			name = "Armoured kraken",
			description = "Colour used for Armoured kraken bounty markers",
			position = 8,
			section = markerColoursSection
	)
	default Color armouredKrakenColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "bullSharkColor",
			name = "Bull shark",
			description = "Colour used for Bull shark bounty markers",
			position = 9,
			section = markerColoursSection
	)
	default Color bullSharkColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "butterflyRayColor",
			name = "Butterfly ray",
			description = "Colour used for Butterfly ray bounty markers",
			position = 10,
			section = markerColoursSection
	)
	default Color butterflyRayColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "hammerheadSharkColor",
			name = "Hammerhead shark",
			description = "Colour used for Hammerhead shark bounty markers",
			position = 11,
			section = markerColoursSection
	)
	default Color hammerheadSharkColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "mantaRayColor",
			name = "Manta ray",
			description = "Colour used for Manta ray bounty markers",
			position = 12,
			section = markerColoursSection
	)
	default Color mantaRayColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "mogreColor",
			name = "Mogre",
			description = "Colour used for Mogre bounty markers",
			position = 13,
			section = markerColoursSection
	)
	default Color mogreColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "narwhalColor",
			name = "Narwhal",
			description = "Colour used for Narwhal bounty markers",
			position = 14,
			section = markerColoursSection
	)
	default Color narwhalColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "orcaColor",
			name = "Orca",
			description = "Colour used for Orca bounty markers",
			position = 15,
			section = markerColoursSection
	)
	default Color orcaColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "ospreyColor",
			name = "Osprey",
			description = "Colour used for Osprey bounty markers",
			position = 16,
			section = markerColoursSection
	)
	default Color ospreyColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "pygmyKrakenColor",
			name = "Pygmy kraken",
			description = "Colour used for Pygmy kraken bounty markers",
			position = 17,
			section = markerColoursSection
	)
	default Color pygmyKrakenColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "ternColor",
			name = "Tern",
			description = "Colour used for Tern bounty markers",
			position = 18,
			section = markerColoursSection
	)
	default Color ternColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "vampyreKrakenColor",
			name = "Vampyre kraken",
			description = "Colour used for Vampyre kraken bounty markers",
			position = 19,
			section = markerColoursSection
	)
	default Color vampyreKrakenColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "veiledKrakenColor",
			name = "Veiled kraken",
			description = "Colour used for Veiled kraken bounty markers",
			position = 20,
			section = markerColoursSection
	)
	default Color veiledKrakenColor()
	{
		return Color.RED;
	}
}