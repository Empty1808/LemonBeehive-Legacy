package nade.lemon.beehive.objects.updates;

import java.util.List;

import org.bukkit.block.Beehive;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.FeaturesEnable;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.bees.Bees;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.library.hologram.Hologram;
import nade.lemon.utils.string.Color;

public class BeehiveUpdate {
    public static void normal(BeehiveObject beehive) {
        BeehiveUpdate.hologram(beehive);
        beehive.getMenu().update();

        if (beehive.isMaxHoney() && beehive.getDataBeehive().getHoneyLevel() != 5) {
			org.bukkit.block.data.type.Beehive data = beehive.getDataBeehive();
			data.setHoneyLevel(5);
			Beehive hive = beehive.getBeehive();
			hive.setBlockData(data);
			hive.update();
		}
    }

    public static void upgrade(BeehiveObject beehive, UpgradeType type) {
		if (type.is("BEEHIVE_CAPACITY")) {
			Bees hiveBees = beehive.getHiveBees();
			if (!hiveBees.isEnters()) {
				hiveBees.setMaxBees(1);
			}
		}
		BeehiveUpdate.hologram(beehive);
		BeehiveUpdate.normal(beehive);
	}

	public static void upgradeObjects() {
		for (BeehiveObject object : Database.getBeehives()) {
			/**
			CompoundTag upgrades = object.getUpgrades();
			for (String key : upgrades.getKeys()) {
				UpgradeType types = UpgradeType.valueOfs(key);
				if (!Objects.isNull(types)) {
					upgrades.setInt(key, types.getUpgrade(upgrades.getInt(key)).getLevel());
				}
			}
			 */
			BeehiveUpdate.hologram(object);
		}
	}

    public static void hologram(BeehiveObject beehive) {
		ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");
		if (FeaturesEnable.DISPLAY_HOLOGRAM.isEnable()) {
			List<String> hologramTexts = language.getList("hologram.beehive", String.class);
			for (int i = 0; i < hologramTexts.size(); i++) {
				String str = beehive.getPlaceholder().replace(hologramTexts.get(i));
				Hologram hologram = beehive.getHolograms().get(i);
				hologram.setText(Color.hex(str));
			}
		}
	}
}
