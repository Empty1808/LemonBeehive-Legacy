package nade.lemon.beehive.upgrades;

import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import nade.empty.configuration.simple.ConfigurationSectionBuild;
import nade.lemon.objects.MObjects;

public class Requirement {
    private MObjects properties = MObjects.create();

    private Requirement(String id, Type type) {
        this.properties.set("id", id);
        this.properties.set("type", type);
    }

    public Requirement(String id, String type) {
        this.properties.set("id", id);
        this.properties.set("type", Type.valueOf(type.toUpperCase()));
    }

    public void setValue(Object object) {
        properties.set("value", object);
    }

    public Object getValue() {
        return properties.get("value");
    }

    public <E> E getValue(Class<E> clazz) {
        return properties.get("value", clazz);
    }

    public <E> E getValue(E def, Class<E> clazz) {
        return properties.getOrDefault("value", def, clazz);
    }

    public boolean hasValue() {
        return properties.has("value");
    }

    public String getId() {
        return this.properties.get("id", String.class);
    }

    public void setType(String type) {
        this.properties.set("type", Type.valueOf(type.toUpperCase()));
    }

    public String getType() {
        return this.properties.get("type", Type.class).name();
    }

    public Material getIcon() {
        return this.properties.get("type", Type.class).getIcon();
    }

    public boolean is(String type) {
        return this.getType().equalsIgnoreCase(type);
    }

    public boolean isValid() {
        if (Objects.isNull(this.getValue())) return false;
        return true;
    }

    public static List<Requirement> applyBySection(ConfigurationSectionBuild parent) {
        List<Requirement> result = Lists.newLinkedList();

        for (String key : parent.getKeys()) {
            ConfigurationSectionBuild section = parent.getSection(key);

            Requirement requirement = new Requirement(section.getName(), Type.valueOf(section.get("type", String.class).toUpperCase()));
            if (section.contains("value")) requirement.setValue(section.get("value", requirement.properties.get("type", Type.class).getType()));

            result.add(requirement);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Upgraderequirement{");

        if (!Objects.isNull(this.getType())) builder.append("type=" + this.getType());
        if (!Objects.isNull(this.properties.get("value"))) builder.append(",value=" + this.properties.get("value"));
        
        return builder.append("}").toString();
    }

    public static Material getIconByType(String type) {
        return Type.valueOf(type.toUpperCase()).getIcon();
    }
}

enum Type {
    MONEY(Material.GOLD_INGOT, Number.class),
    EXP(Material.EXPERIENCE_BOTTLE, Number.class),
    ITEM(Material.LAVA_BUCKET, ItemStack.class);

    private Material icon;
    private Class<?> clazz;

    Type(Material icon, Class<?> clazz) {
        this.icon = icon;
        this.clazz = clazz;
    }

    public Class<?> getType() {
        return this.clazz;
    }

    public Material getIcon() {
        return icon;
    }
}