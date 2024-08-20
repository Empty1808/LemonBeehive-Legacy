package nade.lemon.beehive.configuration.build;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Objects;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.configuration.ConfigurationL;
import nade.lemon.configuration.SimpleConfig;

/**
 * 
 */
public class BeehiveConfigBuild extends BeehiveSectionBuild implements ConfigurationBuild{

    private EmptyPlugin plugin;
    private SimpleConfig simple;

    private String path;

    private BeehiveConfigBuild(String path, boolean creation) {
        this.plugin = LemonBeehive.getInstance();
        this.simple = plugin.get(SimpleConfig.class);
        if (!Objects.isNull(path) && !path.endsWith(".yml")) {
            path += ".yml";
        }
        this.path = path;
        if (!this.simple.hasConfig(path)) {
            this.simple.create(path, creation);
        }
        this.configuration = simple.getConfig(path);
        
    }

    public static BeehiveConfigBuild build(String path) {
        return BeehiveConfigBuild.build(path, true);
    }

    public static BeehiveConfigBuild build(String path, boolean creation) {
        if (path == null || path.isEmpty()) return null;
        return new BeehiveConfigBuild(path, creation);
    }

    public void reload() {
        this.configuration = ConfigurationL.loadConfiguration(((ConfigurationL) this.configuration).getFile());
    }

    public void save() {
        this.save(false);
    }

    public File getBaseFile() {
        return ((ConfigurationL) this.configuration).getFile();
    }

    public void save(boolean copyDefault) {
        this.simple.saveConfig((ConfigurationL) this.configuration);
    }

    @Override
    public String toString() {
        return configuration.toString();
    }

    public ConfigurationL getDefault() {
        return ConfigurationL.loadConfiguration(new InputStreamReader(plugin.getResource(path.replace("\\", "/"))));
    }

    public void copyDefault() {
        ConfigurationL config = (ConfigurationL) this.configuration;
        config.setDefaults(this.getDefault());
        config.options().copyDefaults(true);
        this.save();
    }
}