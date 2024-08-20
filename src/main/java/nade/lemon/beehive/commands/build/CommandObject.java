package nade.lemon.beehive.commands.build;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.function.Function3Parameters;
import nade.lemon.utils.Logger;

public abstract class CommandObject {
    protected final Logger logger = LemonBeehive.getInstance().get(Logger.class);
    protected final Messenger messenger = new Messenger();

    protected Permission permission;

    protected Function3Parameters<CommandSender, String, String[], Boolean> command;
    protected Function3Parameters<CommandSender, String, String[], Collection<String>> tab;

    protected Map<String, Parameter> children = Maps.newHashMap();

    /**
     * sets the execution function for the current parameter.
     * @param executes 
     */
    public void setCommandExecutor(Function3Parameters<CommandSender, String, String[], Boolean> command) {
        if (Objects.isNull(command)) return;
        this.command = command;
    }

    public void setTabCompleter(Function3Parameters<CommandSender, String, String[], Collection<String>> tab) {
        if (Objects.isNull(tab)) return;
        this.tab = tab;
    }

    public void setPermission(String permission) {
        this.permission = new Permission(permission);
    }

    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission);
    }

    public Map<String, Parameter> getChildren() {
        return Maps.newHashMap(children);
    }

    protected List<String> getTabs(int location, Collection<String> list, String[] args) {
		List<String> tabs = Lists.newArrayList();
		if (args[location].isEmpty()) {
			return Lists.newArrayList(list);
		}else {
			for (String tab : list) {
				if (tab.toLowerCase().startsWith(args[location].toLowerCase())) {
					tabs.add(tab);
				}
			}
		}
		return tabs;
	}

    protected class Messenger {
        private final ConfigBuild message = LemonBeehive.getInstance().get(Language.class).get("message");

        public void wrongCommand(CommandSender sender) {
            logger.sendInfo(sender, message.get("command.wrong-command", String.class));
        }
        public void wrongPermission(CommandSender sender) {
            logger.sendInfo(sender, message.get("command.not-permission", String.class));
        }
        public void wrongPlayer(CommandSender sender) {
            logger.sendInfo(sender, message.get("command.only-player", String.class));
        }
    }
}
