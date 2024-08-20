package nade.lemon.beehive.objects.upgrades;

import java.text.DecimalFormat;
import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.data.SoftDepends;
import nade.lemon.beehive.upgrades.Requirement;
import nade.lemon.beehive.utils.string.AdvancedSendMessage;
import nade.lemon.utils.bukkit.ItemStacks;

public class PaymentSystem {
    private static final ConfigBuild message = LemonBeehive.getInstance().get(Language.class).get("message");

    static void withdraw(Player player, Collection<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            if (!requirement.isValid()) continue;
            withdraw(player, requirement);
        }
    }

    private static void withdraw(Player player, Requirement requirement) {
        switch (requirement.getType()) {
            case "EXP": player.giveExp((int) - requirement.getValue(Number.class).doubleValue()); break;
            case "MONEY": SoftDepends.getEconomy().withdrawPlayer(player, requirement.getValue(Number.class).doubleValue()); break;
            case "ITEM":
                ItemStack item = requirement.getValue(ItemStack.class);
                int amount = item.getAmount();

                for (ItemStack slot : player.getInventory().getContents()) {
                    if (amount <= 0) break;
                    if (!ItemStacks.equals(item, slot)) continue;
                    if (slot.getAmount() >= amount) {
                        slot.setAmount(slot.getAmount() - amount);
                        amount = 0;
                    }else {
                        amount = amount - slot.getAmount();
                        slot.setAmount(0);
                    }
                }
                break;
            default: break;
        }
    }

    static boolean isEnough(Player player, Collection<Requirement> objects) {
        boolean isEnough = true;
        for (Requirement object : objects) {
            if (!object.isValid()) continue;
            if (!enough(player, object)) {
                String message = PaymentSystem.message.get("system.not-enough", String.class);
                String requirements = object.getType();
                String cost;
                if (object.getType().equalsIgnoreCase("ITEM")) {
                    ItemStack item = object.getValue(ItemStack.class);
                    requirements = object.getValue(ItemStack.class).getItemMeta().getDisplayName();
                    cost = "x" + item.getAmount();
                }else {
                    cost = new DecimalFormat("#,###").format(object.getValue(Number.class).doubleValue());
                }
                AdvancedSendMessage.message(player, true, message.replace("{amount}", cost).replace("{type}", requirements));
                isEnough = false;
            }
        }
        return isEnough;
    }

    private static boolean enough(Player player, Requirement requirement) {
        double balance = 0;
        double value = 0;
        switch (requirement.getType()) {
            case "EXP":
                balance = player.getTotalExperience();
                value = requirement.getValue(Number.class).doubleValue();
                break;
            case "MONEY":
                balance = SoftDepends.getEconomy().getBalance(player);
                value = requirement.getValue(Number.class).doubleValue();
                break;
            case "ITEM":
                ItemStack item = requirement.getValue(ItemStack.class);
                for (ItemStack slot : player.getInventory().getContents()) {
                    if (ItemStacks.equals(item, slot)) {
                        balance += slot.getAmount();
                    }
                }
                value = item.getAmount();
            default:
                break;
        }
        return (balance >= value);
    }
}
