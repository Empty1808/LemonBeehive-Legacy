package nade.lemon.beehive.commands.build;

import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import com.google.common.collect.Lists;
import nade.lemon.utils.Arrays;

public class CommandBuild extends CommandObject{
    private CommandBuild(String command) {
        PluginCommand pluginCommand = Bukkit.getPluginCommand(command);
        pluginCommand.setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] parameters) {
                if (!Objects.isNull(permission) && !hasPermission(sender)) {
                    CommandBuild.this.messenger.wrongPermission(sender);
                    return false;
                }
                if (parameters.length > 0) {
                    for (Parameter parameter : CommandBuild.this.children.values()) {
                        if (parameters[0].equals(parameter.getParameter())) {
                            return parameter.onCommand(sender, label, Arrays.remove(parameters, 0));
                        }
                    }
                }
                if (Objects.isNull(CommandBuild.this.command)) {
                    CommandBuild.this.messenger.wrongCommand(sender);
                    return false;
                }
                return CommandBuild.this.command.apply(sender, label, parameters);
            }
        });
        pluginCommand.setTabCompleter(new TabCompleter() {
            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] parameters) {
                if (!Objects.isNull(permission) && !hasPermission(sender)) {
                    return Lists.newArrayList();
                }
                if (parameters.length > 0) {
                    for (Parameter parameter : CommandBuild.this.children.values()) {
                        if (parameters[0].equals(parameter.getParameter()) && parameters.length > 1) {
                            return parameter.onTabComplete(sender, label, Arrays.remove(parameters, 0));
                        }
                    }
                }
                if (Objects.isNull(CommandBuild.this.tab)) {
                    if (parameters.length > 1) {
                        return Lists.newArrayList();
                    }
                    return getTabs(0, Lists.newArrayList(CommandBuild.this.children.keySet()), parameters);
                }
                return getTabs(0, CommandBuild.this.tab.apply(sender, label, parameters), parameters);
            }
        });
    }

    public Parameter addParameter(String parameter) {
        Parameter result = Parameter.create(parameter);
        children.put(parameter, result);
        return result;
    }

    public static CommandBuild build(String command) {
        return new CommandBuild(command);
    }
}
