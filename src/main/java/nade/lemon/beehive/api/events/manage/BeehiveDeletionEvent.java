package nade.lemon.beehive.api.events.manage;

import org.bukkit.event.Cancellable;

import nade.lemon.beehive.listeners.event.custom.EmptyEvent;
import nade.lemon.beehive.objects.BeehiveObject;

public class BeehiveDeletionEvent extends EmptyEvent implements Cancellable{

	private BeehiveObject beehive;
	private boolean cancel = false;
	
	public BeehiveDeletionEvent(BeehiveObject beehive) {
		this.beehive = beehive;
	}
	
	public BeehiveObject getBeehive() {
		return beehive;
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
