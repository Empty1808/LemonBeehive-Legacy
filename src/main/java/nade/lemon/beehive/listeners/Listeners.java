package nade.lemon.beehive.listeners;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.listeners.event.CraftBeeEvent;
import nade.lemon.beehive.listeners.event.CraftBlockEvent;
import nade.lemon.beehive.listeners.event.CraftInventoryEvents;
import nade.lemon.beehive.listeners.event.CraftPlayerEvent;
import nade.lemon.beehive.listeners.event.CraftWorldEvents;
import nade.lemon.beehive.listeners.event.JavaPluginEvents;
import nade.lemon.beehive.listeners.event.WhilelistEvent;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class Listeners {
	public Listeners(EmptyPlugin plugin) {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new CraftBeeEvent(), plugin);
		manager.registerEvents(new CraftBlockEvent(plugin), plugin);
		manager.registerEvents(new CraftInventoryEvents(plugin), plugin);
		manager.registerEvents(new CraftPlayerEvent(plugin), plugin);
		manager.registerEvents(new CraftWorldEvents(), plugin);
		manager.registerEvents(new JavaPluginEvents(), plugin);
		manager.registerEvents(new WhilelistEvent(plugin), plugin);
	}
}
