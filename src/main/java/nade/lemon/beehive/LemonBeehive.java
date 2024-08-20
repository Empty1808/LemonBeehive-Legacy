package nade.lemon.beehive;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.commands.Commands;
import nade.lemon.beehive.configuration.YamlChecker;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.data.HeadDatabase;
import nade.lemon.beehive.data.LimitHive;
import nade.lemon.beehive.data.SoftDepends;
import nade.lemon.beehive.data.store.ServiceDataStore;
import nade.lemon.beehive.data.store.SqlStore;
import nade.lemon.beehive.data.store.YamlStore;
import nade.lemon.beehive.features.AdminInventory;
import nade.lemon.beehive.features.harvest.HarvestSystem;
import nade.lemon.beehive.features.manager.ManageSystem;
import nade.lemon.beehive.features.recipe.RecipeSystem;
import nade.lemon.beehive.handlers.players.BlockTargetListener;

import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

import nade.lemon.beehive.listeners.Listeners;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.utils.Logger;
import nade.lemon.utils.bukkit.BukkitKeyed;
import nade.lemon.utils.collect.ItemStackL;
import nade.lemon.beehive.utils.SpigotMC;
import nade.lemon.beehive.utils.VersionAvailable;
import nade.lemon.builders.Builders;
import nade.lemon.configuration.ConfigurationL;
import nade.lemon.configuration.SimpleConfig;
import nade.lemon.head.CustomHead;

public class LemonBeehive extends EmptyPlugin{

	private static EmptyPlugin instance;
	
	public LemonBeehive() {
		LemonBeehive.instance = this;
		this.firstEnable();
		this.registerHeadObject();
	}
	
	@Override
	public void onLoad() {
		this.set(new Logger(this.get(Language.class).get("language").get("prefix", String.class)));
		this.set(new BukkitKeyed(instance));
		this.set(new ItemStackL(instance));
		this.set(new VersionAvailable(15, 16, 17, 18, 19, 20));
	}

	@Override
	public void onEnable() {
		Logger logger = this.get(Logger.class);
		try {
			if (!get(VersionAvailable.class).isAvailable()) {
				logger.info("&cversion unsupport!");
				this.disable();
				return;
			}
			this.set(new UpgradeSystem(this));
			localLoad();
			loadDatabase();
			Metrics();
			this.set(new RecipeSystem(this));
			this.set(new HarvestSystem(this));
			SpigotMC.checkUpdate();
			this.registerInventory();
			logger.info("&ahas been enabled!");
		} catch (Exception e) {
			Exception(e);
		}
	}

	private void loadDatabase() {
		Logger logger = this.get(Logger.class);
		logger.info("&7loading plugin beehives...");
		int count = this.get(ServiceDataStore.class).loading();
		if (count > 0) {
			logger.info("&7loaded {beehive_count} plugin beehive".replace("{beehive_count}", String.valueOf(count)));
		}
	}
	
	@Override
	public void onDisable() {
		Logger logger = this.get(Logger.class);
		try {
			if (!get(VersionAvailable.class).isAvailable()) {
				return;
			}
			Database.getBeehives().forEach((beehive) -> beehive.save());

			logger.info("&7saving plugin beehives...");
			int count = this.get(ServiceDataStore.class).onSave();
			logger.info("&7saved {count} plugin beehive".replace("{count}", String.valueOf(count)));

			this.get(RecipeSystem.class).getEvent().onDisable();;

			logger.info("&chas been disabled!");
		} catch (Exception e) {
			Exception(e);
		}
	}

	private void firstEnable() {
		this.set(new SimpleConfig(instance));
		BeehiveYamlConfig.onEnable();
		this.set(new Language(this));
		YamlChecker.upgrades();
		StaticField.onEnable(this);
		StaticPlaceholder.onEnable();
		FeaturesEnable.onEnable();
	}

	private void registerHeadObject() {
		Configuration config = ConfigurationL.loadConfiguration(new InputStreamReader(this.getResource("HeadDatabase.yml")));
		for (String key : config.getKeys(false)) {
			CustomHead.register(key.replace("_", "-").toLowerCase(), config.getString(key));
		}
	}

	private void registerInventory() {
		this.set(new AdminInventory(this));
		this.set(new ManageSystem(this));
	}
	
	public void Exception(Exception e) {
		this.get(Logger.class).normal("&c" + e.getClass().getSimpleName() + "&7 - &f" + e.getMessage());
		this.get(Logger.class).normal("&cClass exception occurs");
		this.get(Logger.class).blank();
		this.get(Logger.class).normal("&7 [&f%plugin_name%&7] class:".replace("%plugin_name%", getName()));
		for (StackTraceElement stack : e.getStackTrace()) {
			if (stack.getClassName().contains("nade")) {
				this.get(Logger.class).normal("&7  [&e" + stack.getClassName() + ".java&7:[&c" + stack.getLineNumber() + "&7)]");
			}
		}
		this.get(Logger.class).blank();
		this.get(Logger.class).normal("&7 [&fBukkit&7] class:");
		for (StackTraceElement stack : e.getStackTrace()) {
			if (stack.getClassName().contains("org.bukkit")) {
				this.get(Logger.class).normal("&7  [&e" + stack.getClassName() + ".java&7:(&c" + stack.getLineNumber() + "&7)]");
			}
		}
		this.get(Logger.class).blank();
		this.get(Logger.class).normal("&7 [&fNetMinecraft&7] class");
		for (StackTraceElement stack : e.getStackTrace()) {
			if (stack.getClassName().contains("net.minecraft")) {
				this.get(Logger.class).normal("&7  [&e" + stack.getClassName() + ".java&7:(&c" + stack.getLineNumber() + "&7)]");
			}
		}
		this.get(Logger.class).blank();
		getPluginLoader().disablePlugin(instance);
	}
	
	private void Metrics() {
		try {
			int plugin_id = 12428;
			new Metrics(this, plugin_id);
		} catch (Exception ignored) {}
	}
	
	private void localLoad() {
		SoftDepends.onEnable(); //Prioritize = HIGHT
		this.set(Builders.class, new Builders(this));
		getDatabaseStorage();
		LimitHive.onEnable();
		HeadDatabase.onEnable();
		BlockTargetListener.onPluginEnable(this);
		this.get(Builders.class).register();

		onCommands();
		onListeners();
	}
	
	private void getDatabaseStorage() {
		Logger logger = this.get(Logger.class);
		logger.info("&7connecting to database storage...");
		if (FeaturesEnable.MYSQL.isEnable()) {
			SqlStore sql = new SqlStore();
			if (sql.connect()) {
				this.set(ServiceDataStore.class, sql);
				logger.info("&7connected to &bMySQL da.");
			}else {
				this.set(ServiceDataStore.class, new YamlStore());
				this.get(Logger.class).info("&econnection to &bMySQL &efailed, &econnecting to &aYaml &7database.");
				this.get(Logger.class).normal("&7[&cReason&7] &f" + sql.getMessage());
				sql.getFailException().printStackTrace();
			}
		}else {
			this.set(ServiceDataStore.class, new YamlStore());
			this.get(Logger.class).info("&7connected &aYaml &7database.");
		}
	}
	
	private void onCommands() {
		new Commands(this);
	}
	
	private void onListeners() {
		new Listeners(this);
	}

	public static Builders getBuilders() {
		return LemonBeehive.getInstance().get(Builders.class);
	}

	public static EmptyPlugin getInstance() {
		return instance;
	}

	public void disable() {
		Bukkit.getPluginManager().disablePlugin(this);
	}
}