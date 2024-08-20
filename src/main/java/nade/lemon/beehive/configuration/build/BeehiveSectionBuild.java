package nade.lemon.beehive.configuration.build;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nade.empty.configuration.simple.ConfigurationSectionBuild;
import nade.lemon.configuration.ConfigurationL;

public class BeehiveSectionBuild implements ConfigurationSectionBuild{
    
    protected BeehiveConfigBuild root;
    protected ConfigurationSection configuration;

    protected BeehiveSectionBuild() {
        if (!(this instanceof BeehiveConfigBuild)) {
            throw new IllegalStateException("Cannot construct a root SectionBuild when not a ConfigurationBuild");
        }else {
            this.root = (BeehiveConfigBuild) this;
        }
    }

    private BeehiveSectionBuild(BeehiveConfigBuild root, ConfigurationSection configuration) {
        this.root = root;
        this.configuration = configuration;
    }

    public void set(String path, Object object) {
        configuration.set(path, object);
    }

    public void setIfNull(String path, Object object) {
        ConfigurationL defaut = this.root.getDefault();
        if (Objects.isNull(defaut.get(path))) {
            this.root.getDefault().set(path, object);
        }
        if (Objects.isNull(configuration.get(path))) {
            this.set(path, object);
        }
    }

    public Object get(String path) {
        return this.configuration.get(path);
    }

    public <E> E get(String path, Class<E> clazz) {
        return this.getOrDefault(path, null, clazz);
    }

    public BeehiveSectionBuild getSection(String path) {
        ConfigurationSection section = this.get(path, ConfigurationSection.class);
        if (!Objects.isNull(section)) return new BeehiveSectionBuild(this.root, section);
        return null;
    }

    public Set<BeehiveSectionBuild> getSections(boolean deep) {
        Set<BeehiveSectionBuild> results = Sets.newHashSet();

        for (String key : this.getKeys(deep)) {
            ConfigurationSection section = this.get(key, ConfigurationSection.class);
            if (!Objects.isNull(section)) {
                results.add(new BeehiveSectionBuild(this.root, section));
            }
        }

        return results;
    }

    public Object getOrDefault(String path, Object def) {
        return this.configuration.get(path, def);
    }

    public <E> E getOrDefault(String path, E def, Class<E> clazz) {
        Object object = this.get(path);
        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        return def;
    }

    public <E> E getDefaultConfig(String path, Class<E> clazz) {
        Object object = this.root.getDefault().get(path);
        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        return null;
    }

    public <E> E getOrDefaultConfig(String path, Class<E> clazz) {
        E object = this.get(path, clazz);
        if (Objects.isNull(object)) {
            return this.getDefaultConfig(path, clazz);
        }
        return object;
    }

    public <E> List<E> getList(String path, Class<E> clazz) {
        List<?> list = configuration.getList(path);
        if (Objects.isNull(list)) return Lists.newArrayList();
        List<E> results = Lists.newArrayList();
        
        for (Object object : list) {
            if (Objects.isNull(object) || !clazz.isInstance(object)) continue;
            results.add(clazz.cast(object));
        }
        return results;
    }

    public boolean contains(String path) {
        return this.configuration.contains(path);
    }

    public <E> boolean contains(String path, Class<E> clazz) {
        return !Objects.isNull(this.get(path, clazz));
    }

    public boolean isSection(String path) {
        return this.configuration.isConfigurationSection(path);
    }

    public Set<String> getKeys() {
        return this.getKeys(false);
    }

    public Set<String> getKeys(boolean deep) {
        return this.configuration.getKeys(deep);
    }

    public void clearKeys() {
        for (String key : this.getKeys(false)) {
            this.set(key, null);
        }
    }

    public BeehiveSectionBuild createSection(String path) {
        return new BeehiveSectionBuild(this.root, this.configuration.createSection(path));
    }

    public void copyOldKey(String newKey, String oldKey, Object def) {
        if (!Objects.isNull(this.get(newKey))) return;
        Object oldObject = this.get(oldKey);
        this.set(oldKey, null);
        this.set(newKey, Objects.isNull(oldObject) ? def : oldObject);
    }

    @Override
    public String getCurrentPath() {
        return configuration.getCurrentPath();
    }

    @Override
    public BeehiveSectionBuild getParent() {
        return new BeehiveSectionBuild(root, configuration.getParent());
    }

    @Override
    public String getName() {
        return configuration.getName();
    }
}
