package nade.lemon.beehive;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

public class DeveloperDatabase {
    
    private Map<Class<?>, Object> database = Maps.newHashMap();

    public DeveloperDatabase() {}

    public void add(Object object) {
        if (Objects.isNull(object)) return;
        database.put(object.getClass(), object);
    }

    public <E> E get(Class<E> clazz) {
        return this.getOrDefault(clazz, null);
    }

    public <E> E getOrDefault(Class<E> clazz, E def) {
        if (!database.containsKey(clazz)) return def;
        Object object = database.get(clazz);
        if (Objects.isNull(object)) return def;
        if (!clazz.isInstance(object)) return def;
        return clazz.cast(object);
    }

}
