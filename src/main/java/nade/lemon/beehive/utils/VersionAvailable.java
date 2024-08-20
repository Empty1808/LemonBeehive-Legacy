package nade.lemon.beehive.utils;

import nade.lemon.utils.spigot.Version;

public class VersionAvailable {
    private int[] available;

    public VersionAvailable(int... available) {
        this.available = available;
    }

    public boolean isAvailable() {
        for (int version : available) {
            if (Version.version().contains("v1_" + version + "_R")) {
                return true;
            }
        }
        return false;
    }
}