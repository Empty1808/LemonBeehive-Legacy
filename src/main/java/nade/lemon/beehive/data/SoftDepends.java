package nade.lemon.beehive.data;

import org.bukkit.plugin.RegisteredServiceProvider;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.utils.Logger;
import net.milkbowl.vault.economy.Economy;

public class SoftDepends {

	private static EmptyPlugin plugin = LemonBeehive.getInstance();
	private static Logger logger = plugin.get(Logger.class);

	private static Economy economy;
	private static boolean hasEconomy = false;
	
	public static void onEnable() {
		logger.info("&7checking soft depends...");
		economy();
		if (hasEconomy) {
			logger.info("&7Vault found.");
		}
	}
	
	public static boolean hasEconomy() {
		return hasEconomy;
	}

	private static void economy() {
		RegisteredServiceProvider<Economy> economy;
		try {
			economy = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		} catch (NoClassDefFoundError e) {
			return;
		}
		if (economy != null) {
			SoftDepends.economy = economy.getProvider();
		}
		hasEconomy = (SoftDepends.economy != null);
	}
	
	public static Economy getEconomy() {
		return economy;
	}
}
