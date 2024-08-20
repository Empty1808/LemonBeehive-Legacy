package nade.lemon.beehive.configuration.build;

import java.util.Objects;

public class BeehiveConfigBuilder {
    
    public static <E> E getOrDefault(Object object, E def, Class<E> clazz) {
        if (Objects.isNull(object)) return def;
        if (!clazz.isInstance(object)) return def;
        return clazz.cast(object);
    }

}
