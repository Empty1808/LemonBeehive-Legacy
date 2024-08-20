package nade.lemon.beehive.utils.bukkit.item;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import nade.empty.configuration.serialization.ConfigurationSerializable;

public class EItemStack implements ConfigurationSerializable{

    private ItemStack item;

    private EItemStack(ItemStack item) {
        this.item = item;
    }

    @Override
    public Map<String, Object> serialize() {
        return item.serialize();
    }

}
