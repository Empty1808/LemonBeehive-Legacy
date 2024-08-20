package nade.lemon.beehive.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.chat.InputChat;
import nade.lemon.beehive.chat.InputCompound;
import nade.lemon.beehive.chat.InputTypes;
import nade.lemon.beehive.listeners.event.custom.PlayerInputChatEvent;
import nade.lemon.utils.Logger;

public class PlayerInputChat {
	private static Logger logger = LemonBeehive.getInstance().get(Logger.class);
	
	public static void onEnterInputChat(InputTypes types, HumanEntity player, String message, InputObject... objects) {
		InputCompound compound = new InputCompound(objects);
		PlayerInputChatEvent playerEnterInput = new PlayerInputChatEvent(InputChat.Type.Enter ,types, player, message, compound);
		Bukkit.getPluginManager().callEvent(playerEnterInput);
		if (!playerEnterInput.isCancelled()) {
			types.setInput(player, compound);
			logger.sendInfo(player, message);
		}
	}
	
	public static void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		for (InputTypes type : InputTypes.values()) {
			if (type.has(e.getPlayer())) {
				logger.sendInfo(e.getPlayer(), "&cYou cannot use commands in edit mode! Please complete it or use '&e-cancel&c' to exit.");
				e.setCancelled(true);
			}
		}
	}
	
	public static void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		for (InputTypes inputTypes : InputTypes.values()) {
			if (inputTypes.has(e.getPlayer())) {
				e.setCancelled(true);
				InputCompound input = inputTypes.getInput(e.getPlayer());
				if (e.getMessage().equals("-cancel")) {
					callInputChatEvent(InputChat.Type.Cancel, inputTypes, e.getPlayer(), null, input);
					continue;
				}
				callInputChatEvent(InputChat.Type.Completed, inputTypes, e.getPlayer(), e.getMessage(), input);
			}
		}
	}
	
	private static void callInputChatEvent(InputChat.Type type, InputTypes inputTypes, Player player, String value, InputCompound input) {
		PlayerInputChatEvent playerEnterInput = new PlayerInputChatEvent(type, inputTypes, player, value, input);
		Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), () -> {
			Bukkit.getPluginManager().callEvent(playerEnterInput);
			if (!playerEnterInput.isCancelled()) {
				inputTypes.setInput(player, null);
			}
		}, 0);
	}
	
	public static InputObject input(String key, Object value) {
		return new InputObject(key, value);
	}
	
	public static class InputObject {
		private final String key;
		private final Object value;
		
		public InputObject(String key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		
		public Object getValue() {
			return value;
		}
	}
}
