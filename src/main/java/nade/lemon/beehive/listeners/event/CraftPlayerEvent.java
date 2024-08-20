package nade.lemon.beehive.listeners.event;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.StaticField;
import nade.lemon.beehive.api.events.interact.HarvestBeehiveEvent;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.FeaturesEnable;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.data.HeadDatabase;
import nade.lemon.beehive.data.LimitHive;
import nade.lemon.beehive.features.harvest.Harvest;
import nade.lemon.beehive.features.harvest.HarvestSystem;
import nade.lemon.beehive.handlers.DataSaveHandler;
import nade.lemon.beehive.handlers.HologramHandler;
import nade.lemon.beehive.handlers.ItemDropHandler;
import nade.lemon.beehive.handlers.PlayerInputHandlers;
import nade.lemon.beehive.handlers.players.BlockTargetListener;
import nade.lemon.beehive.listeners.event.custom.PlayerChangeTargetBlockEvent;
import nade.lemon.beehive.listeners.event.custom.PlayerInputChatEvent;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.bees.BeeStatus;
import nade.lemon.beehive.objects.updates.BeehiveUpdate;
import nade.lemon.beehive.player.PlayerInputChat;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import nade.lemon.beehive.utils.Utilities;
import nade.lemon.beehive.utils.bukkit.Players;
import nade.lemon.utils.bukkit.ItemStacks;
import nade.lemon.utils.bukkit.Locations;
import nade.lemon.beehive.utils.string.AdvancedSendMessage;
import nade.lemon.tag.BukkitValuesBuild;
import nade.lemon.tag.CompoundTag;

public class CraftPlayerEvent extends LemonListeners{
	private final EmptyPlugin plugin;
	private final ConfigBuild message;

	public CraftPlayerEvent(EmptyPlugin plugin) {
		this.plugin = plugin;
		this.message = plugin.get(Language.class).get("message");
	}

	@EventHandler
	public void onPlayerChangeTargetBlock(PlayerChangeTargetBlockEvent e) {
		BeehiveObject beehive = Database.getByLocation(e.getPrevious().getLocation());
		if (e.getPrevious().getType() == Material.BEEHIVE) {
			if (!Objects.isNull(beehive)) {
				beehive.getHolograms().hide((Player) e.getPlayer());
			}
		}
		if (e.getCurrent().getType() == Material.BEEHIVE) {
			if (!Objects.isNull(beehive)) {
				beehive.getHolograms().show((Player) e.getPlayer());
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Action action = e.getAction();
		Block clicked = e.getClickedBlock();
		EquipmentSlot hand = e.getHand();

		if (action == Action.RIGHT_CLICK_BLOCK && (hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND)) {
			if (e.getItem() != null && isBeehive(e)) onCancelledPlace(e);
			if (clicked.getType() == Material.BEEHIVE) onBeehiveInteractEvent(e);
			if (clicked.getType() == Material.CHEST) PlayerInputHandlers.onChestInteractEvent(e);
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getHand() == EquipmentSlot.HAND && e.getRightClicked().getType() == EntityType.BEE) {
			Bee entity = (Bee) e.getRightClicked();
			BukkitValuesBuild values = BukkitValuesBuild.build(LemonBeehive.getInstance(), entity);
			if (!values.hasString("internal")) return;
			CompoundTag tags = CompoundTag.fromBase64(values.getString("internal"));
			if (!tags.has("hive-pos")) return;
			Location hivePos = Locations.fromJson(tags.getString("hive-pos"));
			if (hivePos == null) return;
			BeehiveObject beehive = Database.getByLocation(hivePos);
			if (Objects.isNull(beehive)) return;
			if (!e.getPlayer().equals(beehive.getOwner().getPlayer())) return;
			if (entity.hasStung()) return;
			if (!isAction(BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive.open-action", String.class), Action.RIGHT_CLICK_BLOCK, e.getPlayer())) return;
			System.err.println(true);
			BeeStatus.open(e.getPlayer(), beehive, entity, tags);
		}
	}
	
	private void onCancelledPlace(PlayerInteractEvent e) {
		if (onPlaceWorldBlock(e)) return;
		onPlaceWhileNotOwner(e);
		onLimitBeehive(e);
	}
	
	private void onBeehiveInteractEvent(PlayerInteractEvent e) {
		Location location = e.getClickedBlock().getLocation();
		if (Database.containByLocation(location)) {
			if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isOp()) {
				ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
				if (hand != null && hand.getType() == Material.ARROW) {
					BeehiveObject beehives = Database.getByLocation(location);
					beehives.setHoney(beehives.getMaxHoney());
					BeehiveUpdate.normal(beehives);
				}
				//System.out.println("Beehive Tags: " + Database.getBeehive(location).getTags());
			}
			onOpenBeehive(e);
			//onHarvestHoney(e);
			onHarvest(e);
		}
	}

	private void cancelled(Cancellable e) {
		e.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		PlayerInputChat.onAsyncPlayerChat(e);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		DataSaveHandler.onPlayerCommandPreprocess(e);
		PlayerInputChat.onPlayerCommandPreprocess(e);
	}
	
	@EventHandler
	public void onPlayerInputChat(PlayerInputChatEvent e) {
		//RewardEditor.onPlayerInputChat(e);
		plugin.get(HarvestSystem.class).getEvent().onPlayerInputChat(e);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		HologramHandler.onPlayerRespawn(e);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		HologramHandler.onPlayerDeath(e);
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		PlayerInputHandlers.onPlayerToggleSneak(e);
	}
	
	public void onOpenBeehive(PlayerInteractEvent e) {
		Location location = e.getClickedBlock().getLocation();
		BeehiveObject manager = Database.getByLocation(location);
		if (isAction(BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive.open-action", String.class), e.getAction(), e.getPlayer())) {
			if (e.getHand().equals(EquipmentSlot.HAND)) {
				if (!e.getPlayer().getUniqueId().equals(manager.getOwner().getUniqueId())) {
					logger.sendInfo(e.getPlayer(), message.get("system.not-owner", String.class)
						.replace("{owner}", manager.getOwner().getName()));
					return;
				}
				manager.getMenu().open(e.getPlayer());
				cancelled(e);
			}
			return;
		}
	}
	
	/**
	 * Place LemonBeehive but they are not owner
	 * @param e (PlayerInteractEvent)
	 */
	public void onPlaceWhileNotOwner(PlayerInteractEvent e) {
		if (Utilities.getStringOwner(e.getItem()) != null) {
			if (Utilities.getStringOwner(e.getItem()).equals("nobody")) {
				return;
			}
			if (!e.getPlayer().getUniqueId().equals(Utilities.getOwner(e.getItem()))) {
				String owner = Bukkit.getOfflinePlayer(Utilities.getOwner(e.getItem())).getName();
				logger.sendInfo(e.getPlayer(), message.get("system.not-owner", String.class)
					.replace("{owner}", owner));
				cancelled(e);
				return;
			}
		}
	}
	
	public boolean onPlaceWorldBlock(PlayerInteractEvent e) {
		if (FeaturesEnable.WORLD_BLACKLIST.isEnable()) {
			List<String> blacklist = BeehiveYamlConfig.getConfig().getList("general.world-blacklist.blacklist", String.class);
			if (blacklist.contains(e.getPlayer().getWorld().getName())) {
				AdvancedSendMessage.send(e.getPlayer(), message.get("system.blacklist-world", String.class)
					.replace("{world-name}", e.getPlayer().getWorld().getName()));
				cancelled(e);
				return true;
			}
		}
		return false;
	}

	private void onHarvest(PlayerInteractEvent e) {
		BeehiveObject object = Database.getByLocation(e.getClickedBlock().getLocation());
		Beehive beehive = (Beehive) e.getClickedBlock().getState();
		org.bukkit.block.data.type.Beehive data = (org.bukkit.block.data.type.Beehive) beehive.getBlockData();
		if (e.getItem() != null && !e.getPlayer().isSneaking()) {
			if (e.getItem().getType() == Material.SHEARS || e.getItem().getType() == Material.GLASS_BOTTLE) e.setCancelled(true);
			HarvestSystem harvestSystem = plugin.get(HarvestSystem.class);
			for (Harvest harvest : harvestSystem.values()) {
				if (ItemStacks.equals(e.getItem(), harvest.getTool()) && object.isMaxHoney()) {
					if (!object.getAlly().contains(e.getPlayer())) {
						logger.sendInfo(e.getPlayer(), message.get("system.cant-interact", String.class)
							.replace("{owner}", object.getOwner().getName()));
						return;
					}
					int count = (object.getMaxHoney() / StaticField.HONEY_MULTIPLIER);
					if (harvest.isConsumable()) {
						if (e.getItem().getItemMeta() instanceof Damageable && ((Damageable) e.getItem().getItemMeta()).hasDamage()) {
							Damageable damageable = (Damageable) e.getItem().getItemMeta();
							int durability = e.getItem().getType().getMaxDurability() - damageable.getDamage();
							if (durability <= count) {
								e.getItem().setAmount(e.getItem().getAmount() - 1);
								count -= durability;
								if (e.getItem().getAmount() <= 0) {
									e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
								}
							}else {
								damageable.setDamage(damageable.getDamage() + count);
								e.getItem().setItemMeta((ItemMeta) damageable);
								count = 0;
							}
						}else {
							int amount = e.getItem().getAmount();
							if (amount <= count) {
								e.getItem().setAmount(e.getItem().getAmount() - amount);
								count -= amount;
							}else {
								e.getItem().setAmount(amount - count);
								count = 0;
							}
						}
					}else {
						count = 0;
					}
					HarvestBeehiveEvent harvestBeehive = new HarvestBeehiveEvent(object, e.getPlayer());
					Bukkit.getPluginManager().callEvent(harvestBeehive);
					if (!harvestBeehive.isCancelled()) {
						data.setHoneyLevel(0);
						beehive.getBlock().setBlockData(data);
						object.setHoney(count * StaticField.HONEY_MULTIPLIER);
						BeehiveUpdate.normal(object);
						if (e.getItem().getType() != Material.SHEARS && e.getItem().getType() != Material.GLASS_BOTTLE) {
							e.getPlayer().playSound(object.getLocation(), Sound.BLOCK_BEEHIVE_DRIP, 9, 2);
						}
						for (int i = 0; i < (((object.getMaxHoney() / StaticField.HONEY_MULTIPLIER)) - count); i++) {
							Collection<ItemStack> drops = harvest.getDrops().drop(ItemStack.class);
							object.getStorageLink().addItems(drops.toArray(new ItemStack[0]));
						}
						if (!isCreative(e.getPlayer()) && !beehive.isSedated()) {
							object.onAnger(e.getPlayer());
						}
					}
					e.setCancelled(true);
					break;
				}
			}
		}
	}
	
	/**
	 * Harvest honey event
	 * @param e (PlayerInteractEvent)
	 */
	private boolean isCreative(Player player) {
		return (player.getGameMode() == GameMode.CREATIVE);
	}
	
	/**
	 * Limit place LemonBeehive event
	 * @param e (PlayerInteractEvent)
	 */
	public void onLimitBeehive(PlayerInteractEvent e) {
		if (!FeaturesEnable.BEEHIVE_LIMIT.isEnable()) return;
		UUID uuid = e.getPlayer().getUniqueId();
		int maxLimit = BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.beehive.limit.default", Integer.class);
		for (Permission perm : LimitHive.getLimitPerm().keySet()) {
			if (e.getPlayer().hasPermission(perm)) {
				int i = LimitHive.getLimitPerm().get(perm);
				if (i > maxLimit) {
					maxLimit = i;
				}
			}
		}
		int limit = LimitHive.getLimit(uuid);
		if (limit >= maxLimit && !e.getPlayer().isOp()) {
			e.setCancelled(true);
			String sLimit = String.valueOf(maxLimit);
			logger.sendInfo(e.getPlayer(), message.get("system.limit-beehive", String.class).replace("{beehive_limit}", sLimit));
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Item item = e.getItemDrop();
		if (Utilities.isLocalItemStack(item.getItemStack())) {
			Utilities.getStringOwner(item.getItemStack());
			ItemDropHandler.addDrop(item, Utilities.getStringOwner(item.getItemStack()));
		}
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onEntityPickupItem(EntityPickupItemEvent e) {
		if (e.getEntity().getType() == EntityType.PLAYER) {
			Item item = e.getItem();
			if (Utilities.isLocalItemStack(item.getItemStack())) {
				ItemDropHandler.removeDrop(item);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		HologramHandler.onPlayerJoin(e);
		HeadDatabase.onPlayerJoin(e.getPlayer());
		BlockTargetListener.onPlayerJoin(plugin, e);
		Players.onPlayerJoin(e);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent e) {
		HologramHandler.onPlayerQuit(e);
		HeadDatabase.onPlayerQuit(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
		HologramHandler.onPlayerChangeWorld(e);
	}
	
	private boolean isBeehive(PlayerInteractEvent e) {
		ItemStack hand, offhand;
		hand = e.getPlayer().getInventory().getItemInMainHand();
		offhand = e.getPlayer().getInventory().getItemInOffHand();
		if (e.getHand().equals(EquipmentSlot.HAND)) {
			if (Utilities.isLocalItemStack(hand)) {
				return true;
			}
		}else if (e.getHand().equals(EquipmentSlot.OFF_HAND)) {
			if (Utilities.isLocalItemStack(offhand)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAction(String string, Action action, Player player) {
		string = string.toUpperCase();
		if (string.equals("RIGHT_CLICK")) {
			if (action.equals(Action.RIGHT_CLICK_BLOCK) && !player.isSneaking()) {
				return true;
			}
			return false;
		}else if (string.equals("SHIFT_RIGHT_CLICK")) {
			if (action.equals(Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
				return true;
			}
			return false;
		}else {
			string = "SHIFT_RIGHT_CLICK";
			return isAction(string, action, player);
		}
	}
}