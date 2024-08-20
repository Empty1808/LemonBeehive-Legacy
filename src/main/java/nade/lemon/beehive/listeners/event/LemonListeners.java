package nade.lemon.beehive.listeners.event;

import org.bukkit.event.Listener;

import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.utils.Logger;

public abstract class LemonListeners implements Listener{
	protected static Logger logger = LemonBeehive.getInstance().get(Logger.class);
	protected EmptyPlugin plugin;
}
