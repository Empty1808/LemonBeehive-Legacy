package nade.lemon.beehive.data;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Maps;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.features.developers.DeveloperMode;
import nade.lemon.head.CustomHead;
import nade.lemon.head.HeadObject;

public class HeadDatabase {
    
    private static Map<UUID, HeadObject> database = Maps.newHashMap();
    private static Map<UUID, BukkitTask> unregister = Maps.newHashMap();

    public static void onEnable() {
        database.clear();
        unregister.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(LemonBeehive.getInstance(), () -> {
                database.put(player.getUniqueId(), CustomHead.getByPlayer(player));
                DeveloperMode.notify("&eregister &6" + player.getName() + " &ehead!");
            });
        }
    }

    public static void onPlayerJoin(Player player) {
        if (unregister.containsKey(player.getUniqueId())) {
            unregister.get(player.getUniqueId()).cancel();
            unregister.remove(player.getUniqueId());
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(LemonBeehive.getInstance(), () -> {
            database.put(player.getUniqueId(), CustomHead.getByPlayer(player));
            DeveloperMode.notify("&eregister &6" + player.getName() + " &ehead!");
        });
    }

    public static void onPlayerQuit(Player player) {
        if (database.containsKey(player.getUniqueId())) {
            unregister.put(player.getUniqueId(), requestQuit(player, 2400));
        }
    }

    private static BukkitTask requestQuit(Player player, long timeOut) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), () -> {
            unregister.remove(player.getUniqueId());
            database.remove(player.getUniqueId());
            DeveloperMode.notify("&eunregister &6" + player.getName() + " &ehead &c" + timeOut);
        }, timeOut);
        return task;
    }

    public static HeadObject getHead(OfflinePlayer player) {
        return database.get(player.getUniqueId());
    }
}
