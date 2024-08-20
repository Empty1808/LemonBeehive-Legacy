package nade.lemon.beehive.handlers.players;

import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Maps;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.listeners.event.custom.PlayerChangeTargetBlockEvent;

public class BlockTargetListener {
    private BukkitTask task;
    private final EmptyPlugin plugin;
    private final HumanEntity player;

    private Block targetBlock;

    public static final Map<HumanEntity, BlockTargetListener> listeners = Maps.newHashMap();

    public BlockTargetListener(EmptyPlugin plugin, HumanEntity player) {
        this.plugin = plugin;
        this.player = player;
        this.targetBlock = player.getTargetBlock(null, 3);

        this.onRunnable();
    }

    private void onRunnable() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Objects.isNull(player) && !Objects.isNull(task)) {
                task.cancel();
                return;
            }
            Block current = player.getTargetBlock(null, 4);
            if (!Objects.isNull(targetBlock) && !targetBlock.equals(current)) {
                Bukkit.getPluginManager().callEvent(new PlayerChangeTargetBlockEvent(player, current, targetBlock));
            }
            targetBlock = player.getTargetBlock(null, 4);
        }, 0, 1);
    }

    public static void onReloadConfig(EmptyPlugin plugin) {
        if (BeehiveYamlConfig.getConfig().get("general.beehive.display-hologram.style", String.class).equalsIgnoreCase("looking")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!listeners.containsKey(player)) {
                    listeners.put(player, new BlockTargetListener(plugin, player));
                }
            }
        }
    }

    public static void onPluginEnable(EmptyPlugin plugin) {
        if (BeehiveYamlConfig.getConfig().get("general.beehive.display-hologram.style", String.class).equalsIgnoreCase("looking")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                listeners.put(player, new BlockTargetListener(plugin, player));
            }
        }
    }

    public static void onPlayerJoin(EmptyPlugin plugin, PlayerJoinEvent e) {
        if (BeehiveYamlConfig.getConfig().get("general.beehive.display-hologram.style", String.class).equalsIgnoreCase("looking")) {
            listeners.put(e.getPlayer(), new BlockTargetListener(plugin, e.getPlayer()));
        }
    }
}