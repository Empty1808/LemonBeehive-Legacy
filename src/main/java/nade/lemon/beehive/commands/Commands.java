package nade.lemon.beehive.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.StaticField;
import nade.lemon.beehive.commands.build.CommandBuild;
import nade.lemon.beehive.commands.build.Parameter;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.features.AdminInventory;
import nade.lemon.beehive.handlers.ReloadHandler;
import nade.lemon.beehive.utils.Utilities;
import nade.lemon.utils.Collections;
import nade.lemon.utils.Logger;
import nade.lemon.utils.spigot.Players;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Commands {
	private final EmptyPlugin plugin;
	private final Logger logger;

	private final ReloadHandler reload;

	CommandBuild build = CommandBuild.build("lemonbeehive");

	private final ConfigBuild message;

	public Commands(EmptyPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.get(Logger.class);
		this.reload = new ReloadHandler(plugin);

		this.message = plugin.get(Language.class).get("message");

		this.onSetup();
	}

	private void onSetup() {
		build.setCommandExecutor((sender, label, parameters) -> this.onInfoCMD(sender, label));
		build.setPermission("LemonBeehive.commands");

		Parameter version = build.addParameter("version");
		version.setCommandExecutor((sender, label, parameters) -> onVersionCMD(sender));
		version.setPermission("LemonBeehive.commands.version");

		Parameter get = build.addParameter("get");
		get.setPermission("LemonBeehive.commands.get");
		get.setCommandExecutor((sender, label, parameters) -> onGetCMD(sender, label, parameters));
		get.setTabCompleter((sender, label, parameters) -> this.onGetTAB(parameters));

		Parameter give = build.addParameter("give");
		give.setPermission("LemonBeehive.commands.give");
		give.setCommandExecutor((sender, label, parameters) -> onGiveCMD(sender, parameters));
		give.setTabCompleter((sender, label, parameters) -> this.onGiveTAB(parameters));

		Parameter admin = build.addParameter("admin");
		admin.setPermission("LemonBeehive.commands.admin");
		admin.setCommandExecutor((sender, label, parameters) -> this.onAdmin(sender, parameters));

		Parameter reload = build.addParameter("reload-config");
		admin.setPermission("LemonBeehive.commands.reload-config");
		reload.setCommandExecutor((sender, label, parameters) -> this.onReloadConfig(sender, parameters));

		Parameter help = build.addParameter("help");
		help.setPermission("LemonBeehive.commands.help");
		help.setCommandExecutor((sender, label, parameters) -> this.onHelpCMD(sender, label, parameters));

		
		//Parameter debug = build.addParameter("debug");
		//debug.setCommandExecutor((sender, label, parameters) -> onDebug(sender, parameters));
	}

	@SuppressWarnings("unused")
	private boolean onDebug(CommandSender sender, String[] args) {
		return false;
	}
	private Collection<String> getPlayerList() {
		return Collections.to(Bukkit.getOnlinePlayers(), Lists.newArrayList(), (player) -> {
			return player.getName();
		});
	}
	private boolean onInfoCMD(CommandSender sender, String label) {
		logger.sendInfo(sender, "&fBeehive Upgradable!");	
		logger.sendNormal(sender, "&cAuthors &7|| &7[&f%author%&7]".replace("%author%", LemonBeehive.getInstance().getDescription().getAuthors().get(0)));
		logger.sendNormal(sender, "&3Version &7|| &f%version%".replace("%version%", LemonBeehive.getInstance().getDescription().getVersion()));
		logger.sendNormal(sender, "&eFor help, use &f/" + label + " help.");
		return true;
	}
	private boolean onVersionCMD(CommandSender sender) {
		logger.sendInfo(sender, "&fPlugin version");
		logger.sendNormal(sender, "&cAuthors: &7[&f%author%&7]".replace("%author%", LemonBeehive.getInstance().getDescription().getAuthors().get(0)));
		logger.sendNormal(sender, "&3Version: &f%version%".replace("%version%", LemonBeehive.getInstance().getDescription().getVersion()));
		return true;
	}
	private boolean onGetCMD(CommandSender sender, String label, String[] args) {
		if (!this.hasPlayer(sender)) return false;
		Player player = (Player) sender;
			if (args.length == 0) {
				logger.sendInfo(sender, Objects.requireNonNull(message.get("help.get-command", String.class)).replace("%command%", label));
				return false;
			}else if (args.length == 1) {
				if (!Utilities.isIntegers(args[0])) {
					logger.sendInfo(sender, message.get("command.wrong-number", String.class));
					return false;
				}
				player.getInventory().addItem(Utilities.getCommandItemStack(player, Utilities.getInt(args[0])));
				return true;
			}else {
				logger.sendInfo(sender, message.get("command.wrong-command", String.class));
			}
		return false;
	}
	private List<String> onGetTAB(String[] parameters) {
		List<String> tabs = Lists.newArrayList();
			if (parameters.length == 1) {
				return Lists.newArrayList("[<amount>]");
			}
			return tabs;
	}
	private List<String> onGiveTAB(String[] parameters) {
		List<String> tabs = Lists.newArrayList();
			if (parameters.length == 1) {
				return Lists.newArrayList(getPlayerList());
			}
			if (parameters.length == 2) {
				return Lists.newArrayList("[<amount>]");
			}
			return tabs;
	}
	private boolean onGiveCMD(CommandSender sender, String[] parameters) {
		if (parameters.length < 2) {
			return wrongCMD(sender);
		}
		if (!Players.isOnline(parameters[0])) {
			logger.sendInfo(sender, this.message.get("command.not-online", String.class).replace("%player%", parameters[0]));
			return false;
		}
		if (!Utilities.isIntegers(parameters[1])) {
			logger.sendInfo(sender, this.message.get("command.wrong-number", String.class));
			return false;
		}
		Player player = Players.getOnline(parameters[0]);
		player.getInventory().addItem(Utilities.getCommandItemStack(player, Utilities.getInt(parameters[1])));
		return true;
	}
	private boolean onAdmin(CommandSender sender, String[] args) {
		if (!this.hasPlayer(sender)) return false;
		Player player = (Player) sender;
		if (isEqual(sender, args, 0)) {
			LemonBeehive.getInstance().get(AdminInventory.class).open(player);
			return true;
		}
		return false;
	}
	private boolean onReloadConfig(CommandSender sender, String[] args) {
		if (hasPermission(sender, "LemonBeehive.commands.reload-config")) {
			if (isEqual(sender, args, 0)) {
				reload.all();
				StaticField.reload();
				logger.sendInfo(sender, message.get("command.reload-config", String.class));
				return true;
			}
		}
		return false;
	}
	private boolean onHelpCMD(CommandSender sender, String label, String[] args) {
		if (isEqual(sender, args, 0)) {
			logger.sendInfo(sender, "&ePlugin help commands");
			if (!Objects.isNull(message.get("help", ConfigurationSection.class))) {
				for (String key : message.get("help", ConfigurationSection.class).getKeys(false)) {
					if (sender.hasPermission("LemonBeehive.commands." + key.replace("-command", "")) || key.equals("help-command")) {
						logger.sendInfo(sender, message.get("help." + key, String.class).replace("%command%", label));
					}
				}
			}
			return true;
		}
		return false;
	}
	private boolean hasPermission(CommandSender sender, String permission) {
		if (!sender.hasPermission(permission)) {
			logger.sendInfo(sender, message.get("command.not-permission", String.class));
			return false;
		}
		return true;
	}
	private boolean hasPlayer(CommandSender sender) {
		if (!(sender instanceof  Player)) {
			logger.sendInfo(sender, message.get("command.only-player", String.class));
			return false;
		}
		return true;
	}
	private boolean isEqual(CommandSender sender, String[] args, int size) {
		if (!(args.length == size)) {
			return wrongCMD(sender);
		}
		return true;
	}
	private boolean wrongCMD(CommandSender sender) {
		logger.sendInfo(sender, message.get("command.wrong-command", String.class));
		return false;
	}
	protected EmptyPlugin getPlugin() {
		return plugin;
	}
}