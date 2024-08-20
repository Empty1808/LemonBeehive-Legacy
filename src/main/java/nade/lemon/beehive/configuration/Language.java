package nade.lemon.beehive.configuration;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.configuration.simple.YamlBuild;
import nade.empty.plugin.EmptyPlugin;

public class Language {
    private final EmptyPlugin plugin;

    private File folder;
    private ConfigBuild config;

    private String language;
    private Map<String, ConfigBuild> configurations = Maps.newLinkedHashMap();

    public Language(EmptyPlugin plugin) {
        this.plugin = plugin;
        this.config = YamlBuild.build(plugin.getDataFolder().getPath() + "/language/config", false);
        this.language = config.get("language", String.class);
    };

    public ConfigBuild get(String key) {
        StringBuild build = new StringBuild(key);
        build.replace("\\", ".");
        build.replace("/", ".");
        return configurations.get(build.toString());
    }

    public Language load() {
        if (this.isAvailiable(language)) {
            loadDirectory(folder);
            return this;
        }
        return this;
    }

    public void reload() {
        for (ConfigBuild config : this.configurations.values()) {
            config.reload();
        }
    }

    public Set<String> keySet() {
        return this.configurations.keySet();
    }

    private void loadFile(File file) {
        if (file.isDirectory()) {
            this.loadDirectory(file);
            return;
        }
        if (file.getName().endsWith(".yml")) {
            StringBuild filePath = new StringBuild(file.getPath());
            StringBuild folderPath = new StringBuild(folder.getPath());

            filePath.replace("\\", "/");
            folderPath.replace("\\", "/");

            filePath.replace(folderPath.toString() + "/", "");
            filePath.replace(".yml", "");
            filePath.replace("/", ".");
            
            configurations.put(filePath.toString(), YamlBuild.build(file.getPath(), false));
        }
    }

    private void loadDirectory(File directory) {
        for (File file : directory.listFiles()) {
            this.loadFile(file);
        }
    }
    
    private boolean isAvailiable(String language) {
        File file = new File(plugin.getDataFolder().getPath() + "/language/" + language);
        if (file.exists() && file.isDirectory()) {
            this.folder = file;
            return true;
        }
        return false;
    }
}

class StringBuild {
    private String string;

    public StringBuild() {
        this("");
    }

    public StringBuild(String string) {
        this.string = string;
    }

    public StringBuild replace(String target, String replacement) {
        string = string.replace(target, replacement);
        return this;
    }

    public String toString() {
        return string;
    }
}