package nade.lemon.beehive.configuration;

import java.util.Collection;
import java.util.List;

import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.beehive.configuration.build.BeehiveSectionBuild;

public class YamlChecker {
    
    public static void upgrades() {
        Collection<BeehiveConfigBuild> builds = BeehiveYamlConfig.getSafes("upgrade.");
		for (BeehiveConfigBuild build : builds) {
			if (build.getKeys(false).size() <= 0) continue;
			for (String key : build.getKeys(false)) {
				BeehiveSectionBuild section = build.getSection(key);
				if (!section.contains("amounts")) continue;
				List<String> methods = build.getList(key + ".methods", String.class);
				List<Integer> amounts = build.getList(key + ".amounts", Integer.class);
				if (methods.get(0).contains(":")) continue;
				methods.set(0, methods.get(0) + ":" + amounts.get(0));
				build.set(key + ".methods", methods);
				build.set(key + ".amounts", null);
			}
			build.save();
			build.reload();
		}
    }

}
