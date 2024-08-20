package nade.lemon.beehive.features.harvest;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;

import com.google.common.collect.Maps;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.utils.bukkit.ItemStacks;

public class HarvestSystem {
    private Map<UUID, Harvest> harvest = Maps.newLinkedHashMap();
    private ConfigurationBuild config = BeehiveYamlConfig.getHarvest();

    private HarvestEditor editor;
    private HarvestEvent event;

    public HarvestSystem(EmptyPlugin plugin) {
        for (String key : config.getKeys()) {
            Harvest harvest = Harvest.fromSection(config.getSection(key));
            this.harvest.put(harvest.getUniqueId(), harvest);
        }

        this.editor = new HarvestEditor(plugin, this);
        this.event = new HarvestEvent(plugin, this);
    }

    public Harvest register() {
        Harvest harvest = new Harvest(ItemStacks.newItemStack(Material.BARRIER));
        this.harvest.put(harvest.getUniqueId(), harvest);
        config.set(harvest.getUniqueId().toString() + ".uuid", harvest.getUniqueId().toString());
        config.set(harvest.getUniqueId().toString() + ".tool", harvest.getTool());
        config.set(harvest.getUniqueId().toString() + ".consumable", false);
        config.save();
        return harvest;
    }

    public void unregister(UUID uniqueId) {
        this.harvest.remove(uniqueId);
        config.set(uniqueId.toString(), null);
        config.save();
    }

    public Harvest get(UUID uuid) {
        return this.harvest.get(uuid);
    }

    public Collection<Harvest> values() {
        return this.harvest.values();
    }

    public HarvestEditor getEditor() {
        return editor;
    }

    public HarvestEvent getEvent() {
        return event;
    }
}
