package nade.lemon.beehive.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Sets;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuilder;
import nade.lemon.utils.parse.DataTypes;

public class PropertiesBuild {
    private final Properties properties;

    private PropertiesBuild(Properties properties) {
        this.properties = properties;
    }

    /**
     * sets the value of a property with the given key to the given object. The object is converted to a String and stored as the property value.
     * @param key 
     * @param object
     */
    public void set(String key, Object object) {
        if (object instanceof Boolean) properties.setProperty(key, object.toString().toLowerCase());
        else if (object instanceof Number) properties.setProperty(key, object.toString().replace(".", ",") + "n");
        else properties.setProperty(key, object.toString());
    }

    /**
     * returns the value of the property with the given key as an Object.
     * @param key
     * @return the value of the property with the given key as an Object.
     */
    public Object get(String key) {
        return DataTypes.parse(key);
    }

    public <E> E get(String key, Class<E> clazz) {
        return this.getOrDefault(key, null, clazz);
    }

    /**
     * returns the value of the property with the given key, converted to the specified class type. If the property does not exist, returns the given default value.
     * @param <E> specified type
     * @param key the given key
     * @param def the default value
     * @param clazz the specified class type
     * @return the value of the property with the given key
     */
    public <E> E getOrDefault(String key, E def, Class<E> clazz) {
        return BeehiveConfigBuilder.getOrDefault(this.get(key), def, clazz);
    }

    /**
     * returns a Set of all the keys in the properties file.
     * @return a Set of all the keys in the properties file
     */
    public Set<String> getKeys() {
        Set<String> result = Sets.newHashSet();
        for (Object object : this.properties.keySet()) {
            result.add(object.toString());
        }
        return result;
    }

    /**
     * creates a new instance of PropertiesBuild by loading the properties from the file at the given path. Returns null if there is an IO exception.
     * @param path the file path
     * @return a new PropertiesBuild, null if there is an IO exception 
     */
    public static PropertiesBuild build(String path) {
        try {
            Properties properties = new Properties();
            InputStream input = LemonBeehive.getInstance().getResource(path);
            properties.load(input);
            return new PropertiesBuild(properties);
        }catch (IOException e) {
            return null;
        }
    }
}