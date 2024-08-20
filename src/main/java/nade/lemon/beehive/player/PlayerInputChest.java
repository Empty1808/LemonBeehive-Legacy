package nade.lemon.beehive.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.HumanEntity;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.StaticPlaceholder;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.utils.string.AdvancedSendMessage;

public class PlayerInputChest {
	public static final Map<HumanEntity, PlayerInputChest> inputMap = new HashMap<>();
	
	private HumanEntity player;
	private BeehiveObject beehive;

	private final ConfigBuild language = LemonBeehive.getInstance().get(Language.class).get("language");
	
	public PlayerInputChest(HumanEntity player, BeehiveObject beehive, boolean isMessenger) {
		this.player = player;
		this.beehive = beehive;
		if (isMessenger) {
			String message = language.get("storage-link.start", String.class);
			AdvancedSendMessage.send(player, message.replace("%area%", StaticPlaceholder.area()));	
		}
		PlayerInputChest.inputMap.put(player, this);
	}
	
	public HumanEntity getPlayer() {
		return player;
	}
	
	public BeehiveObject getBeehiveObject() {
		return beehive;
	}
}
