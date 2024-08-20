package nade.lemon.beehive.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.utils.string.Color;

public class RandomName {
	static List<String[]> name = new ArrayList<>();
	private static final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");

	static {
		Set<String> key = BeehiveYamlConfig.getRandomName().getOrDefaultConfig("random-name", ConfigurationSection.class).getKeys(false);
		if (key.size() >= 0) {
			for (String str : key) {
				List<String> list = BeehiveYamlConfig.getRandomName().getList("random-name." + str, String.class);
				name.add(list.toArray(new String[list.size()]));
			}
		}
	}
	
	public static String random() {
		Random random = new Random();
		StringBuilder builder = new StringBuilder();
		if (name.size() <= 0) {
			return Color.vanilla("&f>&7>&8> &6LemonBeehive &8<&7<&f<");
		}
		for (String[] array : name) {
			if (builder.length() <= 0) {
				builder.append(array[random.nextInt(array.length)]);
				continue;
			}
			builder.append(" ").append(array[random.nextInt(array.length)]);
		}
		return builder.toString();
	}
	
	public static String setString(String string) {
		return Color.hex(string.replace("{random_name}", RandomName.random()));
	}
	
	public static String getString() {
		return Color.hex(language.get("beehive.item-interface.display-name", String.class).replace("{random-name}", RandomName.random()));
	}
	
	public static ItemStack setItem(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		String name = Color.hex(language.get("beehive.item-interface.display-name", String.class).replace("{random-name}", random()));
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
}