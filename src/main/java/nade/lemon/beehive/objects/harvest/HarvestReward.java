package nade.lemon.beehive.objects.harvest;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import nade.lemon.beehive.configuration.BeehiveYamlConfig;

public class HarvestReward {
	
	private static Map<Double, Set<ItemStack>> getHarvest(HarvestType type) {
		Map<Double, Set<ItemStack>> harvest = new TreeMap<>();
		
		ConfigurationSection shearsSection = BeehiveYamlConfig.getHarvest().get(type.getSectionPath(), ConfigurationSection.class);
		if (shearsSection != null) {
			for (String path : shearsSection.getKeys(false)) {
				ConfigurationSection pathSection = shearsSection.getConfigurationSection(path);
				Reward reward = getReward(pathSection);
				if (reward != null) {
					if (harvest.containsKey(reward.getChance())) {
						Set<ItemStack> items = harvest.get(reward.getChance());
						items.add(reward.getItem());
						continue;
					}
					Set<ItemStack> items = new HashSet<>();
					items.add(reward.getItem());
					harvest.put(reward.getChance(), items);
					continue;
				}
			}
		}
		return harvest;
	}
	
	public static ItemStack getHarvestItem(HarvestType type) {
		return getRandomItem(getHarvestItems(getHarvest(type)));
	}
	
	public static ItemStack getHarvestItemStack(HarvestType type) {
		ConfigurationSection section = BeehiveYamlConfig.getHarvest().get(type.getSectionPath(), ConfigurationSection.class);
		if (section != null) {
			Set<String> key = section.getKeys(false);
			if (key.size() > 0) {
				ItemStack[] items = new ItemStack[key.size()];
				for (int i = 0; i < key.size(); i++) {
					Object[] keyArray = key.toArray();
					items[i] = new ItemStack(section.getItemStack(keyArray[i] + ".item"));
				}
				ItemStack item = items[new Random().nextInt(items.length)];
				return item != null ? item : type.getDefaultReward();
			}
		}
		return type.getDefaultReward();
	}
	
	private static Set<ItemStack> getHarvestItems(Map<Double, Set<ItemStack>> harvestMap) {
		double randomChance = Math.random();
		for (Double chance : harvestMap.keySet()) {
			if (randomChance <= chance) {
				return harvestMap.get(chance);
			}
		}
		return ((TreeMap<Double, Set<ItemStack>>) harvestMap).lastEntry().getValue();
	}
	
	private static ItemStack getRandomItem(Set<ItemStack> items) {
		int randomItem = new Random().nextInt(items.size());
		return (ItemStack) items.toArray()[randomItem];
	}
	
	private static Reward getReward(ConfigurationSection rewardSection) {
		if (rewardSection != null) {
			Double chance = rewardSection.getDouble("chance");
			ItemStack item = rewardSection.getItemStack("item");
			return new Reward(chance, item);
		}
		return null;
	}
	
	private static class Reward {
		private final double chance;
		private final ItemStack item;
		
		Reward(Double chance, ItemStack item) {
			this.chance = chance;
			this.item = item;
		}
		public double getChance() {
			return chance;
		}
		public ItemStack getItem() {
			return item;
		}
	}
}
