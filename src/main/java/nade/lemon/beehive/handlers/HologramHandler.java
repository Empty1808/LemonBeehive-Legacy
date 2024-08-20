package nade.lemon.beehive.handlers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.FeaturesEnable;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.library.hologram.Holograms;

public class HologramHandler {
	public static final Set<Holograms> holograms = new HashSet<>();
	
	public static void add(Holograms hologram) {
		holograms.add(hologram);
		hologram.show();
	}

	public static void remove(Holograms hologram) {
		holograms.remove(hologram);
		hologram.hide();
	}

	public static void onPluginDisable() {
		if (HologramHandler.isEnable()) {
			for (Holograms holograms : holograms) {
				holograms.hide();
			}
			holograms.clear();
		}
	}
	
	public static void onShow(Player player) {
		for (BeehiveObject beehive : Database.getBeehives()) {
			if (player.getWorld().getName().equals(beehive.getWorld().getName())) {
				beehive.getHolograms().show(player);
			}
		}
	}
	
	public static void onHide(Player player) {
		for (BeehiveObject beehive : Database.getBeehives()) {
			if (player.getWorld().getName().equals(beehive.getWorld().getName())) {
				beehive.getHolograms().hide(player);
			}
		}
	}
	
	public static void onPlayerDeath(PlayerDeathEvent e) {
		onHide(e.getEntity());
	}
	
	public static void onPlayerRespawn(PlayerRespawnEvent e) {
		if (BeehiveYamlConfig.getConfig().get("general.beehive.display-hologram.style", String.class).equalsIgnoreCase("always")) {
			Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), () -> onShow(e.getPlayer()), 0);
		}
	}
	
	public static void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
		if (HologramHandler.isEnable() && BeehiveYamlConfig.getConfig().get("general.beehive.display-hologram.style", String.class).equalsIgnoreCase("always")) {
			for (BeehiveObject beehive : Database.getBeehives()) {
				beehive.getHolograms().hide(e.getPlayer());
				if (e.getPlayer().getWorld().getName().equals(beehive.getWorld().getName())) {
					beehive.getHolograms().show(e.getPlayer());
				}
			}
		}
	}

	private static boolean isEnable() {
		return FeaturesEnable.DISPLAY_HOLOGRAM.isEnable();
	}
	
	public static void onPlayerJoin(PlayerJoinEvent e) {
		if (BeehiveYamlConfig.getConfig().get("general.beehive.display-hologram.style", String.class).equalsIgnoreCase("always")) {
			onShow(e.getPlayer());
		}
	}
	
	public static void onPlayerQuit(PlayerQuitEvent e) {
		onHide(e.getPlayer());
	}
}