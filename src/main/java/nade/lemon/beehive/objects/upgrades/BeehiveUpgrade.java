package nade.lemon.beehive.objects.upgrades;

import java.util.Collection;

import org.bukkit.entity.Player;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.upgrades.Upgrade;
import nade.lemon.beehive.upgrades.Requirement;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.beehive.utils.string.AdvancedSendMessage;

public class BeehiveUpgrade {
	private static final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");
	private static final ConfigBuild message = LemonBeehive.getInstance().get(Language.class).get("message");
	private static UpgradeSystem system = LemonBeehive.getInstance().get(UpgradeSystem.class);

	public static boolean upgrade(BeehiveObject beehive, Player player, UpgradeType type) {
		Upgrade upgrade = beehive.getUpgrade(type);
		if (!system.hasHigher(upgrade) || !system.higher(upgrade).isValid()) {
			AdvancedSendMessage.message(player, true, message.get("system.cant-upgrade", String.class));
			return false;
		}	
		Collection<Requirement> objects = upgrade.getRequirements().values();
		if (!PaymentSystem.isEnough(player, objects)) return false;
		PaymentSystem.withdraw(player, objects);
		int nextLevel = system.higher(upgrade).getLevel();
		String types = language.get("parameter." + type.getType().toLowerCase(), String.class);
		beehive.setUpgrade(type, nextLevel);
		AdvancedSendMessage.message(player, true, message.get("system.success-upgrade", String.class).replace("{upgrade-type}", types) .replace("{level}", String.valueOf(nextLevel)));
		BeehiveUpdate.upgrade(beehive, type);
		return true;
	}
}