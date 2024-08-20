package nade.lemon.beehive.placeholder;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface NekoPlacehoder {

	String place(String request);
	
	List<String> place(List<String> request);
	
	ItemStack place(ItemStack item);
}
