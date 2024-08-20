package nade.lemon.beehive.objects;

import java.util.*;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.data.store.ServiceDataStore;
import nade.lemon.beehive.objects.ally.BeehiveAlly;
import nade.lemon.beehive.objects.bees.Bees;
import nade.lemon.beehive.objects.inventory.BeehivesInventory;
import nade.lemon.beehive.objects.storage.LinkedStorage;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.placeholder.BeehiveObjectPlaceholder;
import nade.lemon.beehive.upgrades.Upgrade;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.tag.CompoundTag;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Beehive;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nade.lemon.utils.Collections;
import nade.lemon.beehive.utils.bukkit.Entities;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.utils.bukkit.Locations;
import nade.lemon.utils.string.Color;
import nade.lemon.library.hologram.Holograms;

public class BeehiveObject {
	private CompoundTag tags;
	
	private final OfflinePlayer owner;
	private String displayName;
	
	private final Location location;
	private Beehive hive;
	private Set<String> friend = new HashSet<>();

	private Holograms holograms;
	
	private final LinkedStorage storageLink;
	
	private final Bees hiveBees;
	private final BeehiveAlly ally;
	
	private final InventoryBuild menu;

	private final BeehiveObjectPlaceholder placeholder = new BeehiveObjectPlaceholder(this);

	private final ConfigBuild language;
	
	//attribute
	private boolean loaded = true;

	private UpgradeSystem upgradeSystem = LemonBeehive.getInstance().get(UpgradeSystem.class);
	
	public BeehiveObject(CompoundTag tags, CreateReason reason) {
		this.tags = tags;
		
		this.owner = Bukkit.getOfflinePlayer(UUID.fromString(tags.getString("owner")));
		this.displayName = tags.getString("display_name");
		this.location = Locations.fromJson(tags.getString("location"));
		this.hive = (Beehive) location.getBlock().getState();
		this.friend = Sets.newHashSet(tags.getStringList("friends"));
		
		this.storageLink = new LinkedStorage(this);
		
		this.hiveBees = new Bees(this, (reason == CreateReason.PLACE) ? true : false);
		this.ally = new BeehiveAlly(this.owner.getUniqueId());
		this.ally.add(friend.toArray(new String[0]));

		this.language = LemonBeehive.getInstance().get(Language.class).get("language");

		if (!this.tags.has("uuid")) {
			this.tags.set("uuid", UUID.randomUUID().toString());
		}

		setupHolograms(hive.getLocation().clone());
		
		this.menu = BeehivesInventory.setupMenu(this);
		BeehiveUpdate.normal(this);
		
	}
	
	public InventoryBuild getMenu() {
		return menu;
	}

	public LinkedStorage getStorageLink() {
		return storageLink;
	}
	
	public Holograms getHolograms() {
		return holograms;
	}
	
	private void setupHolograms(Location location) {
		this.holograms = new Holograms(location.clone().add(0.5, 1, 0.5), 0.25);
		for (String text : language.getList("hologram.beehive", String.class)) {
			text = this.placeholder.replace(text);
			this.holograms.add(Color.hex(text));
		}
	}

	public BeehiveObjectPlaceholder getPlaceholder() {
		return placeholder;
	}
	
	public List<String> getFriends() {
		return Lists.newLinkedList(this.friend);
	}
	
	public boolean addFriend(OfflinePlayer player) {
		return addFriend(player.getUniqueId());
	}
	
	public boolean addFriend(UUID uuid) {
		boolean added = friend.add(uuid.toString());
		if (added) {
			this.ally.add(uuid);
			LemonBeehive.getInstance().get(ServiceDataStore.class).update(this);
		};
		return added;
	}

	public boolean removeFriend(OfflinePlayer player) {
		return removeFriend(player.getUniqueId());
	}
	
	public boolean removeFriend(UUID uuid) {
		boolean removed = friend.remove(uuid.toString());
		if (removed) {
			this.ally.remove(uuid);
			LemonBeehive.getInstance().get(ServiceDataStore.class).update(this);
		};
		return removed;
	}
	
	public boolean isFriend(UUID uuid) {
		return getFriends().contains(uuid.toString());
	}
	
	public void setFriend(Set<String> friend) {
		this.friend = friend;
		LemonBeehive.getInstance().get(ServiceDataStore.class).update(this);
	}
	
	public OfflinePlayer getOwner() {
		return owner;
	}
	
	public boolean isOwner(UUID uuid) {
		return getOwner().getUniqueId().equals(uuid);
	}

	public Upgrade getUpgrade(UpgradeType type) {
		return upgradeSystem.getByLevel(type, this.getUpgrades().getInt(type.getType(), 1));
	}

	public void setUpgrade(UpgradeType type, int level) {
		this.getUpgrades().setInt(type.getType(), level);
	}
	
	public CompoundTag getUpgrades() {
		return tags.getCompound("upgrades");
	}
	
	public CompoundTag getStorages() {
		return tags.getCompound("storages");
	}
	
	public void setTags(CompoundTag tags) {
		this.tags = new CompoundTag(tags);
	}
	
	public CompoundTag getTags() {
		if (ally != null) {
			this.tags.setStringList("friends", Lists.newArrayList(Collections.to(ally.getAllys(), new HashSet<String>(), element -> element.toString())));
		}
		return this.tags;
	}
	
	public Bees getHiveBees() {
		return hiveBees;
	}
	
	public Beehive getBeehive() {
		return (Beehive) hive.getBlock().getState();
	}
	
	public org.bukkit.block.data.type.Beehive getDataBeehive() {
		return (org.bukkit.block.data.type.Beehive) getBeehive().getBlockData();
	}
	
	public void save() {
		LemonBeehive.getInstance().get(ServiceDataStore.class).update(this);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public BeehiveAlly getAlly() {
		return ally;
	}

	public UUID getUniqueId() {
		return UUID.fromString(tags.get("uuid", String.class));
	}
	
	public boolean harvest(int honey) {
		if (this.isMaxHoney()) {
			org.bukkit.block.data.type.Beehive dataBeehive = getDataBeehive();
			dataBeehive.setHoneyLevel(0);
			getBeehive().getBlock().setBlockData(dataBeehive);
			this.setHoney(honey);
			BeehiveUpdate.normal(this);
			return true;
		}
		return false;
	}
	
	public Location getDropLocation() {
		return this.getLocation().clone().add(0.5, 0.7, 0.5);
	}

	public Location getSpawnLocation() {
		return this.getLocation().getBlock().getRelative(this.getDataBeehive().getFacing()).getLocation().add(0.5, 0.5, 0.5);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public World getWorld() {
		return getLocation().getWorld();
	}
	
	public void setHoney(int honey) {
		if (honey > getMaxHoney()) honey = getMaxHoney();
		this.getTags().getCompound("storages").setInt("honey", honey);
	}
	
	public void addHoney(int honey) {
		this.setHoney(honey+getHoney());
	}
	
	public int getHoney() {
		return this.getTags().getCompound("storages").getInt("honey");
	}
	
	public int getMaxHoney() {
		return this.getUpgrade(UpgradeType.valueOf("honey-capacity")).getValue().intValue();
	}
	
	public boolean isMaxHoney() {
		return getHoney() >= getMaxHoney();
	}
	
	public void onLoaded() {
		this.loaded = true;
		hiveBees.getBeeTags().forEach((tags) -> hiveBees.runTask(tags));
	}
	
	public void onUnloaded() {
		this.loaded = false;
	}
	
	public void onAnger(Player player) {
		hive.releaseEntities();
		for (Bee bee : getHiveBees().getLeaves()) {
			bee.setAnger(500);
			bee.setTarget(player);
		}
		BeehiveUpdate.normal(this);
	}
	
	public void onClearTags() {
		for (Bee bee : getHiveBees().getLeaves()) {
			CompoundTag tags = Entities.getTags(bee);

			if (tags.has("default.maxHealth")) {
				double health = bee.getHealth();
				double maxHealth = tags.getDouble("default.maxHealth");
				if (health >= maxHealth) {
					bee.setHealth(maxHealth);
				}
				bee.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
			}
			if (tags.has("default.attackDamage")) {
				bee.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(tags.getDouble("default.attackDamage"));
			}

			tags.remove("hive-pos");
			Entities.setTags(bee, tags);
		}
	}
	
	public void onClearLinked() {
		getStorageLink().clear();
	}
	
	public void destroy() {
		onClearTags();
		onClearLinked();
		getHiveBees().destroy();
	}
	
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
		if (loaded) onLoaded();
		else onUnloaded();
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	@Override
	public String toString() {
		return "[LemonBeehive={"
				+ "x=" + getLocation().getX()
				+ ", y=" + getLocation().getY()
				+ ", z=" + getLocation().getZ()
				+ "}]";
	}
}