package nade.lemon.beehive.handlers;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.StaticField;
import nade.lemon.beehive.StaticPlaceholder;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.player.PlayerInputChest;
import nade.lemon.beehive.utils.string.AdvancedSendMessage;
import nade.lemon.utils.bukkit.Locations;

public class PlayerInputHandlers {
	private static final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");

	public static void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (e.isSneaking() && PlayerInputChest.inputMap.containsKey(e.getPlayer())) {
			AdvancedSendMessage.send(e.getPlayer(), language.get("storage-link.cancel", String.class));
			PlayerInputChest.inputMap.remove(e.getPlayer());
		}
	}

	public static void onChestInteractEvent(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (PlayerInputChest.inputMap.containsKey(player)) {
			Chest chest = (Chest) e.getClickedBlock().getState();
			PlayerInputChest inputChest = PlayerInputChest.inputMap.get(player);
			BeehiveObject beehive = inputChest.getBeehiveObject();
			if (!Locations.distance(beehive.getLocation(), chest.getLocation(), StaticField.LINKED_DISTANCE)) {
				PlayerInputChest.inputMap.remove(player);
				String reason = language.get("storage-link.fail-reason.out-area", String.class).replace("{area}", StaticPlaceholder.area());
				String message = language.get("storage-link.fail", String.class).replace("{reason}", reason);
				AdvancedSendMessage.send(player, message);
				e.setCancelled(true);
				return;
			}
			if (beehive.getStorageLink().containsKey(chest.getLocation())) {
				PlayerInputChest.inputMap.remove(player);
				String reason = language.get("storage-link.fail-reason.linked", String.class);
				String message = language.get("storage-link.fail", String.class).replace("{reason}", reason);
				AdvancedSendMessage.send(player, message);
				e.setCancelled(true);
				return;
			}
			inputChest.getBeehiveObject().getStorageLink().add(chest);
			BeehiveUpdate.normal(inputChest.getBeehiveObject());
			PlayerInputChest.inputMap.remove(player);
			int x = chest.getX();
			int y = chest.getY();
			int z = chest.getZ();
			String message =  language.get("storage-link.completed", String.class)
					.replace("{location}", StaticPlaceholder.xyzStyle(x, y, z));
			AdvancedSendMessage.send(player, message);
			e.setCancelled(true);
		}
	}
}