package nade.lemon.beehive.utils.string;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nade.lemon.beehive.FeaturesEnable;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.utils.Logger;
import nade.lemon.utils.string.Color;

public class AdvancedSendMessage {

	private static Logger logger = LemonBeehive.getInstance().get(Logger.class);

	private AdvancedSendMessage() {};
	
	public static void send(CommandSender sender, String string) {
		if (string.equals("none")) return;
		else if (string.startsWith("[MESSAGE]")) message(sender, true, string.replace("[MESSAGE]", ""));
		else if (string.startsWith("[TITLE]")) title(sender, string.replace("[TITLE]", ""));
		else message(sender, true, string);
	}

	public static void message(CommandSender sender, boolean prefix, String string) {
		if (string.equals("none")) return;
		if (!FeaturesEnable.MESSAGE.isEnable()) return;
		if (prefix) logger.sendInfo(sender, string);
		else logger.sendNormal(sender, string);
	}
	
	private static void title(CommandSender sender, String string) {
		if (string.equals("none")) return;
		if (FeaturesEnable.TITLE.isEnable() && sender instanceof Player) {
			Player player = (Player) sender;
			String[] titles = string.split("\n");
			int fadeIn = getConfig().getOrDefaultConfig("general.notification.title.fade-in", Integer.class);
			int stay = getConfig().getOrDefaultConfig("general.notification.title.stay", Integer.class);
			int fadeOut = getConfig().getOrDefaultConfig("general.notification.title.fade-out", Integer.class);
			if (titles.length > 1) {
				player.sendTitle(Color.hex(titles[0]), Color.hex(titles[1]), fadeIn, stay, fadeOut);
			}else if (titles.length > 0) {
				player.sendTitle(Color.hex(titles[0]), "", fadeIn, stay, fadeOut);
			}
		}
 	}
	
	private static BeehiveConfigBuild getConfig() {
		return BeehiveYamlConfig.getConfig();
	}
}
