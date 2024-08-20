package nade.lemon.beehive.data;

import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;

public class NekoConfiguration {
	
	private String path;
	private BeehiveConfigBuild build;
	
	public NekoConfiguration(String path) {
		this.path = path;
		this.build = BeehiveYamlConfig.getSafe(path);
	}
	
	public BeehiveConfigBuild getConfig() {
		return build;
	}
	
	public String getPath() {
		return path;
	}
	
	public void update() {
		this.build = BeehiveYamlConfig.getSafe(path);
	}
}
