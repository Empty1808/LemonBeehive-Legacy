package nade.lemon.beehive.listeners.event;

import java.util.ConcurrentModificationException;

import nade.craftbukkit.inventory.CraftItemStack;
import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.api.events.manage.BeehiveCreationEvent;
import nade.lemon.beehive.api.events.manage.BeehiveDeletionEvent;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.data.LimitHive;
import nade.lemon.beehive.data.store.ServiceDataStore;
import nade.lemon.beehive.handlers.HologramHandler;
import nade.lemon.beehive.handlers.ItemDropHandler;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.CreateReason;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import nade.lemon.beehive.utils.Utilities;
import nade.lemon.utils.bukkit.Locations;
import nade.lemon.utils.collect.ItemStackL;
import nade.lemon.library.nbt.NBTTagTileEntity;
import nade.lemon.tag.CompoundTag;
import nade.net.nbt.NBTTagCompound;
import nade.net.nbt.NBTTagList;

public class CraftBlockEvent extends LemonListeners{

	private static ItemStackL stackL = LemonBeehive.getInstance().get(ItemStackL.class);

	protected final ConfigBuild message;

	public CraftBlockEvent(EmptyPlugin plugin) {
		this.plugin = plugin;
		this.message = plugin.get(Language.class).get("message");
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.getBlock().getType() == Material.BEEHIVE) onBeehivePlace(e);
	}
	
	private void onBeehivePlace(BlockPlaceEvent e) {
		ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());
		if (!Utilities.isLocalItemStack(item)) {
			return;
		}
		Location location = e.getBlock().getLocation();
		
		CompoundTag tags = stackL.save(item);
		if (tags.isString("owner") && tags.getString("owner").equalsIgnoreCase("nobody")) {
			tags.setUUID("owner", e.getPlayer().getUniqueId());
		}
		tags.setString("location", Locations.toJson(location));
		BeehiveObject beehive = new BeehiveObject(tags, CreateReason.PLACE);
			BeehiveCreationEvent creation = new BeehiveCreationEvent(beehive, plugin.get(ServiceDataStore.class).getType());
		Bukkit.getPluginManager().callEvent(creation);
		if (!creation.isCancelled()) {
			HologramHandler.add(beehive.getHolograms());
			LimitHive.add(beehive.getOwner().getUniqueId());

			if (isCreative(e.getPlayer())) {
				item.setAmount(item.getAmount() - 1);
				e.getPlayer().getInventory().setItem(e.getHand(), item);
			}
			Database.addBeehive(location, beehive);
		}
	}
	private boolean isSilk(Player player) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getEnchantments().size() > 0) {
			if (hand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
				return true;
			}
		}
		return false;
	}
	private boolean isCreative(Player player) {
		return (player.getGameMode() == GameMode.CREATIVE);
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.BEEHIVE) onBeehiveBreak(e);
		if (e.getBlock().getType() == Material.CHEST) onChestBreak(e);
	}
	
	private void onBeehiveBreak(BlockBreakEvent e) {
		if (!Database.containByLocation(e.getBlock().getLocation())) {
			return;
		}
		BeehiveObject manager = Database.getByLocation(e.getBlock().getLocation());
		if (!manager.isOwner(e.getPlayer().getUniqueId()) && !e.getPlayer().isOp()) {
			String message = this.message.get("system.cant-interact", String.class);
			logger.sendInfo(e.getPlayer(), message.replace("{owner}", manager.getOwner().getName()));
			e.setCancelled(true);
			return;
		}
		if (!isSilk(e.getPlayer()) && !isCreative(e.getPlayer())) {
			manager.onAnger(e.getPlayer());
		}
		manager.destroy();
		dropBeehiveItemStack(e.getBlock(), manager);
	}
	private void onChestBreak(BlockBreakEvent e) {
		Chest chest = (Chest) e.getBlock().getState();
		for (BeehiveObject beehive : Database.getBeehives()) {
			if (beehive.getStorageLink().contains(chest)) {
				beehive.getStorageLink().remove(chest);
				BeehiveUpdate.normal(beehive);
			}
		}
	}
	
	@EventHandler
	public void onHiveBurn(BlockBurnEvent e) {
		if (e.getBlock().getType() != Material.BEEHIVE) {
			return;
		}
		if (!Database.containByLocation(e.getBlock().getLocation())) {
			return;
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onHiveExplode(EntityExplodeEvent e) {
		try {
			for (Block block : e.blockList()) {
				if (block.getType() != Material.BEEHIVE) {
					continue;
				}
				if (!Database.containByLocation(block.getLocation())) {
					continue;
				}
				e.blockList().remove(block);
			}
		} catch (ConcurrentModificationException ex) {}
	}
	
	@EventHandler
	public void onHiveExplode(BlockExplodeEvent e) {
		try {
			for (Block block : e.blockList()) {
				if (block.getType() != Material.BEEHIVE) {
					continue;
				}
				if (!Database.containByLocation(block.getLocation())) {
					continue;
				}
				e.blockList().remove(block);
			}
		} catch (ConcurrentModificationException ex) {}
	}
	
	private void dropBeehiveItemStack(Block block, BeehiveObject manager) {
		ItemStack item = getItemBeehiveTag(block);
		block.setType(Material.AIR);
		
		Location location = block.getLocation();
		location.add(0.5, 0.5, 0.5);
		
		Item itemDrop = block.getWorld().dropItem(location, Utilities.getItemStack(manager, item));
		BeehiveDeletionEvent deletion = new BeehiveDeletionEvent(manager);
		Bukkit.getPluginManager().callEvent(deletion);
		if (!deletion.isCancelled()) {
			ItemDropHandler.addDrop(itemDrop, manager.getOwner().getName());
			Database.removeByLocation(manager.getLocation());

			HologramHandler.remove(manager.getHolograms());
			LimitHive.remove(manager.getOwner().getUniqueId());

			plugin.get(ServiceDataStore.class).remove(manager);
		}
	}
	private ItemStack getItemBeehiveTag(Block block) {
		ItemStack item = new ItemStack(Material.BEEHIVE);
		
		nade.net.world.item.ItemStack sItem = CraftItemStack.asNMSCopy(item);
		
		NBTTagCompound tag = sItem.getTag();
		NBTTagCompound blockEntityTag = new NBTTagCompound();
		NBTTagCompound blockStateTag = new NBTTagCompound();
		
		blockEntityTag.set("Bees", getBeeList(block));
		blockStateTag.setString("honey_level", "0");
		
		tag.setCompound("BlockEntityTag", blockEntityTag);
		tag.setCompound("BlockStateTag", blockStateTag);
		sItem.setTag(tag);
		
		return CraftItemStack.asBukkitCopy(sItem);
	}
	private NBTTagList<NBTTagCompound> getBeeList(Block block) {
		NBTTagTileEntity tile = new NBTTagTileEntity(block.getState());
		return tile.getCompound().getCompoundList("Bees");
	}
}