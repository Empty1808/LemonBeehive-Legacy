package nade.lemon.beehive.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class BeehiveTabCompleter implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmds, String label, String[] args) {
		List<String> tabs = new ArrayList<>();
		if (sender.hasPermission("LemonBeehive.commands")) {
			if (args.length == 1) {
				return getCommandTabs(0, Arrays.asList(BASE), args);
			}
			if (args.length > 1) {
				if (args[0].equals("get")) {
					if (args.length == 2) {
						return getCommandTabs(1, getStringList("[<amount>]"), args);
					}
				}
				if (args[0].equals("give")) {
					if (args.length == 2) {
						return getCommandTabs(1, getPlayerList(), args);
					}
					if (args.length == 3) {
						return getCommandTabs(2, getStringList("[<amount>]"), args);
					}
				}
			}
		}
		return tabs;
	}
	
	private List<String> getCommandTabs(int location, List<String> list, String[] args) {
		List<String> tabs = new ArrayList<>();
		if (args[location].isEmpty()) {
			return list;
		}else {
			for (String tab : list) {
				if (tab.toLowerCase().startsWith(args[location].toLowerCase())) {
					tabs.add(tab);
				}
			}
		}
		return tabs;
	}

	private List<String> getPlayerList(String... ignores) {
		List<String> players = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			boolean bypass = false;
			for (String ignore : ignores) {
				if (player.getName().toLowerCase().equals(ignore.toLowerCase())) {
					bypass = true;
				}
			}
			if (bypass) continue;
			players.add(player.getName());
		}
		return players;
	}
	
	private List<String> getStringList(String... args) {
		List<String> arrayList = new ArrayList<>();
		for (String item : args) {
			arrayList.add(item);
		}
		return arrayList;
	}
	
	private static final String[] BASE = {
			"get",
			"give",
			"help",
			"version",
			"admin",
			"reload-config"
	};
}
