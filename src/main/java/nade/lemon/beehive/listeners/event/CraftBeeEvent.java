package nade.lemon.beehive.listeners.event;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.data.Database;
import nade.lemon.tag.BukkitValuesBuild;
import nade.lemon.tag.CompoundTag;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.upgrades.Upgrade;
import nade.lemon.beehive.upgrades.UpgradeType;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;

import nade.lemon.utils.bukkit.Locations;

public class CraftBeeEvent extends LemonListeners{
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBeeEnterBeehive(EntityEnterBlockEvent e) {
		if (e.getEntityType() != EntityType.BEE) return;
		Bee bee = (Bee) e.getEntity();
		Block enter = e.getBlock();

		Returns returns = this.isInternal(bee);
		if (returns.element(0, Boolean.class)) {
			if (!Locations.equalsBlock(returns.element(2, Location.class), enter.getLocation())) {
				bee.setCannotEnterHiveTicks(20);
				bee.setHive(returns.element(2, Location.class));
				return;
			}
		}
		if (!Database.containByLocation(e.getBlock().getLocation())) return;
		BeehiveObject beehive = Database.getByLocation(e.getBlock().getLocation());
		if (beehive.getHiveBees().isMax() && !beehive.getHiveBees().contains(bee.getUniqueId())) {
			bee.setCannotEnterHiveTicks(20);
			e.setCancelled(true);
			return;
		}
		beehive.getHiveBees().enter(bee, bee.hasNectar() ? 45 : 45);
		e.getEntity().remove();
		e.setCancelled(true);
	}

	@EventHandler
	public void onBeeStung(EntityDamageByEntityEvent e) {
		if (e.getDamager().getType() != EntityType.BEE) return;
		Bee damager = (Bee) e.getDamager();
		Returns returns = this.isInternal(damager);
		if (returns.element(0, Boolean.class)) {
			Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), (task) -> {
				CompoundTag internal = returns.element(1, CompoundTag.class);
				BeehiveObject beehive = returns.element(3, BeehiveObject.class);
				Upgrade stinger = beehive.getUpgrade(UpgradeType.valueOf("bees-stinger"));
				if (damager.hasStung()) {
					internal.setInt("stung", internal.getInt("stung") + 1);
				}
				if (internal.getInt("stung") < stinger.getValue().intValue()) {
					damager.setAnger(0);
					damager.setHasStung(false);
				}
				this.setInternal(damager, internal);
			}, 0);
		}
	}
	
	@EventHandler
	public void onBeeDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.BEE) {
			Bee bee = (Bee) e.getEntity();
			Returns returns = this.isInternal(bee);
			if (returns.element(0, Boolean.class)) {
				BeehiveObject beehive = returns.element(3, BeehiveObject.class);
				beehive.getHiveBees().remove(bee.getUniqueId());
			}
		}             
	}

	private Returns isInternal(Bee entity) {
		BukkitValuesBuild build = BukkitValuesBuild.build(LemonBeehive.getInstance(), entity);
		if (build.hasString("internal")) {
			CompoundTag internal = CompoundTag.fromBase64(build.getString("internal"));
			if (internal.has("hive-pos")) {
				Location location = Locations.fromJson(internal.getString("hive-pos"));
				if (Database.containByLocation(location)) return returns(true, internal, location, Database.getByLocation(location));
			}
		}
		return returns(false);
	}

	private void setInternal(Bee entity, CompoundTag tags) {
		BukkitValuesBuild build = BukkitValuesBuild.build(LemonBeehive.getInstance(), entity);
		build.setString("internal", tags.toBase64());
	}

	private Returns returns(Object... objects) {
		return new Returns(objects);
	}

	private class Returns {
		private Object[] objects;

		public Returns(Object... objects) {
			this.objects = objects;
		}

		public <E> E element(int index, Class<E> clazz) {
			Object object = objects[index];
			if (!clazz.isInstance(object)) return null;
			return clazz.cast(object);
		}

		public String toString() {
			return Arrays.toString(objects);
		}
	}
}