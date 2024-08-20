package nade.lemon.beehive.placeholder;

public class FeaturesPlaceholder {

    public static String setColor(boolean enable) {
        if (enable) return "&a" + enable;
        return "&c" + enable;
    }
}
