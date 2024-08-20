package nade.lemon.beehive.features.manager;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Bee;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.features.AdminInventory;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.utils.UUIDs;
import nade.lemon.builders.Builders;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.inventory.InventoryBuilder;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.head.CustomHead;
import nade.lemon.head.HeadObject;

public class ManageSystem {

    private EmptyPlugin plugin;
    private InventoryBuilder builder;

    private final ConfigBuild admin;

    public ManageSystem(EmptyPlugin plugin) {
        this.plugin = plugin;
        this.builder = plugin.get(Builders.class).getInventoryBuilder();

        this.admin = plugin.get(Language.class).get("admin");
    }

    public void open(HumanEntity player, int page) {
        InventoryBuild build = builder.register("").setSize(54).setTitle(admin.get("beehive-manage.owner-list.title", String.class));

        ItemBuild glass = ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuild back = ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(admin.get("global.back", String.class));

        ItemBuild next = ItemBuild.build(CustomHead.get("Quartz_Arrow_Right")).setDisplayName(admin.get("global.next", String.class));
        ItemBuild previous = ItemBuild.build(CustomHead.get("Quartz_Arrow_Left")).setDisplayName(admin.get("global.previous", String.class));

        build.setItems(new int[] {
            0,  1,  2,  3,  4,  5,  6,  7,  8,
            9,                              17,
            18,                             26,
            27,                             35,
            36,                             44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
        }, glass);

        build.setItem(45, "back", back);

        List<UUID> owners = Lists.newArrayList(getOwners());
        for (int i = 0; i < owners.size(); i++) {
            if ((i < (page*28)) || (i >= ((page+1)*28))) continue;
            OfflinePlayer owner = Bukkit.getOfflinePlayer(owners.get(i));
            ItemBuild item = ItemBuild.build(Material.PLAYER_HEAD);
            for (int slot = 0; slot < build.getSize(); slot++) {
                if (!Objects.isNull(build.getItem(slot))) continue;

                PlaceholderBuild placeholder = PlaceholderBuild.build();
                placeholder.set("{owner-displayname}", owner.getName());
                item.setPlaceholder(placeholder);

                item.applyBySection(admin.getSection("beehive-manage.owner-list"));
                build.setItem(slot, owner.getUniqueId().toString(), item); //đã hoàn thành beehive manage, vẫn chưa áp dụng vào /beehive admin
                break;
            }
        }

        if (page > 0) {
            build.setItem(52, "previous", previous);
        }
        if (owners.size() >= ((page+1)*28)) {
            build.setItem(53, "next", next);
        }

        build.setClickEvent((e) -> {
            e.setLocked(true);
            if (Objects.isNull(e.getKeyed())) return;
            if (e.getKeyed().equals("previous")) {
                open(player, page-1);
            }
            if (e.getKeyed().equals("next")) {
                open(player, page+1);
            }
            if (e.isKeyed("back")) {
                LemonBeehive.getInstance().get(AdminInventory.class).open(e.getPlayer());
            }
            if (UUIDs.isUUID(e.getKeyed())) {
                openManage(player, UUID.fromString(e.getKeyed()), 0);
            }
        });

        build.setOpenedEvent((e) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < build.getSize(); i++) {
                    if (Objects.isNull(e.getKeyed(i)) || !UUIDs.isUUID(e.getKeyed(i))) continue;
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(e.getKeyed(i)));
                    HeadObject head = CustomHead.getByPlayer(build.getItem(i), owner);
                    e.getInventory().setItem(i, head.getItem(owner.getName()));
                }
            }, 0);
        });

        build.open(player);
    }

    private void openManage(HumanEntity player, UUID uniqueId, int page) {
        InventoryBuild build = builder.register("").setSize(54).setTitle(admin.getOrDefault("beehive-manage.manages.title", "", String.class));

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{owner-displayname}", Bukkit.getOfflinePlayer(uniqueId).getName());
        build.setPlaceholder(placeholder);

        ItemBuild glass = ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuild back = ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(admin.get("global.back", String.class));
        ItemBuild clear = ItemBuild.build(CustomHead.get("Red_X")).setDisplayName(admin.get("beehive-manage.manages.clear", String.class));

        ItemBuild next = ItemBuild.build(CustomHead.get("Quartz_Arrow_Right")).setDisplayName(admin.get("global.next", String.class));
        ItemBuild previous = ItemBuild.build(CustomHead.get("Quartz_Arrow_Left")).setDisplayName(admin.get("global.previous", String.class));

        build.setItems(new int[] {
            0,  1,  2,  3,  4,  5,  6,  7,  8,
            9,                              17,
            18,                             26,
            27,                             35,
            36,                             44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
        }, glass);

        build.setItem(45, "back", back);
        build.setItem(46, "clear", clear);

        List<BeehiveObject> beehives = Lists.newArrayList(Database.getByOwner(uniqueId));
        
        for (int i = 0; i < beehives.size(); i++) {
            if (i < (page*28) || i > (page+1)*28) continue;
            for (int slot = 0; slot < build.getSize(); slot++) {
                if (!Objects.isNull(build.getItem(slot))) continue;
                ItemBuild item = ItemBuild.build(Material.BEEHIVE);
                BeehiveObject beehive = beehives.get(i);
                PlaceholderBuild itemPlaceholder = PlaceholderBuild.build();
                itemPlaceholder.set("{beehive-displayname}", beehive.getDisplayName());
                itemPlaceholder.set("{beehive-world}", beehive.getWorld().getName());
                itemPlaceholder.set("{beehive-x}", beehive.getLocation().getBlockX());
                itemPlaceholder.set("{beehive-y}", beehive.getLocation().getBlockY());
                itemPlaceholder.set("{beehive-z}", beehive.getLocation().getBlockZ());
                item.setPlaceholder(itemPlaceholder);
                item.applyBySection(admin.getSection("beehive-manage.manages.element"));
                build.setItem(slot, beehive.getUniqueId().toString(), item);
                break;
            }
        }

        if (page > 0) {
            build.setItem(52, "previous", previous);
        }
        if (beehives.size() > ((page+1)*28)) {
            build.setItem(53, "next", next);
        }

        build.setClickEvent((e) -> {
            e.setLocked(true);
            if (Objects.isNull(e.getKeyed())) return;
            switch (e.getKeyed()) {
                case "previous": this.openManage(player, uniqueId, page-1);
                    break;
                case "next": this.openManage(player, uniqueId, page+1);
                    break;
                case "back": this.open(player, 0);
                    break;
                case "clear": 
                    Lists.newArrayList(Database.getByOwner(uniqueId)).forEach((beehive) -> this.delete(beehive));
                    this.open(e.getPlayer(), 0);
                    break;
                default:
                    break;
            }
            if (UUIDs.isUUID(e.getKeyed())) {
                if (e.getClickType() == ClickType.SHIFT_LEFT) {
                    BeehiveObject beehive = Database.getByUniqueId(UUID.fromString(e.getKeyed()));
                    this.delete(beehive);
                    this.openManage(player, uniqueId, page);
                }
            }
        });

        build.open(player);
    }

    private Set<UUID> getOwners() {
        Set<UUID> results = Sets.newHashSet();

        for (BeehiveObject beehive : Database.getBeehives()) {
            results.add(beehive.getOwner().getUniqueId());
        }
        
        return results;
    }

    public void delete(BeehiveObject beehive) {
        for (Bee bee : beehive.getHiveBees().getLeaves()) {
            bee.remove();
        }
        beehive.getLocation().getBlock().setType(Material.AIR);
        Database.removeByUniqueId(beehive.getUniqueId());
    }
}
