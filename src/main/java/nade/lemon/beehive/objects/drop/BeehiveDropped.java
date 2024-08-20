package nade.lemon.beehive.objects.drop;

import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import nade.lemon.beehive.objects.BeehiveObject;

public class BeehiveDropped {

    public static List<Item> items(BeehiveObject beehive, Collection<ItemStack> items) {
		List<Item> result = Lists.newArrayList();
		for (ItemStack item : items) {
			Item entity = beehive.getWorld().dropItem(beehive.getDropLocation(), item);
			entity.setCustomNameVisible(true);
			entity.setCustomName(entity.getItemStack().getItemMeta().getDisplayName());
			result.add(entity);
		}
		return result;
	}

    public static List<Item> items(BeehiveObject beehive, ItemStack... items) {
		return BeehiveDropped.items(beehive, Lists.newArrayList(items));
	}
}
