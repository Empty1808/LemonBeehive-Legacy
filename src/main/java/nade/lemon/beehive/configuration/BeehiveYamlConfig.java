package nade.lemon.beehive.configuration;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;

public class BeehiveYamlConfig {
    
    private static BeehiveConfigBuild YAML_CONFIG;

    private static Map<String, BeehiveConfigBuild> mapper = Maps.newHashMap();

    private static void register(String key, BeehiveConfigBuild object) {
        if (key.isEmpty() || Objects.isNull(object)) return;
        mapper.put(key.toLowerCase(), object);
    }

    public static void onEnable() {
        setupYamlConfig();
        
        for (String key : YAML_CONFIG.getKeys(true)) {
            if (!YAML_CONFIG.contains(key, ConfigurationSection.class)) {
                Object object = YAML_CONFIG.get(key, String.class);
                if (Objects.isNull(object)) System.out.println(key);
                BeehiveConfigBuild build = BeehiveConfigBuild.build(YAML_CONFIG.get(key, String.class));
                if (key.contains("language")) build.copyDefault();
                register(key, build);
            }
        }

        getConfig().copyDefault();
    }

    

    private static BeehiveConfigBuild get(String key) {
        return mapper.get(key.replace(".yml", "").toLowerCase());
    }

    public static BeehiveConfigBuild getSafe(String key) {
        BeehiveConfigBuild config = BeehiveYamlConfig.get(key);
        config.reload();
        return config;
    }

    public static Collection<BeehiveConfigBuild> getSafes(String startstWith) {
        Collection<BeehiveConfigBuild> results = Sets.newHashSet();
        for (String key : mapper.keySet()) {
            if (key.toLowerCase().startsWith(startstWith.toLowerCase(), 0)) {
                results.add(getSafe(key));
            }
        }
        return results;
    }

    public static BeehiveConfigBuild getYamlConfig() {
        return YAML_CONFIG;
    }

    public static BeehiveConfigBuild getConfig() {
        return getSafe("default.config");
    }

    public static BeehiveConfigBuild getRandomName() {
        return getSafe("default.random-name");
    }

    public static BeehiveConfigBuild getCustomRecipe() {
        return getSafe("data.custom-recipe");
    }

    public static BeehiveConfigBuild getServerDatabase() {
        return getSafe("data.server-database");
    }

    public static BeehiveConfigBuild getHarvest() {
        return getSafe("data.harvest");
    }

    private static void setupYamlConfig() {
        YAML_CONFIG = BeehiveConfigBuild.build("yaml-config.yml", false);
    }

    public static Set<BeehiveConfigBuild> getAvailable() {
        return Sets.newHashSet(mapper.values());
    }
}
