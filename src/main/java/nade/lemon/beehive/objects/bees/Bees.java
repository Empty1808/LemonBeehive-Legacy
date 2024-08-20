package nade.lemon.beehive.objects.bees;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nade.craftbukkit.block.CraftBeehive;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.api.events.interact.PollinateEvent;
import nade.lemon.tag.CompoundTag;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.beehive.utils.bukkit.Entities;
import nade.lemon.utils.Collections;
import nade.lemon.utils.bukkit.Locations;
import nade.lemon.library.nbt.NBTTagEntity;
import nade.net.nbt.NBTTagCompound;
import nade.net.nbt.NBTTagList;
import nade.net.world.level.block.entity.TileEntityBeehive;

public class Bees {

	private final BeehiveObject beehive;
	
	private final List<CompoundTag> bees;
	private final Set<BukkitTask> tasks = Sets.newHashSet();
	
	public Bees(BeehiveObject beehive, boolean runTask) {
		this.beehive = beehive;
		this.bees = beehive.getTags().getCompoundList("bees");
		if (bees.size() == 0) {
			beehive.getTags().setCompoundList("bees", bees);
		}
		getBeeTags().forEach(tags -> {
			Entity entity = Bukkit.getEntity(tags.getUUID("uuid"));
			if (!tags.getBoolean("enter")) {
				if (entity == null || !entity.isValid() || entity.isDead()) {
					this.remove(tags);
				}
			}
		});
		if (runTask) this.getBeeTags().forEach((tags) -> {
			if (tags.getBoolean("enter")) this.runTask(tags);
		});
		if (this.isEnters()) setMaxBees(0);
		else setMaxBees(1);
	}
	
	public void enter(Bee bee, int secondInHive) {
		bee.getAttribute(Attribute.GENERIC_MAX_HEALTH); //call attribute
		bee.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE); //call attribute
		NBTTagCompound nbt = new NBTTagEntity(bee).getCompound();
		UUID uuid = nbt.getUUID("UUID");
		CompoundTag tags = new CompoundTag();
		if (contains(uuid)) tags.replace(this.get(uuid));
		else this.add(tags);
		tags.setInt("second-in-hive", secondInHive);
		tags.setUUID("uuid", uuid);
		tags.setBoolean("enter", true);
		tags.setString("nbt-tags", nbt.toBase64());
		runTask(tags);
		if (isEnters()) setMaxBees(0);
		BeehiveUpdate.normal(beehive);
	}
	
	public void runTask(CompoundTag tags) {
		Bukkit.getScheduler().runTaskTimer(LemonBeehive.getInstance(), (task) -> {
			if (!tasks.contains(task)) tasks.add(task);
			if (!beehive.isLoaded()) {
				task.cancel();
				tasks.remove(task);
				return;
			}
			if (tags.getInt("second-in-hive") <= 0) {
				if (!tags.getString("nbt-tags").equals("empty")) {
					if (leave(tags.getUUID("uuid"), 200)) {
						task.cancel();
						tasks.remove(task);
					}
				}else {
					task.cancel();
				}
				return;
			}
			tags.setInt("second-in-hive", tags.getInt("second-in-hive")-1);
		}, 0, 20);
	}
	
	public void leave(UUID uuid) {
		this.leave(uuid, 0);
	}
	
	public boolean leave(UUID uuid, int cannotEnterBeehive) {
		if (contains(uuid)) {
			CompoundTag tags = get(uuid);
			NBTTagCompound nbt = NBTTagCompound.fromBase64(tags.getString("nbt-tags"));
			if (!beehive.getSpawnLocation().getBlock().isEmpty() || isNight(beehive.getWorld()) || beehive.getWorld().hasStorm()) {
				tags.setInt("second-in-hive", 60);
				return false;
			}
			
			tags.setBoolean("enter", false);
			tags.setString("nbt-tags", "empty");
			if (nbt.getBoolean("HasNectar") && tags.getInt("second-in-hive") <= 0) {
				nbt.setBoolean("HasNectar", false);
				PollinateEvent event = new PollinateEvent(beehive, tags.getUUID("uuid"), beehive.getUpgrade(UpgradeType.valueOf("POLLINATE")).getValue().intValue());
				Bukkit.getPluginManager().callEvent(event);
				if (!beehive.isMaxHoney()) {
					beehive.addHoney(event.getPollen());
				}
				double health = nbt.getDouble("Health");
				double newHealth = health + beehive.getUpgrade(UpgradeType.valueOf("REGENERATION")).getValue().doubleValue();
				nbt.setDouble("Health", newHealth);
			}
		
			this.setCompoundTags(nbt);
			Bee bee = Entities.createBee(nbt, beehive.getSpawnLocation());
			bee.setCannotEnterHiveTicks(cannotEnterBeehive);
		}
		if (!isEnters()) {
			this.setMaxBees(1);
		}
		BeehiveUpdate.normal(beehive);
		return true;
	}

	public void setMaxBees(int maxBees) {
		TileEntityBeehive tileEntity = CraftBeehive.getInstance(beehive.getBeehive()).getTileEntity();
		tileEntity.setMaxBees(maxBees);
		tileEntity.update();
	}

	private void setCompoundTags(NBTTagCompound nbt) {
		NBTTagList<NBTTagCompound> attributes = nbt.getCompoundList("Attributes");
		NBTTagCompound bukkitValues = this.getBukkitValues(nbt);
		CompoundTag lemonValues = this.getLemonValues(bukkitValues);

		NBTTagCompound maxHealth = this.getIf(attributes, element -> {
			return (element.hasKey("Name") && element.getString("Name").replace("_", "")
																	   .replace("minecraft:", "")
																	   .toLowerCase()
																	   .equals("generic.maxhealth"));
		});

		NBTTagCompound attackDamage = this.getIf(attributes, element -> {
			return (element.hasKey("Name") && element.getString("Name").replace("_", "")
																	   .replace("minecraft:", "")
																	   .toLowerCase()
																	   .equals("generic.attackdamage"));
		});

		if (!lemonValues.has("hive-pos")) lemonValues.setString("hive-pos", Locations.toJson(beehive.getLocation()));
		if (!lemonValues.has("default.maxHealth")) lemonValues.setDouble("default.maxHealth", maxHealth.getDouble("Base"));
		if (!lemonValues.has("default.attackDamage")) lemonValues.setDouble("default.attackDamage", attackDamage.getDouble("Base"));

		double newMaxHealth = lemonValues.getDouble("default.maxHealth")+ beehive.getUpgrade(UpgradeType.valueOf("BEES_HEALTH")).getValue().doubleValue();
		double newAttackDamage = lemonValues.getDouble("default.attackDamage") +beehive.getUpgrade(UpgradeType.valueOf("BEES_STRENGTH")).getValue().doubleValue();

		maxHealth.setDouble("Base", newMaxHealth);
		attackDamage.setDouble("Base", newAttackDamage);

		bukkitValues.setString("lemonbeehive:internal", lemonValues.toBase64());
	}

	private <E> E getIf(NBTTagList<E> list, Predicate<? super E> condition) {
		for (int i = 0; i < list.size(); i++) {
			if (condition.test(list.get(i))) {
				return list.get(i);
			}
		}
		return null;
	}

	private NBTTagCompound getBukkitValues(NBTTagCompound compound) {
		if (!compound.hasKey("BukkitValues")) {
			NBTTagCompound bukkit = new NBTTagCompound();
			compound.setCompound("BukkitValues", bukkit);
			return bukkit;
		}
		return compound.getCompound("BukkitValues");
	}

	private CompoundTag getLemonValues(NBTTagCompound bukkitValues) {
		return bukkitValues.hasKey("lemonbeehive:internal") ? CompoundTag.fromBase64(bukkitValues.getString("lemonbeehive:internal")) : new CompoundTag();
	}
	
	public void leaves() {
		for (CompoundTag tags : bees) {
			tags.setInt("second-in-hive", -1);
			tags.setBoolean("enter", false);
			tags.setString("nbt-tags", "empty");
		}
	}
	
	public void destroy() {
		tasks.forEach((task) -> {
			task.cancel();
		});
		Collections.removeIfs(bees, element -> {
			return !element.getBoolean("enter");
		});
	}
	
	public boolean add(CompoundTag tags) {
		return bees.add(tags);
	}
	
	public boolean remove(CompoundTag tags) {
		return bees.remove(tags);
	}
	
	public boolean remove(UUID uuid) {
		CompoundTag tags = get(uuid);
		return remove(tags);
	}
	
	public int getMax() {
		return beehive.getUpgrade(UpgradeType.valueOf("BEEHIVE_CAPACITY")).getValue().intValue();
	}
	
	public boolean isMax() {
		return bees.size() >= getMax();
	}
	
	public boolean contains(UUID uuid) {
		for (CompoundTag tags : bees) {
			if (uuid.equals(tags.getUUID("uuid"))) return true;
		}
		return false;
	}
	
	public boolean isEnters() {
		if (bees.isEmpty() || !isMax()) {
			return false;
		}
		for (CompoundTag tags : this.bees) {
			if (!tags.getBoolean("enter")) return false;
		}
		return true;
	}
	
	public CompoundTag get(UUID uuid) {
		for (CompoundTag tags : bees) {
			if (uuid.equals(tags.getUUID("uuid"))) return tags;
		}
		return null;
	}
	
	public Collection<UUID> getBees() {
		Collection<UUID> bees = Lists.newArrayList();
		for (CompoundTag tags : this.bees) {
			bees.add(tags.getUUID("uuid"));
		}
		return bees;
	}
	
	public Collection<Bee> getLeaves() {
		Collection<Bee> leaves = Lists.newArrayList();
		Set<CompoundTag> deaths = Sets.newHashSet();
		for (CompoundTag tags : this.bees) {
			if (!tags.getBoolean("enter")) {
				UUID uuid = tags.getUUID("uuid");
				Entity entity = Bukkit.getEntity(uuid);
				if (entity == null || entity.getType() != EntityType.BEE) {
					deaths.add(tags);
					continue;
				}
				leaves.add((Bee) entity);
			}
		}
		bees.removeAll(deaths);
		return leaves;
	}
	
	public Collection<UUID> getEnters() {
		Collection<UUID> enters = Lists.newArrayList();
		for (CompoundTag tags : this.bees) {
			if (tags.getBoolean("enter")) 
				enters.add(tags.getUUID("uuid"));
		}
		return enters;
	}
	
	public Collection<CompoundTag> getBeeTags() {
		return Lists.newArrayList(bees);
	}
	
	public BeehiveObject getBeehiveObject() {
		return beehive;
	}

	private boolean isDay(World world) {
        long time = world.getTime();
        return (world.getEnvironment() == Environment.NORMAL) && (time > 0 && time < 13000);
    }

	private boolean isNight(World world) {
		return (world.getEnvironment() == Environment.NORMAL) && !isDay(world);
	}
}