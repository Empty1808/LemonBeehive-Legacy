package nade.lemon.beehive.upgrades;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.configuration.simple.ConfigurationSectionBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;

public class UpgradeSystem {

    private Map<UpgradeType, TreeSet<Upgrade>> upgrades = Maps.newLinkedHashMap();

    public UpgradeSystem(EmptyPlugin plugin) {
        this.reload();
    }

    public void reload() {
        upgrades.clear();

        ConfigurationSectionBuild section = BeehiveYamlConfig.getYamlConfig().getSection("upgrades");

        section.getKeys(true).forEach((path) -> {
            if (section.contains(path, String.class)) {
                ConfigurationBuild config = BeehiveYamlConfig.getSafe("upgrades." + path);

                TreeSet<Upgrade> upgrades = Sets.newTreeSet();
                
                UpgradeType type = UpgradeType.create(config.get("type", String.class), config.get("category", String.class), config.get("icon", String.class));
                type.set("config", config);

                if (config.isSection("upgrades")) {
                    for (String key : config.getSection("upgrades").getKeys()) {
                        Upgrade upgrade = Upgrade.applyBySection(config.getSection("upgrades." + key), type);
                        upgrades.add(upgrade);
                    }
                }

                this.upgrades.put(type, upgrades);
            }
        });
    }

    public Upgrade add(UpgradeType type) {
        Upgrade _last = this.last(type);
        Upgrade _new = new Upgrade(!Objects.isNull(_last) ? _last.getLevel()+1 : 1, type);

        _new.update();
        
        this.gets(type).add(_new);
        type.getConfig().save();

        return _new;
    }

    public void remove(UpgradeType type) {
        Upgrade _last = this.last(type);
        
        _last.remove();

        this.gets(type).remove(_last);
        type.getConfig().save();
    }
    
    public Set<UpgradeType> keySet() {
        return upgrades.keySet();
    }

    private TreeSet<Upgrade> gets(UpgradeType type) {
        if (!upgrades.containsKey(type)) {
            upgrades.put(type, Sets.newTreeSet());
        }
        return upgrades.get(type);
    }

    public void forEach(UpgradeType type, Consumer<? super Upgrade> action) {
        this.gets(type).forEach(action);
    }

    public Upgrade getByLevel(UpgradeType type, int level) {
        TreeSet<Upgrade> upgrades = this.gets(type);
        if (upgrades.size() < level) return upgrades.last();
        if (level < 1) return upgrades.first();
        return (Upgrade) upgrades.toArray()[level-1];
    }

    public int size(UpgradeType type) {
        return this.gets(type).size();
    }

    public Upgrade first(UpgradeType type) {
        if (this.size(type) <= 0) return null;
        return this.gets(type).first();
    }

    public Upgrade last(UpgradeType type) {
        if (this.size(type) <= 0) return null;
        return this.gets(type).last();
    }

    public boolean isFirst(Upgrade upgrade) {
        if (Objects.isNull(upgrade)) return false;
        return this.first(upgrade.getType()).equals(upgrade);
    }

    public boolean isLast(Upgrade upgrade) {
        if (Objects.isNull(upgrade)) return false;
        return this.last(upgrade.getType()).equals(upgrade);
    }

    public Upgrade higher(Upgrade upgrade) {
        if (Objects.isNull(upgrade)) return null;
        return this.gets(upgrade.getType()).higher(upgrade);
    }

    public Upgrade lower(Upgrade upgrade) {
        if (Objects.isNull(upgrade)) return null;
        return this.gets(upgrade.getType()).lower(upgrade);
    }

    public boolean hasHigher(Upgrade upgrade) {
        return !Objects.isNull(this.higher(upgrade));
    }

    public boolean hasLower(Upgrade upgrade) {
        return !Objects.isNull(this.lower(upgrade));
    }
}