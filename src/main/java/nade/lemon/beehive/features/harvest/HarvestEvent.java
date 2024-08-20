package nade.lemon.beehive.features.harvest;

import java.text.DecimalFormat;
import java.util.Objects;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.chat.InputTypes;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.listeners.event.custom.PlayerInputChatEvent;
import nade.lemon.beehive.utils.Utilities;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.inventory.InventoryBuilder;
import nade.lemon.builders.inventory.events.ClickedListener;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.utils.Logger;

public class HarvestEvent {
 
    private Logger logger;
    private InventoryBuilder builder;

    private final ConfigBuild admin;
    private final ConfigBuild message;

    public HarvestEvent(EmptyPlugin plugin, HarvestSystem system) {
        this.logger = plugin.get(Logger.class);
        this.builder = system.getEditor().builders.getInventoryBuilder();

        this.admin = plugin.get(Language.class).get("admin");
        this.message = plugin.get(Language.class).get("message");
    }

    @SuppressWarnings("deprecation")
    void onPickupAll(int rawSlot, ClickedListener e) {
        ItemBuild rawItem = e.getItemBuild(rawSlot);
        if (!Objects.isNull(rawItem)) {
            e.getEvent().setCursor(rawItem.getProperties("item", ItemStack.class));
            e.getBuild().removeItem(rawSlot);
        }
    }

    @SuppressWarnings("deprecation")
    void onPickupHalf(int rawSlot, ClickedListener e) {
        ItemBuild rawItem = e.getItemBuild(rawSlot);
        if (!Objects.isNull(rawItem)) {
            ItemStack item = rawItem.getProperties("item", ItemStack.class);
            if (item.getAmount() > 1) {
                item.setAmount(rawItem.getItem().getAmount()/2);
            }
            e.getEvent().setCursor(item);
        }
    }

    void onPlaceAll(int rawSlot, ClickedListener e) {
        ItemBuild rawItem = ItemBuild.build();
        rawItem.setProperties("rate", 1.0);
        rawItem.setProperties("item", e.getEvent().getCurrentItem());
        if (!Objects.isNull(rawItem)) {
            e.getBuild().setItem(rawSlot, rawItem);
            e.getEvent().setCurrentItem(this.getItem(rawItem).getItem());
        }
    }

    void onPlaceOne(int rawSlot, ClickedListener e) {
        this.onPlaceAll(rawSlot, e);
    }

    @SuppressWarnings("deprecation")
    void onSwap(int rawSlot, ClickedListener e) {
        ItemBuild rawItem = ItemBuild.build();
        rawItem.setProperties("rate", 1.0);
        rawItem.setProperties("item", e.getEvent().getCurrentItem());
        ItemBuild oldItem = e.getBuild().getItemBuild(rawSlot);
        if (!Objects.isNull(rawItem)) {
            e.getEvent().setCurrentItem(this.getItem(rawItem).getItem());
        }
        if (!Objects.isNull(oldItem)) {
            e.getEvent().setCursor(oldItem.getProperties("item", ItemStack.class));
        }
    }

    public void onPlayerInputChat(PlayerInputChatEvent e) {
        if (e.getInputTypes() != InputTypes.RewardRate) return;
        HumanEntity player = e.getPlayer();
        InventoryBuild build = e.getInputCompound().get("inventory", InventoryBuild.class);
        ItemBuild rawItem = e.getInputCompound().get("raw-item", ItemBuild.class);
        switch (e.getType()) {
            case Enter:
                player.closeInventory();
                break;
            case Cancel:
                if (!Objects.isNull(build)) {
                    builder.register(build);
                    build.open(player);
                }
                break;
            case Completed:
                String message = e.getMessage();
                if (!Utilities.isDoubles(message)) {
                    e.setCancelled(true);
                    logger.sendInfo(e.getPlayer(), this.message.get("input.invalid", String.class).replace("{value}", message));
                    return;
                }
                rawItem.setProperties("rate", (Utilities.getDouble(message)/100));
                build.open(player);
                break;
            default:
                break;
        }
    }

    private DecimalFormat decimal = new DecimalFormat("0.###");

    ItemBuild getItem(ItemBuild item) {
        item.setItem(item.getProperties("item", ItemStack.class));
        item.addLore(" ");
        item.addLore(admin.get("harvest-editor.editor.drop-editor.rate", String.class));
        item.addLore(" ");
        item.addLore(admin.get("global.shift-right-set", String.class));

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{rate}", () -> {
            return decimal.format(item.getProperties("rate", 0d, Double.class) * 100) + "&f%";
        });

        item.setPlaceholder(placeholder);
        item.setItemFlag(ItemFlag.HIDE_ATTRIBUTES);
        return item;
    }

}
