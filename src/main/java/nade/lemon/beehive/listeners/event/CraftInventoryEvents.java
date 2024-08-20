package nade.lemon.beehive.listeners.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.features.recipe.RecipeSystem;

public class CraftInventoryEvents extends LemonListeners {

	private EmptyPlugin plugin;

	public CraftInventoryEvents(EmptyPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onCraftItem(CraftItemEvent e) {
		plugin.get(RecipeSystem.class).getEvent().onCraftItem(e);
	}
}