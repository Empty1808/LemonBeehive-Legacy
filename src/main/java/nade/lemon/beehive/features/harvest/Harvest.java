package nade.lemon.beehive.features.harvest;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.configuration.simple.ConfigurationSectionBuild;
import nade.lemon.gacha.DropObjects;
import nade.lemon.utils.bukkit.ItemStacks;

public class Harvest {
    private ItemStack tool;
    private DropObjects drops;
    private UUID uuid;
    private boolean consumable;

    private Harvest() {}

    public Harvest(ItemStack tool) {
        this.tool = tool;
        this.drops = new DropObjects();
        this.uuid = UUID.randomUUID();
    }

    public void setTool(ItemStack tool) {
        this.tool = tool;
    }

    public ItemStack getTool() {  
        return ItemStacks.newItemStack(tool);
    }

    public DropObjects getDrops() {
        return drops;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public void reload(ConfigurationBuild config) {
        ConfigurationSectionBuild section = config.getSection(uuid.toString());

        this.tool = section.get("tool", ItemStack.class);
        drops.clear();

        ConfigurationSectionBuild reward = section.getSection("reward");
        if (!Objects.isNull(reward)) {
            for (String key : reward.getKeys()) {
                ConfigurationSectionBuild element = reward.getSection(key);
                drops.addElement(element.getOrDefault("rate", 1d, Double.class), element.get("item", ItemStack.class));
            }
        }
    }

    public static Harvest fromSection(ConfigurationSectionBuild section) {
        Harvest harvest = new Harvest();

        harvest.tool = section.get("tool", ItemStack.class);
        harvest.uuid = UUID.fromString(section.get("uuid", String.class));
        harvest.drops = new DropObjects();

        ConfigurationSectionBuild drops = section.getSection("drops");
        if (!Objects.isNull(drops)) {
            for (String key : drops.getKeys()) {
                ConfigurationSectionBuild element = drops.getSection(key);
                harvest.drops.addElement(element.getOrDefault("rate", 1d, Double.class), element.get("item", ItemStack.class));
            }
        }

        harvest.consumable = section.get("consumable", Boolean.class);

        return harvest;
    }
}