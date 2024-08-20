package nade.lemon.beehive.utils.bukkit;

import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.persistence.PersistentDataContainer;

import static org.bukkit.persistence.PersistentDataType.*;

import org.bukkit.Location;

import nade.craftbukkit.CraftWorld;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.tag.CompoundTag;
import nade.lemon.utils.bukkit.BukkitKeyed;
import nade.net.nbt.NBTTagCompound;
import nade.net.world.entity.animal.EntityBee;

public class Entities {
	private static BukkitKeyed keyed = LemonBeehive.getInstance().get(BukkitKeyed.class);

	public static CompoundTag getTags(Entity entity) {
		PersistentDataContainer container = entity.getPersistentDataContainer();
		return container.has(keyed.byString("internal"), STRING) ? CompoundTag.fromBase64(container.get(keyed.byString("internal"), STRING)) : new CompoundTag();
	}
	
	public static void setTags(Entity entity, CompoundTag tags) {
		PersistentDataContainer container = entity.getPersistentDataContainer();
		container.set(keyed.byString("internal"), STRING, tags.toBase64());
	}
	
	public static Bee createBee(NBTTagCompound nbt, Location location) {
		EntityBee bee = new EntityBee(location.getWorld());
		bee.load(nbt);
		bee.setLocation(location);
		CraftWorld.getInstance(location.getWorld()).getHandle().addEntity(bee, SpawnReason.BEEHIVE);
		return (Bee) bee.getBukkitEntity();
	}
}
