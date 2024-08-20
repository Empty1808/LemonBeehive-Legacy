package nade.lemon.beehive.utils;

import java.util.Objects;
import java.util.UUID;

public class UUIDs {
    public static boolean isUUID(String arg) {
        if (Objects.isNull(arg)) return false;
        try {
            UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public static UUID fromString(String string) {
        return UUID.fromString(string);
    }
}
