package nade.lemon.beehive.upgrades;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.configuration.simple.ConfigurationSectionBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.objects.MObjects;

public class Upgrade implements Comparable<Upgrade>{
    private MObjects properties = MObjects.create();

    public Upgrade(int level, UpgradeType type) {
        this.properties.set("level", level);
        this.properties.set("type", type);
        this.properties.set("requirements", Lists.newArrayList());
    }

    public void set(String key, Object value) {
        this.properties.set(key, value);
    }

    private <E> E get(String key, Class<E> clazz) {
        return properties.get(key, clazz);
    }

    public <E> E getOrDefault(String key, E def, Class<E> clazz) {
        return properties.getOrDefault(key, def, clazz);
    }

    private <E> List<E> getList(String key, Class<E> clazz) {
        return properties.getList(key, clazz);
    }

    public Number getValue() {
        return this.getOrDefault("value", 0, Number.class);
    }

    public int getLevel() {
        return this.get("level", Integer.class);
    }

    public UpgradeType getType() {
        return this.get("type", UpgradeType.class);
    }

    public Map<String, Requirement> getRequirements() {
        Map<String, Requirement> result = Maps.newLinkedHashMap();

        for (Requirement method : this.getList("requirements", Requirement.class)) {
            result.put(method.getId(), method);
        }

        return result;
    }

    public Requirement addRequirement(String type) {
        Requirement result = new Requirement("method-" + new Random().nextInt(1000), "exp");
        List<Requirement> requirements = this.getList("requirements", Requirement.class);
        requirements.add(result);
        this.set("requirements", requirements);
        return result;
    }

    public void removeRequirement(Requirement method) {
        List<Requirement> requirements = this.getList("requirements", Requirement.class);
        requirements.remove(method);
        this.set("requirements", requirements);
    }

    public boolean isValid() {
        if (this.getOrDefault("value", 0, Number.class).doubleValue() <= 0) return false;
        if (Objects.isNull(this.get("type", Object.class))) return false;
        if (this.getRequirements().isEmpty() && !LemonBeehive.getInstance().get(UpgradeSystem.class).isLast(this)) return false;
        return true;
    }

    public void update() {
        ConfigurationBuild config = this.getType().getConfig();
        ConfigurationSectionBuild upgrades = !Objects.isNull(config.getSection("upgrades")) ? config.getSection("upgrades") : config.createSection("upgrades");
        ConfigurationSectionBuild section = upgrades.contains("i-" + this.getLevel()) ? upgrades.getSection("i-" + this.getLevel()) : upgrades.createSection("i-" + this.getLevel());

        if (Objects.isNull(section)) return;

        section.set("level", this.getLevel());
        if (properties.has("value")) section.set("value", this.get("value", Number.class));
        if (properties.has("requirements")) {
            section.set("requirements", null);
            List<Requirement> requirements = this.getList("requirements", Requirement.class);
            requirements.forEach((method) -> {
                ConfigurationSectionBuild clone = section.createSection("requirements." + method.getId());

                clone.set("type", method.getType());
                if (method.hasValue()) clone.set("value", method.getValue());
            });
        }
    }

    public void save() {
        this.getType().getConfig().save();
    }

    public void remove() {
        ConfigurationBuild config = this.getType().getConfig();
        config.set("upgrades.i-" + this.getLevel(), null);
    }

    @Override
    public int compareTo(Upgrade object) {
        return Integer.compare(this.get("level", Integer.class), object.get("level", Integer.class));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Upgrade{");

        builder.append("level=" + this.get("level", Number.class));

        if (properties.has("value")) builder.append(",value=" + this.get("value", Number.class));
        if (properties.has("requirements")) builder.append(",requirements" + this.getList("requirements", Requirement.class));

        return builder.append("}").toString();
    }

    public static Upgrade applyBySection(ConfigurationSectionBuild section, UpgradeType type) {
        Upgrade upgrade = new Upgrade(section.get("level", Integer.class), type);
        
        if (section.contains("value")) upgrade.set("value", section.get("value"));
        if (section.contains("requirements")) upgrade.set("requirements", Requirement.applyBySection(section.getSection("requirements")));

        return upgrade;
    }
}