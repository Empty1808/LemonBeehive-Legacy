package nade.lemon.beehive.listeners.event;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.handlers.DataSaveHandler;
import nade.lemon.beehive.objects.BeehiveObject;

public class CraftWorldEvents extends LemonListeners{

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) {
		DataSaveHandler.onWorldSave(e);
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		for (BeehiveObject object : Database.getByWorld(e.getWorld())) {
			Location location = object.getLocation();
			if ((location.getBlockX()>>4) == e.getChunk().getX() && (location.getBlockZ()>>4) == e.getChunk().getZ()) {
				object.onLoaded();
			}
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		for (BeehiveObject object : Database.getByWorld(e.getWorld())) {
			Location location = object.getLocation();
			if ((location.getBlockX()>>4) == e.getChunk().getX() && (location.getBlockZ()>>4) == e.getChunk().getZ()) {
				object.onUnloaded();
			}
		}
	}
}