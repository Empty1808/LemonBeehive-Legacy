package nade.lemon.beehive;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;

public class StaticField {
	public static String MESSAGE_PREFIX;
	public static Integer HONEY_MULTIPLIER;
	public static Integer LINKED_DISTANCE;
	public static Boolean MATERIAL_REPLACE;

	private static ConfigBuild language;
	
	public static void onEnable(EmptyPlugin plugin) {
		System.out.println(plugin.get(Language.class).get("language"));
		language = plugin.get(Language.class).get("language");
		MESSAGE_PREFIX = language.get("prefix", String.class);
		HONEY_MULTIPLIER = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive.honey-multiplier", Integer.class);
		LINKED_DISTANCE = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.storage-linked.distance", Integer.class);
		MATERIAL_REPLACE = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive-recipe.material-replace", Boolean.class);
	}

	public static void reload() {
		MESSAGE_PREFIX = language.get("prefix", String.class);
		HONEY_MULTIPLIER = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive.honey-multiplier", Integer.class);
		LINKED_DISTANCE = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.storage-linked.distance", Integer.class);
		MATERIAL_REPLACE = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive-recipe.material-replace", Boolean.class);
	}
}
