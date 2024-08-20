package nade.lemon.beehive.api.events.manage;

import org.bukkit.event.Cancellable;

import nade.lemon.beehive.data.store.DataStoreType;
import nade.lemon.beehive.listeners.event.custom.EmptyEvent;
import nade.lemon.beehive.objects.BeehiveObject;

public class BeehiveCreationEvent extends EmptyEvent implements Cancellable{

	private BeehiveObject beehive;
	private boolean cancel = false;
	private DataStoreType storeType;
	
	public BeehiveCreationEvent(BeehiveObject beehive, DataStoreType storeType) {
		this.beehive = beehive;
		this.storeType = storeType;
	}
	
	public BeehiveObject getBeehive() {
		return beehive;
	}
	
	public DataStoreType getStoreType() {
		return storeType;
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
