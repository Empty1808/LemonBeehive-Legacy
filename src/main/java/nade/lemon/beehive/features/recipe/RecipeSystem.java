package nade.lemon.beehive.features.recipe;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import nade.lemon.beehive.StaticField;
import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.FeaturesEnable;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.utils.string.Color;
import nade.lemon.beehive.utils.Utilities;
import nade.lemon.beehive.utils.bukkit.Materials;
import nade.lemon.beehive.utils.bukkit.Players;
import nade.lemon.builders.Properties;
import nade.lemon.utils.Logger;
import nade.lemon.utils.bukkit.BukkitKeyed;
import nade.lemon.utils.bukkit.ItemStacks;

public class RecipeSystem extends Properties{
	private static final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");

	ShapedRecipe shapedRecipe;
	public static String RECIPE_ITEMDISPLAY = language.get("crafting.displayname", String.class);
	
	private static Set<Material> planks = Materials.endWith("_planks");
	private static Set<Material> log = Materials.endWith("_log");
	private static Set<Material> wood = Materials.endWith("_wood");

	private RecipeEditor inventory;
	private RecipeEvent event;

	private NamespacedKey key;
	
	public RecipeSystem(EmptyPlugin plugin) {
		this.inventory = new RecipeEditor(plugin, this);
		this.event = new RecipeEvent(plugin, this);
		
		Material.getMaterial(RECIPE_ITEMDISPLAY);

		this.key = plugin.get(BukkitKeyed.class).byString("beehive");

		Logger logger = plugin.get(Logger.class);
		if (isEnable()) {
			logger.info("&7register crating recipe...");
			this.reload();
		}else {
			logger.info("&7crafting recipe has been disabled");
		}
	}

	public RecipeEditor getEditor() {
		return inventory;
	}

	public RecipeEvent getEvent() {
		return event;
	}

	public NamespacedKey getBukkitKeyed() {
		return key;
	}

	public void reload() {
		BeehiveConfigBuild build = BeehiveYamlConfig.getCustomRecipe();
		if (build.contains("recipe-1", ConfigurationSection.class)) {
			this.shapedRecipe = new ShapedRecipe(this.key, this.getResult());
			this.shapedRecipe.shape("123", "456", "789");
			
			ItemStack i1 = build.get("recipe-1.slot-1", ItemStack.class);
			ItemStack i2 = build.get("recipe-1.slot-2", ItemStack.class);
			ItemStack i3 = build.get("recipe-1.slot-3", ItemStack.class);
			
			ItemStack i4 = build.get("recipe-1.slot-4", ItemStack.class);
			ItemStack i5 = build.get("recipe-1.slot-5", ItemStack.class);
			ItemStack i6 = build.get("recipe-1.slot-6", ItemStack.class);
			
			ItemStack i7 = build.get("recipe-1.slot-7", ItemStack.class);
			ItemStack i8 = build.get("recipe-1.slot-8", ItemStack.class);
			ItemStack i9 = build.get("recipe-1.slot-9", ItemStack.class);

			this.update(i1, i2, i3, i4, i5, i6, i7, i8, i9);
		}
	}
    
    public boolean save(ItemStack... items) {
    	if (!isNullList(items)) {
    		update(items);
    		return true;
    	}
    	return false;
    }
	
	ItemStack enableRandomName(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(meta.getDisplayName().replace(RECIPE_ITEMDISPLAY, "{random-name}"));
		item.setItemMeta(meta);
		return item;
	}
	
	private void setYaml(ItemStack[] items) {
        BeehiveConfigBuild build = BeehiveYamlConfig.getCustomRecipe();

        build.set("recipe-1.slot-1", items[0]);
		build.set("recipe-1.slot-2", items[1]);
        build.set("recipe-1.slot-3", items[2]);

        build.set("recipe-1.slot-4", items[3]);
        build.set("recipe-1.slot-5", items[4]);
        build.set("recipe-1.slot-6", items[5]);

        build.set("recipe-1.slot-7", items[6]);
        build.set("recipe-1.slot-8", items[7]);
        build.set("recipe-1.slot-9", items[8]);
			
		build.save();

	}
	
	private boolean isNullList(ItemStack[] items) {
		boolean isNull = true;
		for (ItemStack item : items) {
			if (item != null) {
				isNull = false;
			}else {
				item = new ItemStack(Material.AIR);
			}
		}
		
		if (isNull) {
			return true;
		}
		return false;
	}
	
	private void update(ItemStack... items) {
		items = this.checkNull(items);
		this.setIngredient(items);
		this.update();
		this.setYaml(items);
	}
	
	private void update() {
		removeRecipe();
		if (BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive-recipe.enable", Boolean.class)) {
			this.addRecipe(this.shapedRecipe);
		}
	}
	
	private void addRecipe(ShapedRecipe recipe) {
		this.shapedRecipe = recipe;
		Bukkit.addRecipe(recipe);
		
		Players.discoverRecipe(this.key);
	}
	
	public void removeRecipe() {
		Bukkit.removeRecipe(this.key);
		Players.undiscoverRecipe(this.key);
	}
	
	private ItemStack getResult() {
		ItemStack result = Utilities.getCommandItemStack(null, 1);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(Color.hex(language.get("beehive.item-interface.display-name", String.class).replace("{random-name}", RECIPE_ITEMDISPLAY)));
		result.setItemMeta(meta);
		return result;
	}
	
	private List<ItemStack> getItems(Collection<Material> materials, ItemStack item) {
		List<ItemStack> list = Lists.newArrayList();
		
		for (Material material : materials) {
			ItemStack clone = ItemStacks.newItemStack(item);
			clone.setType(material);
			list.add(clone);
		}
		
		return list;
	}

	private ItemStack[] checkNull(ItemStack... items) {
		ItemStack[] result = new ItemStack[items.length];
		for (int i = 0; i < items.length; i++) {
			if (Objects.isNull(items[i])) {
				result[i] = new ItemStack(Material.AIR);
			}else {
				result[i] = items[i];
			}
		}
		return result;
	}
	
	private void setIngredient(ItemStack... items) {
		if (!isNullList(items)) {
			List<ItemStack> a = Lists.newArrayList();
			List<ItemStack> b = Lists.newArrayList();
			List<ItemStack> c = Lists.newArrayList();
			
			List<ItemStack> d = Lists.newArrayList();
			List<ItemStack> e = Lists.newArrayList();
			List<ItemStack> f = Lists.newArrayList();
			
			List<ItemStack> g = Lists.newArrayList();
			List<ItemStack> h = Lists.newArrayList();
			List<ItemStack> i = Lists.newArrayList();
			
			if (StaticField.MATERIAL_REPLACE) {
				for (int count = 0; count < items.length; count++) {
					if (items[count] == null && items[count].getType() == Material.AIR) continue;
					List<ItemStack> list = Lists.newArrayList();
					ItemStack item = items[count];
					
					if (planks.contains(item.getType())) list.addAll(this.getItems(planks, item));
					else if (log.contains(item.getType())) list.addAll(this.getItems(log, item));
					else if (wood.contains(item.getType())) list.addAll(this.getItems(wood, item));
					else list.add(item);
					
					if (count == 0) a.addAll(list);
					if (count == 1) b.addAll(list);
					if (count == 2) c.addAll(list);

					if (count == 3) d.addAll(list);
					if (count == 4) e.addAll(list);
					if (count == 5) f.addAll(list);

					if (count == 6) g.addAll(list);
					if (count == 7) h.addAll(list);
					if (count == 8) i.addAll(list);
				}
			}else {
				a.add(items[0]);
				b.add(items[1]);
				c.add(items[2]);
				
				d.add(items[3]);
				e.add(items[4]);
				f.add(items[5]);
				
				g.add(items[6]);
				h.add(items[7]);
				i.add(items[8]);
			}

			this.shapedRecipe.setIngredient('1', new RecipeChoice.ExactChoice(a));
			this.shapedRecipe.setIngredient('2', new RecipeChoice.ExactChoice(b));
			this.shapedRecipe.setIngredient('3', new RecipeChoice.ExactChoice(c));
			
			this.shapedRecipe.setIngredient('4', new RecipeChoice.ExactChoice(d));
			this.shapedRecipe.setIngredient('5', new RecipeChoice.ExactChoice(e));
			this.shapedRecipe.setIngredient('6', new RecipeChoice.ExactChoice(f));
			
			this.shapedRecipe.setIngredient('7', new RecipeChoice.ExactChoice(g));
			this.shapedRecipe.setIngredient('8', new RecipeChoice.ExactChoice(h));
			this.shapedRecipe.setIngredient('9', new RecipeChoice.ExactChoice(i));
		}
	}
	
	public boolean isEnable() {
		return (FeaturesEnable.RECIPE.isEnable());
	}
}