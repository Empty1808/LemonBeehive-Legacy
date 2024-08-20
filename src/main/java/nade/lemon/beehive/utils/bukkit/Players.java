package nade.lemon.beehive.utils.bukkit;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.collect.Sets;

import nade.lemon.beehive.LemonBeehive;

public class Players {
    
    private static Set<NamespacedKey> keys = Sets.newHashSet();

    public static void discoverRecipe(NamespacedKey key) {
        keys.add(key);

        Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach((player) -> {
                player.discoverRecipe(key);
            });
        }, 0);
    }

    public static void undiscoverRecipe(NamespacedKey key) {
        keys.remove(key);

        Bukkit.getOnlinePlayers().forEach((player) -> {
            player.undiscoverRecipe(key);
        });
    }


    public static void onPlayerJoin(PlayerJoinEvent e) {
        keys.forEach((key) -> {
            if (!e.getPlayer().hasDiscoveredRecipe(key)) {
                e.getPlayer().discoverRecipes(keys);
            }
        });
    }
}
