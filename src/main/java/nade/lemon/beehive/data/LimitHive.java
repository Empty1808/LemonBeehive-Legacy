package nade.lemon.beehive.data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.permissions.Permission;

import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.utils.Utilities;

public class LimitHive {

	private static HashMap<UUID, Integer> limit = new HashMap<>();
	private static HashMap<Permission, Integer> limitPerm = new HashMap<>();
	
	public static void add(UUID playerUUID) {
		if (!limit.containsKey(playerUUID)) {
			limit.put(playerUUID, 1);
		}else {
			int amount = limit.get(playerUUID);
			limit.put(playerUUID, amount + 1);
		}
	}
	
	public static void remove(UUID playerUUID) {
		if (!limit.containsKey(playerUUID)) {
			return;
		}else {
			int amount = limit.get(playerUUID);
			if (amount == 0) {
				return;
			}
			limit.put(playerUUID, amount - 1);
		}
	}
	
	public static HashMap<UUID, Integer> getLimit() {
		return limit;
	}
	
	public static HashMap<Permission, Integer> getLimitPerm() {
		return limitPerm;
	}
	
	public static void onEnable() {
		limit = new HashMap<>();
		for (BeehiveObject manager : Database.getBeehives()) {
			UUID owner = manager.getOwner().getUniqueId();
			add(owner);
		}
		loadPerm();
	}
	
	public static int getLimit(UUID playerUUID) {
		if (!limit.containsKey(playerUUID)) {
			return 0;
		}
		return limit.get(playerUUID);
	}
	
	private static void loadPerm() {
		List<String> list = BeehiveYamlConfig.getConfig().getList("general.beehive.limit.permission", String.class);
		if (list.isEmpty()) {
			return;
		}
		for (String string : list) {
			cutString(string);
		}
	}
	
	private static void cutString(String perm) {
		String[] permissionArray = perm.split(";");
		if (Utilities.isIntegers(permissionArray[1])) {
			Permission permission = new Permission(permissionArray[0]);
			int limit = Integer.parseInt(permissionArray[1]);
			limitPerm.put(permission, limit);
		}
	}
}
