package nade.lemon.beehive.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.HumanEntity;

public enum InputTypes {
	
	RewardRate, UpgradeEditor;
	
	private Map<HumanEntity, InputCompound> input = new HashMap<>();
	
	InputTypes() {};
	
	public InputCompound setInput(HumanEntity key, InputCompound value) {
		if (value == null && input.containsKey(key)) {
			return input.remove(key);
		}
		return input.put(key, value);
	}
	
	public InputCompound getInput(HumanEntity key) {
		return input.get(key);
	}
	
	public boolean has(HumanEntity key) {
		return input.containsKey(key);
	}
}
