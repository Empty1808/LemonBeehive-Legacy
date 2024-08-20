package nade.lemon.beehive.handlers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldSaveEvent;

import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.objects.BeehiveObject;

public class DataSaveHandler {
	
	public static void onWorldSave(WorldSaveEvent e) {
		DataSaveHandler.save(e.getWorld());
	}
	
	public static void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equals("save-all")) {
			DataSaveHandler.save(Bukkit.getWorlds().toArray(new World[0]));
		}
	}
	
	private static void save(World... worlds) {
		for (World world : worlds) {
			for (BeehiveObject beehive : Database.getByWorld(world)) {
				beehive.save();
			}
		}
	}
}
