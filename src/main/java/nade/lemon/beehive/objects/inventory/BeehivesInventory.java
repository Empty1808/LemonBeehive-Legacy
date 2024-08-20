package nade.lemon.beehive.objects.inventory;

import org.bukkit.entity.Player;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.objects.upgrades.BeehiveUpgrade;
import nade.lemon.beehive.placeholder.BeehiveObjectPlaceholder;
import nade.lemon.beehive.player.PlayerInputChest;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.builders.inventory.InventoryBuild;

public class BeehivesInventory {
	
	static InventoryBuild menu;

	private static Language language = LemonBeehive.getInstance().get(Language.class);

	private BeehivesInventory() {};
	
	//Logger.info("&2[LemonDebug]", "&7Task-", "&f{miliseconds=" + System.currentTimeMillis() + "}");	
	
	public static InventoryBuild setupMenu(BeehiveObject beehive) {
		menu  = LemonBeehive.getBuilders().getInventoryBuilder().register("beehive-menu")
											   .applyByConfiguration(language.get("gui.beehive-menu"))
											   .setPlaceholder(beehive.getPlaceholder());

		menu.setClickEvent(listener -> {
			listener.setLocked(true);
			if (listener.isKeyed("friends")) {
				new FriendInventory(LemonBeehive.getInstance()).open(listener.getPlayer(), beehive);
			}else if (listener.isKeyed("storage-link")) {
				new PlayerInputChest(listener.getPlayer(), beehive, true);
				listener.close();
			}else if (listener.isKeyed("upgrades")) {
				listener.openChild("category-beehive", listener.getPlayer());
			}
		});
		menu.addChildren(setupUpgrade(beehive, "category-beehive", language.get("gui.upgrade-category.beehive"), beehive.getPlaceholder()));
		menu.addChildren(setupUpgrade(beehive, "category-bees", language.get("gui.upgrade-category.bees"), beehive.getPlaceholder()));
		return menu;
	}
	
	private static InventoryBuild setupUpgrade(BeehiveObject beehive, String key, ConfigurationBuild configuration, BeehiveObjectPlaceholder placeholder) {
		InventoryBuild build = LemonBeehive.getBuilders().getInventoryBuilder().register(key);
		build.applyByConfiguration(configuration)
			 .setPlaceholder(placeholder)
			 .setClickEvent(listener -> {
				 listener.setLocked(true);
				 if (listener.getBuild().getParent() != null && listener.getKeyed() != null) {
					 InventoryBuild parent = listener.getBuild().getParent();
					 if (listener.getKeyed().startsWith("category-")) {
						 if (parent.containsChild(listener.getKeyed())) {
							 parent.openChild(listener.getKeyed(), listener.getPlayer());
						 }
					 }else if (listener.getKeyed().endsWith("_upgrade")) {
						 UpgradeType type = UpgradeType.valueOf(listener.getKeyed().replace("_upgrade", ""));
						 if (type != null) {
							if (BeehiveUpgrade.upgrade(beehive, (Player) listener.getPlayer(), type)) {
								BeehiveUpdate.upgrade(beehive, type);
							}
						 }
					 }else if (listener.getKeyed().equals("back-menu")) {
						 listener.openParent(listener.getPlayer());
					 }
					}
			 });
		
		return build;
	}
	
	public static final void update() {
		menu.update(true, false);
	}
}