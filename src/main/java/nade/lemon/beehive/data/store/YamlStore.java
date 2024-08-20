package nade.lemon.beehive.data.store;

import java.util.Objects;
import java.util.Set;

import nade.lemon.beehive.api.events.manage.BeehiveCreationEvent;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.data.LimitHive;
import nade.lemon.beehive.handlers.HologramHandler;
import nade.lemon.tag.CompoundTag;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.CreateReason;
import nade.lemon.utils.bukkit.Locations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;


public class YamlStore implements ServiceDataStore{

	@Override
	public void remove(BeehiveObject manager) {
		String key = Locations.toPosition(manager.getLocation());
		BeehiveConfigBuild config = BeehiveYamlConfig.getServerDatabase();
		if (!Objects.isNull(config.get(key)) && config.get(key, ConfigurationSection.class).getKeys(false) != null) {
			config.set(key, null);
			config.save();
		}
	}

	@Override
	public void add(BeehiveObject beehive) {
		this.set(beehive);
	}
	
	@Override
	public void update(BeehiveObject beehive) {
		this.set(beehive);
	}
	
	private void set(BeehiveObject beehive) {
		String location = Locations.toPosition(beehive.getBeehive().getLocation());
		BeehiveConfigBuild config = BeehiveYamlConfig.getServerDatabase();
		if (beehive != null) {
			config.set(location + ".internal", beehive.getTags().toBase64());
			config.save();
		}
	}

	@Override
	public Object get(String key, Location location) {
		BeehiveConfigBuild config = BeehiveYamlConfig.getServerDatabase();
		CompoundTag tags = CompoundTag.fromBase64(config.get(Locations.toPosition(location) + ".internal", String.class));
		return tags.get(key);
	}
	
	public int loading() {
		BeehiveConfigBuild config = BeehiveYamlConfig.getServerDatabase();
		Set<String> key = config.getKeys(false);
		int database = 0;
		for (String string : key) {
			Location location = Locations.fromPosition(string);
			if (Database.containByLocation(location)) continue;
			if (location.getBlock().getType() != Material.BEEHIVE) {
				config.set(string, null);
				continue;
			}
			CompoundTag tags = CompoundTag.fromBase64(config.get(string + ".internal", String.class));
			BeehiveObject beehive = new BeehiveObject(tags, CreateReason.LOAD);
			BeehiveCreationEvent created = new BeehiveCreationEvent(beehive, this.getType());
			Bukkit.getPluginManager().callEvent(created);
			if (!created.isCancelled()) {
				HologramHandler.add(beehive.getHolograms());
				LimitHive.add(beehive.getOwner().getUniqueId());

				beehive.getHiveBees().getBeeTags().forEach((beeTags) -> beehive.getHiveBees().runTask(beeTags));
				Database.addBeehive(location, beehive);
				database+=1;
			};
		}
		return database;
	}
	
	public int onSave() {
		int count = Database.getBeehives().size();
		Database.getBeehives().clear();
		return count;
	}

	@Override
	public DataStoreType getType() {
		return DataStoreType.YML;
	}
}
