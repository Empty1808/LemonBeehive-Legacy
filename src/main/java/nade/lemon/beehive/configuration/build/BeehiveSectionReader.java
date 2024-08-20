package nade.lemon.beehive.configuration.build;

import org.bukkit.configuration.Configuration;

public class BeehiveSectionReader {
    
    private Configuration configuration;

    public BeehiveSectionReader(Configuration configuration) {
        this.configuration = configuration;
    }

    public BeehiveSectionReader readKey(String key) {
        
        return this;
    }
    
    public boolean hasKey(String key) {
        return configuration.getKeys(false).contains(key);
    }
}
