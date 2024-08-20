package nade.lemon.beehive.api.events.interact;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;

import nade.lemon.beehive.listeners.event.custom.EmptyEvent;
import nade.lemon.beehive.objects.BeehiveObject;

public class HarvestBeehiveEvent extends EmptyEvent implements Cancellable{
    
    private BeehiveObject object;
	private HumanEntity player;
	private boolean cancel = false;

	public HarvestBeehiveEvent(BeehiveObject object, HumanEntity player) {
		this.object = object;
		this.player = player;
	}

	public BeehiveObject getBeehive() {
		return object;
	}

	public HumanEntity getPlayer() {
		return player;
	}

	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	public boolean isCancelled() {
		return cancel;
	}
}
