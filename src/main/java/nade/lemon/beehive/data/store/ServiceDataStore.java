package nade.lemon.beehive.data.store;

import org.bukkit.Location;

import nade.lemon.beehive.objects.BeehiveObject;

public interface ServiceDataStore {
	
	void remove(BeehiveObject location);
	
	void add(BeehiveObject manager);
	
	void update(BeehiveObject beehive);
	
	Object get(String key, Location location);
	
	int loading();
	
	int onSave();

	DataStoreType getType();
	
	public static enum Key {
		Upgrade,
		Location,
		Honeycomb,
		Pollinate,
		BeeCapacity,
		Honey,
		HoneyCapacity,
		Friends,
		DisplayName,
		Owner;
	}
}
