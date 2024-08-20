package nade.lemon.beehive.features.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.StaticField;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.beehive.features.AdminInventory;
import nade.lemon.beehive.utils.bukkit.Players;
import nade.lemon.builders.Builders;
import nade.lemon.builders.Properties;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.inventory.InventoryBuilder;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.head.CustomHead;

public class RecipeEditor extends Properties{
    
    private EmptyPlugin plugin;
    private RecipeSystem recipe;
    private InventoryBuilder buidler;
    private Language language;
    private ConfigBuild admin;

	private ConfigurationBuild config;

    public RecipeEditor(EmptyPlugin plugin, RecipeSystem recipe) {
        this.plugin = plugin;
        this.recipe = recipe;
        this.buidler = plugin.get(Builders.class).getInventoryBuilder();
        this.language = plugin.get(Language.class);
        this.admin = this.language.get("admin");

		this.config = BeehiveYamlConfig.getConfig();
		this.properties.set("default-recipe", config.get("general.beehive-recipe.default-recipe", Boolean.class));

		if (!this.properties.getOrDefault("default-recipe", false, Boolean.class)) {
			this.removeDefault(false);
		}
    }

    public void open(HumanEntity player) {
        InventoryBuild build = buidler.register("").setSize(45);
		BeehiveConfigBuild custom_recipe = BeehiveYamlConfig.getCustomRecipe();
		build.setItems(new int[] {
			 0,  1,      3,  4,  5,  6,  7,  8,
			 9,             13, 14, 15, 16, 17,
			18,             22, 23,     25, 26,
			27,             31, 32, 33, 34, 35,
			36, 37, 38, 39, 40, 41, 42, 43, 44
		}, "glass", ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));

        build.setItem(10, custom_recipe.get("recipe-1.slot-1", ItemStack.class));
		build.setItem(11, custom_recipe.get("recipe-1.slot-2", ItemStack.class));
		build.setItem(12, custom_recipe.get("recipe-1.slot-3", ItemStack.class));
		build.setItem(19, custom_recipe.get("recipe-1.slot-4", ItemStack.class));
		build.setItem(20, custom_recipe.get("recipe-1.slot-5", ItemStack.class));
		build.setItem(21, custom_recipe.get("recipe-1.slot-6", ItemStack.class));
		build.setItem(28, custom_recipe.get("recipe-1.slot-7", ItemStack.class));
		build.setItem(29, custom_recipe.get("recipe-1.slot-8", ItemStack.class));
		build.setItem(30, custom_recipe.get("recipe-1.slot-9", ItemStack.class));

        build.setItem(24, "save", ItemBuild.build(CustomHead.get("crafting-table")).setDisplayName(admin.get("custom-recipe.save", String.class)));
		build.setItem(1, "material-replace", ItemBuild.build(CustomHead.get("oak-planks")).setDisplayName(admin.get("custom-recipe.material-replace", String.class)));
		build.setItem(2, "default-recipe", ItemBuild.build(CustomHead.get("Beehive")).setDisplayName(admin.get("custom-recipe.default-recipe", String.class)));
		build.setItem(36, "back", ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(admin.get("global.back", String.class)));

        build.setKeyedEvent("glass", (e) -> {
				e.setLocked(true);
				Players.undiscoverRecipe(NamespacedKey.minecraft("beehive"));
			})
			.setKeyedEvent("material-replace", listener -> {
				listener.setLocked(true);
				if (listener.getClickType() == ClickType.LEFT) {
					BeehiveConfigBuild config = BeehiveYamlConfig.getConfig();
					StaticField.MATERIAL_REPLACE = !StaticField.MATERIAL_REPLACE;						
					config.set("general.beehive-recipe.material-replace", StaticField.MATERIAL_REPLACE);
					config.save();
					listener.update();
					recipe.reload();
				}
			})
			.setKeyedEvent("default-recipe", e -> {
				e.setLocked(true);
				if (e.isClickType(ClickType.LEFT)) {
					ConfigurationBuild config = BeehiveYamlConfig.getConfig();
					System.err.println(properties.get("default-recipe"));
					properties.set("default-recipe", !properties.get("default-recipe", Boolean.class));
					config.set("general.beehive-recipe.default-recipe", properties.get("default-recipe", Boolean.class));
					config.save();
					e.update();
					this.removeDefault(properties.get("default-recipe", Boolean.class));
				}
			})
			.setKeyedEvent("save", listener -> {
				listener.setLocked(true);
				if (listener.getClickType() == ClickType.LEFT) {
					if (recipe.save(listener.getItems(10, 11, 12, 19, 20, 21, 28, 29, 30))) {
						listener.close();
					}
				}
			})
			.setKeyedEvent("back", listener -> {
				listener.setLocked(true);
				if (listener.getClickType() == ClickType.LEFT) {
					plugin.get(AdminInventory.class).open(listener.getPlayer());
				}
			});


		PlaceholderBuild placeholder = PlaceholderBuild.build();
		placeholder.set("{material-replace}", () -> {
			return (StaticField.MATERIAL_REPLACE ? admin.get("global.enable", String.class) : admin.get("global.disable", String.class));
		});
		System.err.println(properties.get("default-recipe"));
		placeholder.set("{default-recipe}", () -> {
			return this.properties.get("default-recipe", Boolean.class) ? admin.get("global.enable", String.class) : admin.get("global.disable", String.class);
		});
		build.setPlaceholder(placeholder);

		build.open(player);
    }

	private void removeDefault(boolean toggle) {
		NamespacedKey key = NamespacedKey.minecraft("beehive");
		if (!toggle) {
			properties.set("recipe", Bukkit.getRecipe(key));
			Players.undiscoverRecipe(key);
			Bukkit.removeRecipe(key);
		}else {
			Bukkit.addRecipe(properties.get("recipe", Recipe.class));
		}
	}
}
