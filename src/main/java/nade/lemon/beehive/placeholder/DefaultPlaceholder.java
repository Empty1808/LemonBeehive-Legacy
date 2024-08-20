package nade.lemon.beehive.placeholder;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nade.lemon.utils.string.Color;

public class DefaultPlaceholder implements NekoPlacehoder{
	
	private Map<String, String> replaces = Maps.newHashMap();

	@Override
	public String place(String request) {
		return Color.hex(request);
	}
	
	@Override
	public List<String> place(List<String> requests) {
		if (requests == null) requests = Lists.newArrayList();
		for (int i = 0; i < requests.size(); i++) {
			requests.set(i, this.place(requests.get(i)));
		}
		return requests;
	}

	@Override
	public ItemStack place(ItemStack item) {
		if (item.getType() == Material.AIR) return item;
		item = new ItemStack(item);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(this.place(meta.getDisplayName()));
		meta.setLore(this.place(meta.getLore()));
		item.setItemMeta(meta);
		return item;
	}

	protected void setReplaces(String key, String replace) {
		replaces.put(key, replace);
	}

	protected String replaces(String request) {
		String result = request;
		for (String key : replaces.keySet()) {
			String values = replaces.get(key);
			result = result.replace(key, values == null ? "" : values);
		}
		return result;
	}
}
