package nade.lemon.beehive.features.harvest;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.configuration.simple.ConfigurationBuild;
import nade.empty.configuration.simple.ConfigurationSectionBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.chat.InputTypes;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.features.AdminInventory;
import nade.lemon.beehive.player.PlayerInputChat;
import nade.lemon.beehive.player.PlayerInputChat.InputObject;
import nade.lemon.beehive.utils.UUIDs;
import nade.lemon.beehive.utils.Utilities;
import nade.lemon.builders.Builders;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.Placeholder;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.head.CustomHead;
import nade.lemon.objects.MObjects;
import nade.lemon.utils.bukkit.ItemStacks;

public class HarvestEditor {
    
    private EmptyPlugin plugin;
    private HarvestSystem system;
    
    final Builders builders;

    private Language language;
    final ConfigBuild admin;
    final ConfigBuild message;

    private ConfigurationBuild config = BeehiveYamlConfig.getHarvest();

    public HarvestEditor(EmptyPlugin plugin, HarvestSystem system) {
        this.plugin = plugin;
        this.system = system;
        this.builders = plugin.get(Builders.class);
        this.language = plugin.get(Language.class);
        this.admin = this.language.get("admin");
        this.message = this.language.get("message");
    }
    
    public void open(HumanEntity player, int page) {
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(54).setTitle(admin.get("harvest-editor.title", String.class));

        build.setBorder("glass", ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));

        build.setItem(45, "back", ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(admin.get("global.back", String.class)));
        build.setItem(53, "create", ItemBuild.build(CustomHead.get("Green_Plus")).setDisplayName(admin.get("global.create", String.class)));

        for (Harvest harvest : this.system.values()) {
            ItemBuild item = ItemBuild.build(harvest.getTool());
            if (harvest.getTool().getType() == Material.BARRIER) {
                item.setDisplayName(admin.get("global.unset", String.class));
            }
            item.addLore("");
            item.addLore(admin.get("global.click-set", String.class));
            item.setItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            for (int i = 0; i < build.getSize(); i++) {
                if (!Objects.isNull(build.getItem(i))) continue;
                build.setItem(i, harvest.getUniqueId().toString(), item);
                break;
            }
        }

        build.setClickEvent((e) -> {
            e.setLocked(true);
            if (e.isKeyed("back")) plugin.get(AdminInventory.class).open(player);
            if (e.isKeyed("create")) this.openToolEditor(player, this.system.register());
            if (UUIDs.isUUID(e.getKeyed())) {
                this.openToolEditor(player, this.system.get(UUID.fromString(e.getKeyed())));
            }
        });

        build.open(player);
    }

    private void openToolEditor(HumanEntity player, Harvest harvest) {
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(9).setTitle(admin.get("harvest-editor.editor.title", String.class));

        MObjects properties = MObjects.create();

        build.setProperties("tool", harvest.getTool());
        build.setProperties("consumable", harvest.isConsumable());

        build.setItems(new int[] {2, 3, 5, 6}, "glass", ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
        build.setItem(0, "back", ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(admin.get("global.back", String.class)));
        build.setItem(1, "delete", ItemBuild.build(CustomHead.get("Red_X")).setDisplayName(admin.get("global.delete", String.class)));
        build.setItem(7, "consumable", ItemBuild.build(Material.SHEARS).setDisplayName(admin.get("harvest-editor.editor.consumable", String.class)));
        build.setItem(8, "drop", ItemBuild.build(CustomHead.get("Chest")).setDisplayName(admin.get("harvest-editor.editor.drop", String.class)));

        Placeholder placeholder = PlaceholderBuild.build();
        placeholder.set("{consumable}", () -> {
            return build.getProperties("consumable", Boolean.class) ? admin.get("global.enable", String.class) : admin.get("global.disable", String.class);
        });
        build.setPlaceholder(placeholder);

        ItemBuild tool = ItemBuild.build(harvest.getTool());
        if (tool.getMaterial() == Material.BARRIER) {
            tool.setDisplayName(admin.get("global.unset", String.class));
        }
        tool.addLore("");
        tool.addLore(admin.get("global.put-set", String.class));
        tool.setItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        build.setItem(4, "tool", tool);

        build.setKeyedEvent("glass", (e) -> {e.setLocked(true);});

        build.setKeyedEvent("back", (e) -> {
            e.setLocked(true);

            if (!Objects.isNull(build.getProperties("tool", ItemStack.class))) {
                harvest.setTool(build.getProperties("tool", ItemStack.class));
                config.set(harvest.getUniqueId() + ".tool", build.getProperties("tool", ItemStack.class));
            }
            config.set(harvest.getUniqueId() + ".consumable", build.getProperties("consumable", Boolean.class));
            harvest.setConsumable(build.getProperties("consumable", Boolean.class));
            config.save();

            properties.set("saved", true);

            this.open(e.getPlayer(), 0);
        });

        build.setKeyedEvent("delete", (e) -> {
            e.setLocked(true);
            this.system.unregister(harvest.getUniqueId());
            properties.set("saved", true);
            this.open(e.getPlayer(), 0);
        });

        build.setKeyedEvent("tool", (e) -> {
            e.setLocked(true);
            if (!Objects.isNull(e.getEvent().getCursor()) && e.getEvent().getCursor().getType() != Material.AIR) {
                ItemStack cursor = ItemStacks.newItemStack(e.getEvent().getCursor());
                cursor.setAmount(1);
                build.setProperties("tool", cursor);
                ItemBuild cursorBuild = ItemBuild.build(cursor);
                cursorBuild.addLores("", admin.get("global.put-set", String.class));
                cursorBuild.setItemFlag(ItemFlag.HIDE_ATTRIBUTES);
                e.setItem(e.getRawSlot(), cursorBuild.getItem());
            }
        });

        build.setKeyedEvent("drop", (e) -> {
            e.setLocked(true);
            this.openDropEditor(e.getPlayer(), harvest);
        });

        build.setKeyedEvent("consumable", (e) -> {
            e.setLocked(true);
            build.setProperties("consumable", !build.getProperties("consumable", Boolean.class));
            ItemBuild consumable = build.getItemBuild(e.getRawSlot());
            consumable.setPlaceholder(PlaceholderBuild.build().set("{consumable}", build.getProperties("consumable", Boolean.class) ? admin.get("global.enable", String.class) : admin.get("global.disable", String.class)));
            e.setItem(e.getRawSlot(), consumable.getItem());
        });

        build.setClosedEvent((e) -> {
            if (!properties.getOrDefault("saved", false, Boolean.class)) {
                if (!Objects.isNull(build.getProperties("tool", ItemStack.class))) {
                    harvest.setTool(build.getProperties("tool", ItemStack.class));
                    config.set(harvest.getUniqueId() + ".tool", build.getProperties("tool", ItemStack.class));
                }
                config.set(harvest.getUniqueId() + ".consumable", build.getProperties("consumable", Boolean.class));
                harvest.setConsumable(build.getProperties("consumable", Boolean.class));
                config.save();
            }
        });

        build.open(player);
    }

    private void openDropEditor(HumanEntity player, Harvest harvest) {
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(54).setTitle(admin.get("harvest-editor.editor.drop-editor.title", String.class));

        build.setItems(new int[] {46, 47, 48, 49, 50, 51, 52, 53}, "air", ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
        build.setItem(45, "back", ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(admin.get("global.back", String.class)));

        if (!Objects.isNull(config.getSection(harvest.getUniqueId() + ".drops"))) {
            ConfigurationSectionBuild section = config.getSection(harvest.getUniqueId() + ".drops");

            DecimalFormat decimalFormat = new DecimalFormat("0.###");

             for (String key : section.getKeys()) {
                ItemBuild item = ItemBuild.build();
                item.setProperties("rate", section.getOrDefault(key + ".rate", 0d, Double.class));
                item.setProperties("item", section.get(key + ".item", ItemStack.class));

                PlaceholderBuild placeholder = PlaceholderBuild.build();
                placeholder.set("{rate}", () -> {
                    return decimalFormat.format(item.getProperties("rate", 0d, Double.class) * 100) + "&f%";
                });

                item.setPlaceholder(placeholder);
                build.setItem(Utilities.getInt(key.replace("s-", "")), system.getEvent().getItem(item));
            }
        }

        build.setKeyedEvent("air", (e) -> e.setLocked(true));
        build.setKeyedEvent("back", (e) -> {
            this.openToolEditor(e.getPlayer(), harvest);
        });

        build.setClickEvent((e) -> {
            InventoryClickEvent event = e.getEvent();
            int rawSlot = e.getRawSlot();

            if ((event.getClick() == ClickType.SHIFT_LEFT) || event.getClick() == ClickType.SHIFT_RIGHT || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) {
                e.setLocked(true);
                if (event.getClick() == ClickType.SHIFT_LEFT && (rawSlot >= 0 && rawSlot <= 44) && !Objects.isNull(build.getItemBuild(rawSlot))) {
                    ItemBuild rawItem = build.getItemBuild(rawSlot);

                    InputObject[] inputs = {PlayerInputChat.input("inventory", build), PlayerInputChat.input("raw-slot", rawSlot), PlayerInputChat.input("raw-item", rawItem)};
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        PlayerInputChat.onEnterInputChat(InputTypes.RewardRate, event.getWhoClicked(), message.get("input.message", String.class), inputs);
                    }, 0);  
                }
                return;
            }

            if (rawSlot >= 0 && rawSlot <= 44) {
                Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), () -> {
                    switch (event.getAction()) {
                        case PICKUP_ALL: system.getEvent().onPickupAll(rawSlot, e);
                            break;
                        case PICKUP_HALF: system.getEvent().onPickupHalf(rawSlot, e);
                            break;
                        case PLACE_ALL: system.getEvent().onPlaceAll(rawSlot, e);
                            break;
                        case PLACE_ONE: system.getEvent().onPlaceOne(rawSlot, e);
                            break;
                        case SWAP_WITH_CURSOR: system.getEvent().onSwap(rawSlot, e);
                            break;
                        default:
                            break;
                    }
                }, 0);
            }
        });

        build.setDragEvent(listener -> {
            listener.getEvent().setCancelled(true);
            return;
        });

        build.setClosedEvent(listener -> {
            Bukkit.getScheduler().runTaskLater(LemonBeehive.getInstance(), () -> {
                if (InputTypes.RewardRate.has(listener.getPlayer())) return;
                harvest.getDrops().clear();
                for (int i = 0; i < 44; i++) {
                    ItemBuild item = build.getItemBuild(i);
                    if (Objects.isNull(item) || item.getItem().getType() == Material.AIR) {
                        config.set(harvest.getUniqueId() + "drops" + ".s-" + i, null);
                        continue;
                    }
                    double rate = item.getProperties("rate", Double.class);
                    ItemStack drop = item.getProperties("item", ItemStack.class);

                    harvest.getDrops().addElement(rate, drop);

                    config.set(harvest.getUniqueId() + ".drops" + ".s-" + i + ".rate", rate);
                    config.set(harvest.getUniqueId() + ".drops" + ".s-" + i + ".item", drop);
                }
                config.save();
            }, 0);
            return;
        });

        build.open(player);
    }
}
