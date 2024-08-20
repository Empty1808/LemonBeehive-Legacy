package nade.lemon.beehive.upgrades;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Material;

import com.google.common.collect.Sets;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.lemon.objects.MObjects;
import nade.lemon.utils.bukkit.Materials;

public class UpgradeType {
    private static Set<UpgradeType> availiable = Sets.newLinkedHashSet();

    private MObjects properties = MObjects.create();

    private UpgradeType(String type, String category, String icon) {
        this.properties.set("type", type);
        this.properties.set("category", category);
        this.properties.set("icon", Materials.getMaterialOrDefault(icon, Material.STONE));

        availiable.add(this);
    }

    void set(String key, Object object) {
        this.properties.set(key, object);
    }

    public String getType() {
        return properties.get("type", String.class).toLowerCase();
    }

    public String getCategory() {
        return properties.get("category", String.class).toLowerCase();
    }

    public Material getIcon() {
        return properties.get("icon", Material.class);
    }

    public ConfigurationBuild getConfig() {
        return this.properties.getOrDefault("config", null, ConfigurationBuild.class);
    }

    public String getName() {
        String result = this.getType().replace("-", " ");
        String[] arg0 = this.getType().split("-");

        for (int i = 0; i < arg0.length; i++) {
            String after = arg0[i];
            String before = arg0[i].substring(0, 1).toUpperCase() + arg0[i].substring(1);
            result = result.replace(after, before);
        }                               

        return result;
    }

    public static UpgradeType valueOf(String types) {
        for (UpgradeType type : availiable) {
            if (type.getType().equalsIgnoreCase(types.replace("_", "-"))) return type;
        }
        return null;
    }

    public boolean is(String type) {
        return this.getType().equalsIgnoreCase(type.replace("_", "-"));
    }

    public static boolean contains(String type) {
        if (Objects.isNull(type)) return false;
        for (UpgradeType element : availiable) {
            if (element.getType().equals(type.toLowerCase())) return true;
        }
        return false;
    }

    public static Collection<UpgradeType> values() {
        return Sets.newLinkedHashSet(availiable);
    }

    public static UpgradeType create(String type, String category, String icon) {
        if (contains(type)) {
            return valueOf(type);
        }
        return new UpgradeType(type, category, icon);
    }

    @Override
    public String toString() {
        return "UpgradeType{type=" + this.getType() + "}";
    }
}
