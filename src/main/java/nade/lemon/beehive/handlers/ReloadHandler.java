package nade.lemon.beehive.handlers;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.handlers.players.BlockTargetListener;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.inventory.BeehivesInventory;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.builders.Builders;

public class ReloadHandler {
    
    private EmptyPlugin plugin;

    private UpgradeSystem system;

    public ReloadHandler(EmptyPlugin plugin) {
        this.plugin = plugin;
        system = plugin.get(UpgradeSystem.class);
    }

    public void all() {
        this.yaml();
        this.language();
        this.closeInventorys();
        this.beehives();
        this.upgrade();
        this.inventorys();
    }

    public void beehives() {
        for (BeehiveObject object : Database.getBeehives()) {
            /**
            CompoundTag upgrades = object.getUpgrades();
            for (String key : upgrades.getKeys()) {
                UpgradeTypes types = UpgradeTypes.getById(key);
                UpgradeType type = UpgradeType.valueOf(key);
                UpgradeSystem system = plugin.get(UpgradeSystem.class);
                if (!Objects.isNull(types)) {
                    upgrades.setInt(key, types.getUpgrade(upgrades.getInt(key)).getLevel());
                }
            }
             */
            BeehiveUpdate.hologram(object);
            BeehivesInventory.update();
        }
    }

    public void closeInventorys() {
        plugin.get(Builders.class).getInventoryBuilder().close();
    }

    public void yaml() {
        for (BeehiveConfigBuild build : BeehiveYamlConfig.getAvailable()) {
            build.reload();
        }
        BlockTargetListener.onReloadConfig(plugin);
    }

    public void language() {
        plugin.get(Language.class).reload();
    }

    public void upgrade() {
        system.reload();
    }

    public void inventorys() {
        plugin.get(Builders.class).getInventoryBuilder().update();
    }
}