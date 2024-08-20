package nade.lemon.beehive.api.events.interact;

import java.util.UUID;

import nade.lemon.beehive.listeners.event.custom.EmptyEvent;
import nade.lemon.beehive.objects.BeehiveObject;

public class PollinateEvent extends EmptyEvent {

	private BeehiveObject object;
	private UUID uniqueId;
	private int pollen;
	
	public PollinateEvent(BeehiveObject object, UUID uniqueId, int pollen) {
		this.object = object;
		this.pollen = pollen;
		this.uniqueId = uniqueId;
	}

	public BeehiveObject getBeehive() {
		return object;
	}
	
	public int getPollen() {
		return pollen;
	}

	public UUID getEntityUniqueId() {
		return uniqueId;
	}
}
