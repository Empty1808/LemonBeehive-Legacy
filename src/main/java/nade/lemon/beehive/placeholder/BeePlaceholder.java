package nade.lemon.beehive.placeholder;

import java.text.DecimalFormat;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Bee;

import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.tag.CompoundTag;

public class BeePlaceholder extends DefaultPlaceholder{
    
    private BeehiveObject beehives;
    private Bee entity;
    private CompoundTag tags;

    private DecimalFormat format = new DecimalFormat("0.#");

    public BeePlaceholder(BeehiveObject beehives, Bee entity, CompoundTag tags) {
        this.beehives = beehives;
        this.entity = entity;
        this.tags = tags;
    }

    @Override
    public String place(String request) {
        this.setReplaces("%bee_displayname%", entity.getName());
        this.setReplaces("%bee_owner%", beehives.getOwner().getName());
        this.setReplaces("%health%", format.format(entity.getHealth()));
        this.setReplaces("%max_health%", format.format(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        this.setReplaces("%attack_damage%", format.format(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue()));

        int maxStinger = beehives.getUpgrade(UpgradeType.valueOf("BEES_STINGER")).getValue().intValue();

        this.setReplaces("%stinger%", format.format(maxStinger - tags.getInt("stung")));
        this.setReplaces("%max_stinger%", format.format(maxStinger));

        return super.place(replaces(request));
    }
}