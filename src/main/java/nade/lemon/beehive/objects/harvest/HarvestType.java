package nade.lemon.beehive.objects.harvest;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import nade.lemon.utils.string.Color;

public enum HarvestType {
	Shears(Material.SHEARS, "shears",createItemStack(Material.HONEYCOMB, "&6Honeycomb", true)),
	Bottle(Material.GLASS_BOTTLE, "bottle", createItemStack(Material.HONEY_BOTTLE, "&6Honey Bottle", true));
	
	private Material material;
	private String sectionPath;
	private ItemStack defaultReward;
	
	private HarvestType(Material material, String sectionPath, ItemStack defaultReward) {
		this.material = material;
		this.sectionPath = sectionPath;
		this.defaultReward = defaultReward;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public String getSectionPath() {
		return sectionPath;
	}
	
	public ItemStack getDefaultReward() {
		return new ItemStack(defaultReward);
	}
	
	private static ItemStack createItemStack(Material material, String displayName, boolean glow) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Color.hex(displayName));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		if (glow ) {
			meta.addEnchant(Enchantment.DURABILITY, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		item.setItemMeta(meta);
		return item;
	}
}
