package nade.lemon.beehive.listeners.event.custom;

import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;

public class PlayerChangeTargetBlockEvent extends EmptyEvent{
    private final HumanEntity player;
    private Block current;
    private Block previous;

    public PlayerChangeTargetBlockEvent(HumanEntity player, Block current, Block previous) {
        this.player = player;
        this.current = current;
        this.previous = previous;
    }

    public HumanEntity getPlayer() {
        return player;
    }

    public Block getCurrent() {
        return current;
    }

    public Block getPrevious() {
        return previous;
    }
}