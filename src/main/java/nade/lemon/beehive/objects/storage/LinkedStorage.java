package nade.lemon.beehive.objects.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.drop.BeehiveDropped;

public class LinkedStorage {

	private BeehiveObject beehive;
	private Map<Location, Chest> linked = new HashMap<>();
	
	public LinkedStorage(BeehiveObject beehive) {
		this.beehive = beehive;
	}
	
	public Chest add(Location location, Chest chest) {
		linked.clear();
		return linked.put(location, chest);
	}
	
	public Chest add(Chest chest) {
		return this.add(chest.getLocation(), chest);
	}
	
	public Chest remove(Chest chest) {
		return this.remove(chest.getLocation());
	}
	
	public Chest remove(Location location) {
		return linked.remove(location);
	}
	
	public boolean contains(Chest chest) {
		return linked.containsValue(chest);
	}
	
	public boolean contains(Location location) {
		return linked.containsKey(location);
	}
	
	public boolean has() {
		return linked.size() > 0;
	}
	
	public void clear() {
		linked.clear();
	}
	
	public boolean containsKey(Location location) {
		return linked.containsKey(location);
	}
	
	public boolean containsValue(Chest chest) {
		return linked.containsValue(chest);
	}

	public boolean addItem(Location location, ItemStack... items) {
		Chest chest = linked.get(location);
		if (chest != null) {
			chest.getInventory().addItem(items);
			return true;
		}
		
		return false;
	}
	
	public void addItems(ItemStack... items) {
		if (getFirstInventory() != null) {
			HashMap<Integer, ItemStack> redundant = getFirstInventory().addItem(items);
			BeehiveDropped.items(beehive, redundant.values());
		}else {
			BeehiveDropped.items(beehive, items);
		}
	}
	
	public BeehiveObject getBeehiveObject() {
		return beehive;
	}
	
	public Collection<Inventory> getInventorys() {
		Collection<Inventory> inventorys = new ArrayList<>();
		for (Chest chest : linked.values()) {
			inventorys.add(chest.getInventory());
		}
		return inventorys;
	}
	
	public Chest getFirst() {
		Object[] array = linked.values().toArray();
		return array.length > 0 ? (Chest) array[0] : null;
	}
	
	public Chest getLast() {
		Object[] array = linked.values().toArray();
		return array.length > 0 ? (Chest) array[array.length-1] : null;
	}
	
	public Inventory getFirstInventory() {
		Chest first = getFirst();
		return first != null ? first.getInventory() : null;
	}
	
	public boolean isChest(Location location) {
		return location.getBlock().getType().equals(Material.CHEST);
	}
}