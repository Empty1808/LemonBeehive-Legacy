package nade.lemon.beehive.features.recipe;

import org.bukkit.event.inventory.CraftItemEvent;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.utils.RandomName;
import nade.lemon.utils.Logger;

public class RecipeEvent {
    
    private EmptyPlugin plugin;
    private RecipeSystem recipe;

    RecipeEvent(EmptyPlugin plugin, RecipeSystem recipe) {
		this.plugin = plugin;
        this.recipe = recipe;
    }

    public void onDisable() {
		plugin.get(Logger.class).info("unregister crafing recipe...");
		recipe.removeRecipe();
	}

    public void onCraftItem(CraftItemEvent e) {
		if (recipe.isEnable()) {
			if (recipe.shapedRecipe != null && recipe.shapedRecipe.getResult().equals(e.getRecipe().getResult())) {
				RandomName.setItem(recipe.enableRandomName(e.getCurrentItem()));
			}
		}
	}
}
