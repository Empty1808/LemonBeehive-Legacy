package nade.lemon.beehive.chat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static nade.lemon.beehive.player.PlayerInputChat.*;

public class InputCompound {
	private Map<String, Object> compound = new HashMap<>();
	
	public InputCompound() {}
	public InputCompound(InputObject... objects) {
		for (InputObject object : objects) {
			this.set(object.getKey(), object.getValue());
		}
	}
	
	public Object set(String key, Object value) {
		return compound.put(key, value);
	}
	
	public Object get(String key) {
		return compound.get(key);
	}

	public <E> E get(String key, Class<E> clazz) {
		Object object = this.get(key);
		if (clazz.isInstance(object)) {
			return clazz.cast(object);
		}
		return null;
	}
	
	public String getString(String key) {
		return this.get(key, String.class);
	}
	
	public Integer getInteger(String key) {
		Object object = get(key);
		if (object instanceof Integer) {
			return (Integer) object;
		}
		return 0;
	}
	
	public InventoryHolder getInventoryHolder(String key) {
		Object object = get(key);
		if (object instanceof InventoryHolder) {
			return (InventoryHolder) object;
		}
		return null;
	}
	
	public Inventory getInventory(String key) {
		Object object = get(key);
		if (object instanceof Inventory) {
			return (Inventory) object;
		}
		return null;
	}
	
	public Object hasKey(String key) {
		return compound.containsKey(key);
	}
	
	public Set<String> getKeys() {
		return compound.keySet();
	}
	
	public Collection<Object> getItems() {
		return compound.values();
	}
}
