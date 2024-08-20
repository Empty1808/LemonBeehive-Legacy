package nade.lemon.beehive.data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;

public class Database {
	private static Map<Key, BeehiveObject> beehives = Maps.newHashMap();

	public static void addBeehive(Location location, BeehiveObject beehive) {
		beehives.put(new Key(location, beehive.getUniqueId(), beehive.getOwner().getUniqueId()), beehive);
	}

	public static void removeByLocation(Location location) {
		for (Key key : beehives.keySet()) {
			if (key.getLocation().equals(location)) {
				beehives.remove(key);
				break;
			}
		}
	}

	public static void removeByUniqueId(UUID uniqueId) {
		for (Key key : beehives.keySet()) {
			if (key.getUniqueId().equals(uniqueId)) {
				beehives.remove(key);
				break;
			}
		}
	}

	public static boolean containByLocation(Location location) {
		for (Key key : beehives.keySet()) {
			if (key.getLocation().equals(location)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containByUniqueId(UUID uuid) {
		for (Key key : beehives.keySet()) {
			if (key.getUniqueId().equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containByOwner(UUID owner) {
		for (Key key : beehives.keySet()) {
			if (key.getOwner().equals(owner)) {
				return true;
			}
		}
		return false;
	}

	public static BeehiveObject getByLocation(Location location) {
		for (Key key : beehives.keySet()) {
			if (key.getLocation().equals(location)) {
				return beehives.get(key);
			}
		}
		return null;
	}

	public static BeehiveObject getByUniqueId(UUID uuid) {
		for (Key key : beehives.keySet()) {
			if (key.getUniqueId().equals(uuid)) {
				return beehives.get(key);
			}
		}
		return null;
	}

	public static Collection<BeehiveObject> getByOwner(UUID owner) {
		Set<BeehiveObject> results = Sets.newHashSet();
		for (Key key : beehives.keySet()) {
			if (key.getOwner().equals(owner)) {
				results.add(beehives.get(key));
			}
		}
		return results;
	}


	public static Collection<BeehiveObject> getBeehives() {
		return beehives.values();
	}

	public static Set<BeehiveObject> getByWorld(World world) {
		Set<BeehiveObject> result = Sets.newHashSet();
		for (Key key : Database.beehives.keySet()) {
			if (key.getLocation().getWorld().equals(world)) {
				result.add(Database.getByLocation(key.getLocation()));
			}
		}
		return result;
	}

	public static void updateUpgrades() {
		for (BeehiveObject beehive : beehives.values()) {
			BeehiveUpdate.upgradeObjects();
			beehive.getMenu().update();
		}
	}

	public static void updateGUIs() {
		for (BeehiveObject beehive : beehives.values()) {
			BeehiveUpdate.upgradeObjects();
			beehive.getMenu().update();
		}
	}
}

class Key {
	private Location location;
	private UUID uuid;
	private UUID owner;

	Key(Location location, UUID uuid, UUID owner) {
		this.location = location;
		this.uuid = uuid;
		this.owner = owner;
	}

	public Location getLocation() {
		return location;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public UUID getOwner() {
		return owner;
	}
}
