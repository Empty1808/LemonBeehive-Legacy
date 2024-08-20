 package nade.lemon.beehive.utils;

import java.lang.reflect.Array;
import java.util.*;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.tag.CompoundTag;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.Lists;

import nade.lemon.utils.bukkit.BukkitKeyed;
import nade.lemon.utils.collect.ItemStackL;
import nade.lemon.utils.string.Color;

public class Utilities {
	private static BukkitKeyed keyed = LemonBeehive.getInstance().get(BukkitKeyed.class);
	private static ItemStackL stackL = LemonBeehive.getInstance().get(ItemStackL.class);

	private static final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");

	public static ItemStack getCommandItemStack(OfflinePlayer owner, int amount) {
		ItemStack item = getItemStack(owner, amount);
		item.setAmount(amount);
		return item;
	}
	
	public static ItemStack getItemStack(OfflinePlayer owner, int amount) {
		CompoundTag tags = new CompoundTag();
		CompoundTag upgrades = new CompoundTag();
		CompoundTag storage = new CompoundTag();
		tags.setString("owner", (owner != null ? owner.getUniqueId().toString() : "nobody"));
		tags.setString("display_name", "none");
		tags.setString("location", "nil");
		tags.setStringList("friends", Lists.newArrayList());
		for (UpgradeType types : UpgradeType.values()) {
			upgrades.setInt(types.getType(), 1);
		}
		storage.setInt("honey", 0);
		storage.setInt("entity", 0);
		storage.setString("linked", "nil");
		tags.setCompound("upgrades", upgrades);
		tags.setCompound("storages", storage);

		return getItemStack(owner, new ItemStack(Material.BEEHIVE, amount), "none", tags);
	}
	
	private static ItemStack getItemStack(OfflinePlayer owner, ItemStack item, String display, CompoundTag tags) {
		ItemMeta meta = item.getItemMeta();
		String displayName = display.equals("none") ? RandomName.getString() : display;
		List<String> lore = new ArrayList<>();
		for (String str : language.getList("beehive.item-interface.lore", String.class)) {
			lore.add(placeholder(str, owner, tags));
		}
		meta.setDisplayName(placeholder(displayName, owner, tags));
		meta.setLore(Color.hex(lore));
		if (language.get("beehive.item-interface.glowing", Boolean.class)) {
			meta.addEnchant(Enchantment.DURABILITY, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		tags.setString("display_name", displayName);
		item.setItemMeta(meta);
		return stackL.load(item, tags);
	}

	private static String placeholder(String target, OfflinePlayer owner, CompoundTag tags) {
		String result = new String(target);
		CompoundTag upgrades = tags.getCompound("upgrades");
		CompoundTag storages = tags.getCompound("storages");
		UpgradeSystem upgradeSystem = LemonBeehive.getInstance().get(UpgradeSystem.class);
		for (UpgradeType types : UpgradeType.values()) {
			result = result.replace("{" + types.getType() + "_level}", String.valueOf(upgrades.getInt(types.getType())));
		}
		return result.replace("{owner}", owner != null ? owner.getName() : Color.hex(language.get("beehive.no-owner", String.class)))
					 .replace("{honey}", String.valueOf(storages.getInt("honey", 0)))
			  		 .replace("{max-honey}", String.valueOf(upgradeSystem.getByLevel(UpgradeType.valueOf("HONEY_CAPACITY"), upgrades.getInt("honey-capacity")).getValue().intValue()))
			  		 .replace("{entity}", String.valueOf(tags.getCompoundList("bees").size()))
			 		 .replace("{max-entity}", String.valueOf(upgradeSystem.getByLevel(UpgradeType.valueOf("BEEHIVE_CAPACITY"), upgrades.getInt("beehive-capacity")).getValue().intValue()));
	}
	
	public static ItemStack getItemStack(BeehiveObject beehive) {
		return getItemStack(beehive, new ItemStack(Material.BEEHIVE));
	}
	
	public static ItemStack getItemStack(BeehiveObject manager, ItemStack item) {
		return getItemStack(manager.getOwner(), item, manager.getDisplayName(), manager.getTags());
	}

	public static CompoundTag getCompoundTag(ItemStack item) {
		return stackL.save(item);
	}
	
	public static CompoundTag fromInformation(String info) {
		CompoundTag tags = new CompoundTag();
		CompoundTag upgrades = new CompoundTag();
		CompoundTag storages = new CompoundTag();
		String[] array = info.split("#");
		upgrades.setInt("honeycomb", Integer.parseInt(array[0]));
		upgrades.setInt("pollinate", Integer.parseInt(array[1]));
		upgrades.setInt("beehive-capacity", Integer.parseInt(array[2]));
		upgrades.setInt("honey-capacity", Integer.parseInt(array[3]));
		storages.setInt("honey", Integer.parseInt(array[4]));
		storages.setInt("entity", 0);
		storages.setString("linked", "nil");
		tags.setCompound("upgrades", upgrades);
		tags.setCompound("storages", storages);
		return tags;
	}
	
	public static int getInt(String str) {
		return getInt(str, 0);
	}

	public static int getInt(String str, int def) {
		if (!isInteger(str)) return def;
		return Integer.parseInt(str);
	}
	
	public static Integer[] getInteger(String... args) {
		Integer[] array = new Integer[args.length];
		for (int i = 0; i < args.length; i++) {
			array[i] = getInt(args[i]);
		}
		return array;
	}
	
	public static int[] getInteger(int size, String... args) {
		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			array[i] = getInt(args[i]);
		}
		return array;
	}

	public static double getDouble(String str) {
		return getDouble(str, 0d);
	}

	public static double getDouble(String str, double def) {
		if (!isDouble(str)) return def;
		return Double.parseDouble(str);
	}

	public static double[] getDoubles(String... vars) {
		return getDoubles(vars.length, vars);
	}

	public static double[] getDoubles(int size, String... args) {
		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = getDouble(args[i]);
		}
		return array;
	}
	
	private static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static boolean isIntegers(String... args) {
		for (String str : args) {
			if (!isInteger(str)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isDouble(String var) {
		try {
			Double.parseDouble(var);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean isDoubles(String... vars) {
		for (String var : vars) {
			if (!isDouble(var)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isEqualsZero(Integer... args) {
		for (Integer integer : args) {
			if (integer == 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isEqualsZero(double... vars) {
		for (Double var : vars) {
			if (var <= 0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isLocalItemStack(ItemStack item) {
		if (item.getType() != Material.BEEHIVE) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		if (!meta.getPersistentDataContainer().has(keyed.byString("internal"), PersistentDataType.STRING)) {
			return false;
		}
		return true;
	}
	
	public static UUID getOwner(ItemStack item) {
		CompoundTag tags = stackL.save(item);
		return UUID.fromString(tags.getString("owner"));
	}
	
	public static String getStringOwner(ItemStack item) {
		CompoundTag tags = stackL.save(item);
		String uuid = tags.getString("owner");
		if (uuid.equalsIgnoreCase("nobody")) return uuid;
		OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		return (owner != null ? owner.getName() : "nobody");
	}
	
	public static List<String> stringToList(String string) {
		return Arrays.asList(string.split("/"));
	}

	public static Object listToArrays(List<?> list, Class<?> parent) {
		Object array = Array.newInstance(parent, list.size());
		for (int i = 0; i < list.size(); i++) {
			Array.set(array, i, list.get(i));
		}
		return array;
	}
	public static void setContent(Inventory inventory, ItemStack item, int... slots) {
		for (int slot : slots) {
			inventory.setItem(slot, item);
		}
	}
}
