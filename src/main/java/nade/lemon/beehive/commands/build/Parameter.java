package nade.lemon.beehive.commands.build;

import java.util.List;
import java.util.Objects;

import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;
import nade.lemon.utils.Arrays;

public class Parameter extends CommandObject{
    private final String parameter;
    private final int index;

    private Parameter(String parameter, int index) {
        this.parameter = parameter;
        this.index = index;
    }

    /**
     * returns the name of current command parameter.
     * @return name of the parameter
     */
    public String getParameter() {
        return this.parameter;
    }

    public int getIndex() {
        return index;
    }

    /**
     * adds a child parameter to the current parameter.
     * @param parameter the parameter
     * @return a new Parameter object
     */
    public Parameter add(String parameter) {
        int index = this.index;
        Parameter result = new Parameter(parameter, index+1);
        this.children.put(parameter, result);
        return result;
    }

    /**
     * removes a child parameter of the current parameter.
     * @param parameter the parameter
     * @return the deleted Parameter object
     */
    public Parameter remove(String parameter) {
        return children.remove(parameter);
    }

    /**
     * processes the command based on the input string and the execution function set for the parameter.
     * @param sender the sender
     * @param label the command label
     * @param parameters command parameter  
     * @return 
     */
    public boolean onCommand(CommandSender sender, String label, String[] parameters) {
        if (!Objects.isNull(permission) && !hasPermission(sender)) {
            this.messenger.wrongPermission(sender);
            return false;
        }
        if (parameters.length > 0) {
            for (Parameter parameter : this.children.values()) {
                if (parameters[0].equals(parameter.getParameter())) {
                    return parameter.onCommand(sender, label, Arrays.remove(parameters, 0));
                }
            }
        }
        if (Objects.isNull(this.command) || parameters.length < 0) {
            this.messenger.wrongCommand(sender);
            return false;
        }
        return this.command.apply(sender, label, parameters);
    }

    public List<String> onTabComplete(CommandSender sender, String label, String[] parameters) {
        if (!Objects.isNull(permission) && !hasPermission(sender)) {
            return Lists.newArrayList();
        }
        if (parameters.length > 0) {
            for (Parameter parameter : this.children.values()) {
                if (parameters[0].equals(parameter.getParameter()) && parameters.length > 1) {
                    return parameter.onTabComplete(sender, label, Arrays.remove(parameters, 0));
                }
            }
        }
        if (Objects.isNull(this.tab)) {
            if (parameters.length > 1) {
                return Lists.newArrayList();
            }
            return getTabs(0, Lists.newArrayList(this.children.keySet()), parameters);
        }
        return super.getTabs(parameters.length-1, this.tab.apply(sender, label, parameters), parameters);
    }

    /**
     * creates a new Parameter object with the defined parameter name.
     * @param parameter the parameter
     * @return a new Parameter
     */
    public static Parameter create(String parameter) {
        return new Parameter(parameter, 0);
    }
}