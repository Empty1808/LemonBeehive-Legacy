package nade.lemon.beehive.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.utils.Logger;

public class SpigotMC {
	private static String RESOURCE_ID = "%%__RESOURCE__%%";
	private static String USER_ID = "%%__USER__%%";

	private static String spigotVersion;
	private static String localVersion;

	private static boolean hasNewVersion = false;

	private static Logger logger = LemonBeehive.getInstance().get(Logger.class);

    @SuppressWarnings("resource")
	public static void checkUpdate() {
		logger.info("&7checking new version on SpigotMC...");
    	try {
    		HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).openConnection();
    		int timed_out = 1250;
    		connection.setConnectTimeout(timed_out);
    		connection.setReadTimeout(timed_out);
    		localVersion = LemonBeehive.getInstance().getDescription().getVersion();
    		spigotVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
    		connection.disconnect();
			checkNewVersion();
			if (hasNewVersion) {
				logger.info("&7new version &2{spigot_version} &7has been found,".replace("{spigot_version}", spigotVersion));
				logger.info("&7download at &ehttps://www.spigotmc.org/resources/{resource_id}/".replace("{resource_id}", RESOURCE_ID));
			}else {
				logger.info("&7no new version found");
			}
    	} catch (Exception e) {
    		logger.warning("unable to connect to SpigotMC.org!");
    	}
    }

	private static void checkNewVersion() {
		if (spigotVersion == null) {
			return;
		}
		String[] spigot = spigotVersion.replace("-beta", "").replace(".", "//").split("//");
		String[] current = localVersion.replace("-beta", "").replace(".", "//").split("//");

		int spigot0 = Utilities.getInt(getElement(0, spigot));
		int spigot1 = Utilities.getInt(getElement(1, spigot));
		int spigot2 = Utilities.getInt(getElement(2, spigot));

		int current0 = Utilities.getInt(getElement(0, current));
		int current1 = Utilities.getInt(getElement(1, current));
		int current2 = Utilities.getInt(getElement(2, current));

		if (spigot0 < current0) return;
		if (spigot1 < current1) return;
		if (spigot2 < current2) return;
		if (spigot0 == current0 && spigot1 == current1 && spigot2 == current2) return;
		hasNewVersion = true;
	}

	public static boolean hasNewVersion() {
		return hasNewVersion;
	}
    
    public static String getSpigotVersion() {
    	return spigotVersion;
    }

	private static String getElement(int index, String[] arrays) {
		if (arrays.length > index) {
			return arrays[index];
		}
		return "0";
	}
    
    public static String getLocalVersion() {
    	return localVersion;
    }
    
    public static String getId() {
    	return RESOURCE_ID; 
    }

	public static String getUserId() {
		return USER_ID;
	}
}
