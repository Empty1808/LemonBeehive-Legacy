package nade.lemon.beehive;

import org.bukkit.block.Chest;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;

public class StaticPlaceholder {

	private static ConfigBuild language;

	public static void onEnable() {
		language = LemonBeehive.getInstance().get(Language.class).get("language");
	}
	
	public static String area() {
		String area = language.get("storage-link.area", String.class);
		return area.replace("{num}", String.valueOf(StaticField.LINKED_DISTANCE));
	}
	
	public static String xyzStyle(int x, int y, int z) {
		String xyz = language.get("storage-link.location", String.class);
		return xyz.replace("{x}", String.valueOf(x)).replace("{y}", String.valueOf(y)).replace("{z}", String.valueOf(z));
	}
	
	public static String xyzStyle(BeehiveObject beehive) {
		if (!beehive.getStorageLink().has()) return language.get("storage-link.unset", String.class);
		Chest linked = beehive.getStorageLink().getFirst();
		return xyzStyle(linked.getX(), linked.getY(), linked.getZ());
	}
}
