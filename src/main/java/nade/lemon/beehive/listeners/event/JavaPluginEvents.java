package nade.lemon.beehive.listeners.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.handlers.HologramHandler;

public class JavaPluginEvents extends LemonListeners{
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent e) {
		if (e.getPlugin() instanceof LemonBeehive) {
			try {
				HologramHandler.onPluginDisable();
			} catch (NoClassDefFoundError ignore) {}
		}
	}

} 
