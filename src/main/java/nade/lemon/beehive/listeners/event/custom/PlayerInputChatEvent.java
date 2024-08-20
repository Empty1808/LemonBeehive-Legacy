package nade.lemon.beehive.listeners.event.custom;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;

import nade.lemon.beehive.chat.InputChat;
import nade.lemon.beehive.chat.InputCompound;
import nade.lemon.beehive.chat.InputTypes;

public class PlayerInputChatEvent extends EmptyEvent implements Cancellable{
	private InputChat.Type type;
	private InputTypes inputTypes;
	private HumanEntity player;
	private String message;
	private InputCompound inputCompound;
	
	private boolean cancel;

	/**
	 * Called when the player enters input chat
	 * @param inputTypes input type
	 * @param player player is doing this
	 * @param message message sent when enter
	 * @param inputCompound input data
	 */
	public PlayerInputChatEvent(InputChat.Type type, InputTypes inputTypes, HumanEntity player, String message, InputCompound inputCompound) {
		this.type = type; this.inputTypes = inputTypes; this.player = player; this.message = message; this.inputCompound = inputCompound;
	}
	
	public InputChat.Type getType() {
		return type;
	}
	
	public InputTypes getInputTypes() {
		return inputTypes;
	}
	
	public HumanEntity getPlayer() {
		return player;
	}
	
	public String getMessage() {
		return message;
	}
	
	public InputCompound getInputCompound() {
		return inputCompound;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
