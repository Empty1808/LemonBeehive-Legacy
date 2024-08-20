package nade.lemon.beehive.handlers;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.entity.Item;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.FeaturesEnable;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.utils.string.Color;

public class ItemDropHandler {
    private static final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");

    private static Set<Item> drops = Sets.newHashSet();
    
    public static void addDrop(Item item, String owner) {
        drops.add(item);
        String text = language.get("hologram.drop", String.class);
        if (!Objects.isNull(owner) && !owner.equalsIgnoreCase("nobody")) {
            text = text.replace("{beehive-owner}", owner)
                       .replace("{beehive-displayname}", item.getItemStack().getItemMeta().getDisplayName());
        }
        if (owner.equalsIgnoreCase("nobody")) {
            text = text.replace("{beehive-owner}", Color.hex("&c" + language.getOrDefault("parameter.nobody", "nobody",String.class)))
                       .replace("{beehive-displayname}", item.getItemStack().getItemMeta().getDisplayName());
        }
        item.setCustomName(Color.hex(text));
        item.setCustomNameVisible(FeaturesEnable.DROP_HOLOGRAM.isEnable());
    }

    public static void removeDrop(Item item) {
        drops.remove(item);
    }

    public static void reloadHologram() {
        List<Item> invalid = Lists.newArrayList();
        for (Item item : drops) {
            if (!item.isValid()) {
                invalid.add(item);
                continue;
            }
            
            item.setCustomNameVisible(FeaturesEnable.DROP_HOLOGRAM.isEnable());
        }
        drops.removeAll(invalid);
    }
}