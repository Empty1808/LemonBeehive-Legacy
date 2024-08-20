package nade.lemon.beehive.utils.bukkit;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.head.CustomHead;

public class Materials {

    public static ItemStack fromString(String material) {
        String result = material.toUpperCase().replace(" ", "_");
        String[] array = result.split(":");
        if (array.length == 2 && array[0].equalsIgnoreCase("head")) {
            if (CustomHead.contains(array[1])) return CustomHead.get(array[1]).getItem();
            return new ItemStack(getMaterials(array[1]));
        }
        return new ItemStack(getMaterials(replace(result)));
    }

    public static Set<Material> endWith(String endWith) {
        Set<Material> result = Sets.newHashSet();

        for (Material material : Material.values()) {
            if (material.name().endsWith(endWith.toUpperCase())) {
                result.add(material);
            }
        }

        return result;
    }

    private static String replace(String string) {
        String result = string;
        
        for (UpgradeType type : UpgradeType.values()) {
            result = result.replace("%" + type.getType() + "_ICON%", type.getIcon().name());
        }
        return result;
    }

    private static Material getMaterials(String material) {
        try {
            return Material.valueOf(material);
        }catch (Exception ignore) {
            return Material.STONE;
        }
    }
}
