package nade.lemon.beehive.objects.bees;

import java.text.DecimalFormat;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.HumanEntity;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.tag.CompoundTag;

public class BeeStatus {
	private static final Language language = LemonBeehive.getInstance().get(Language.class);

    public static void open(HumanEntity human, BeehiveObject beehives, Bee entity, CompoundTag tags) {
        
        InventoryBuild build = LemonBeehive.getBuilders().getInventoryBuilder().register("bee-status")
                                               .applyByConfiguration(language.get("gui\\bee-status"))
                                               .setPlaceholder(createPlaceholder(beehives, entity, tags));
        
        build.setClickEvent(listener -> {
            listener.setLocked(true);
            if (listener.isKeyed("kill")) {
                entity.setHealth(0);
                build.closeInventory();
            }
        });
        
        build.setClosedEvent(listener -> {
            entity.setAI(true);
        });

        build.setOpenedEvent(listener -> {
            entity.setAI(false);
        });
        
        human.openInventory(build.getInventory());
    }

    private static PlaceholderBuild createPlaceholder(BeehiveObject beehives, Bee entity, CompoundTag tags) {
        PlaceholderBuild placeholder = PlaceholderBuild.build();

        DecimalFormat format = new DecimalFormat("0.#");

        placeholder.set("{bee-displayname}", entity.getName());
        placeholder.set("{bee-owner}", beehives.getOwner().getName());
        placeholder.set("{health}", format.format(entity.getHealth()));
        placeholder.set("{max-health}", format.format(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        placeholder.set("{attack-damage}", format.format(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue()));

        int maxStinger = beehives.getUpgrade(UpgradeType.valueOf("BEES_STINGER")).getValue().intValue();

        placeholder.set("{stinger}", format.format(maxStinger - tags.getInt("stung")));
        placeholder.set("{max-stinger}", format.format(maxStinger));

        return placeholder;
    }
}