package nade.lemon.beehive;

import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;

public class FeaturesEnable {
	public static FeaturesEnable MYSQL;
	public static FeaturesEnable RECIPE;
	public static FeaturesEnable DISPLAY_HOLOGRAM;
	public static FeaturesEnable DROP_HOLOGRAM;
	public static FeaturesEnable DISPENSERS;
	public static FeaturesEnable WORLD_BLACKLIST;
	public static FeaturesEnable TITLE;
	public static FeaturesEnable MESSAGE;
	public static FeaturesEnable BEEHIVE_LIMIT;
	public static FeaturesEnable BEE_STATUS;
	
	public static void onEnable() {
		MYSQL = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.mysql.enable");
		RECIPE = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.beehive-recipe.enable");
		DISPLAY_HOLOGRAM = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.beehive.display-hologram.enable");
		DROP_HOLOGRAM = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.beehive.drop-hologram.enable");
		DISPENSERS = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.beehive.dispensers");
		WORLD_BLACKLIST = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.world-blacklist.enable");
		TITLE = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.notification.title.enable");
		MESSAGE = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.notification.message.enable");
		BEEHIVE_LIMIT = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.beehive.limit.enable");
		BEE_STATUS = new FeaturesEnable(BeehiveYamlConfig.getConfig(), "general.entities.bees-status.enable");
	}

	private BeehiveConfigBuild build;
	private String path;
	
	private FeaturesEnable(BeehiveConfigBuild build, String path) {
		this.build = build;
		this.path = path;
	}
	
	public void setEnable(boolean enable) {
		build.set(path, enable);
		build.save();
	}

	public boolean isEnable() {
		return build.getOrDefault(path, false, Boolean.class);
	}
}