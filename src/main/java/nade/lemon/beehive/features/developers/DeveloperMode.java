package nade.lemon.beehive.features.developers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.configuration.SimpleConfig;
import nade.lemon.utils.string.Color;

public class DeveloperMode {
    public static void warring(HumanEntity sender, boolean console, String message) {
        sendMessage(console ? Bukkit.getConsoleSender() : sender, "&e[warning]", message);
    }

    public static void crash(HumanEntity sender, boolean console, String message) {
        sendMessage(console ? Bukkit.getConsoleSender() : sender, "&c[crash]", message);
    }

    public static void notify(String message) {
        sendMessage(Bukkit.getConsoleSender() , "&3[notify]", message);
    }

    public static void notify(HumanEntity sender, boolean console, String message) {
        sendMessage(console ? Bukkit.getConsoleSender() : sender, "&3[notify]", message);
    }

    private static void sendMessage(CommandSender sender, String prefix, String message) {
        if (isEnable()) {
            sender.sendMessage(Color.hex(prefix + "&r " + message));
        }
    }

    public static boolean isEnable() {
        Boolean enable = LemonBeehive.getInstance().get(SimpleConfig.class).getDefault("plugin.yml").getBoolean("developer-mode");
        return enable;
    }
}
