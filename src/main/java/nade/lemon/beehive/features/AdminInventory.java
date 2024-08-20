package nade.lemon.beehive.features;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.features.editors.upgrades.UpgradeEditor;
import nade.lemon.beehive.features.harvest.HarvestSystem;
import nade.lemon.beehive.features.manager.ManageSystem;
import nade.lemon.beehive.features.recipe.RecipeSystem;
import nade.lemon.builders.Builders;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.inventory.InventoryBuilder;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.PlaceholderConfigBuild;
import nade.lemon.head.CustomHead;

public class AdminInventory {

    private EmptyPlugin plugin;
    private InventoryBuilder builder;
    private Language language;

    public AdminInventory(EmptyPlugin plugin) {
        this.plugin = plugin;
        this.builder = plugin.get(Builders.class).getInventoryBuilder();
        this.language = plugin.get(Language.class);
    }

    public void open(HumanEntity player) {
        InventoryBuild build = builder.register("").setTitle("{title}");

        PlaceholderConfigBuild placeholder = PlaceholderConfigBuild.build(language.get("admin"));
        placeholder.set("{title}", "main.title");
        placeholder.set("{manages}", "main.manages");
        placeholder.set("{recipe-editor}", "main.recipe-editor");
        placeholder.set("{reward-editor}", "main.reward-editor");
        placeholder.set("{upgrade-editor}", "main.upgrade-editor");
        build.setPlaceholder(placeholder);

        ItemBuild manages = ItemBuild.build(CustomHead.get("beehive")).setDisplayName("{manages}");
        ItemBuild recipes = ItemBuild.build(CustomHead.get("crafting-table")).setDisplayName("{recipe-editor}");
        ItemBuild rewards = ItemBuild.build(Material.SHEARS).setDisplayName("{reward-editor}");
        ItemBuild upgrades = ItemBuild.build(Material.ANVIL).setDisplayName("{upgrade-editor}").setGlowing(true);

        build.setItem(0, "manages", manages);
        build.setItem(1, "recipes", recipes);
        build.setItem(2, "rewards", rewards);
        build.setItem(3, "upgrades", upgrades);

        build.setClickEvent(listener -> {
            listener.setLocked(true);
            if (listener.isKeyed("manages")) {
                plugin.get(ManageSystem.class).open(listener.getPlayer(), 0);
            }else if (listener.isKeyed("recipes")) {
                plugin.get(RecipeSystem.class).getEditor().open(listener.getPlayer());
            }else if (listener.isKeyed("rewards")) {
                plugin.get(HarvestSystem.class).getEditor().open(listener.getPlayer(), 0);
            }else if (listener.isKeyed("upgrades")) {
                new UpgradeEditor(plugin).openCategory(listener.getPlayer(), "beehive");
            }
        });


        build.open(player);
    }
}