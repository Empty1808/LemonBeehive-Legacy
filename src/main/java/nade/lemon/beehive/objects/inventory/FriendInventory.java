package nade.lemon.beehive.objects.inventory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;

import com.google.common.collect.Lists;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.utils.UUIDs;
import nade.lemon.builders.Builders;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.head.CustomHead;
import nade.lemon.head.HeadObject;

public class FriendInventory {
    private Friend friend;

    public FriendInventory(EmptyPlugin plugin) {
        this.friend = new Friend(plugin);
    }

    public void open(HumanEntity player, BeehiveObject beehive) {
        friend.open(player, beehive, 0, Sort.ALL);
    }
}

abstract class Inventory {
    protected final EmptyPlugin plugin;
    protected final Builders builders;

    protected final Language LANGUAGE;

    protected final ConfigBuild gui;

    protected Inventory(EmptyPlugin plugin) {
        this.plugin = plugin;
        this.builders = plugin.get(Builders.class);

        this.LANGUAGE = plugin.get(Language.class);
        this.gui = LANGUAGE.get("gui");
    }
}

enum Sort {
    OFFLINE,
    ONLINE,
    ALL
}

class Friend extends Inventory {
    private final AddFriend addFriend;

    public Friend(EmptyPlugin plugin) {
        super(plugin);
        
        this.addFriend = new AddFriend(plugin, this);
    }

    public void open(HumanEntity player, BeehiveObject beehive, int page, Sort sort) {
        InventoryBuild build = this.builders.getInventoryBuilder().register("").setSize(54).setTitle("");
        
        build.setBorder(ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
        build.setItem(4, ItemBuild.build(CustomHead.get("Kirby_Surprised")).setDisplayName(gui.get("friend.info", String.class)));
        build.setItem(45, "back", ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(gui.get("global.back", String.class)));
        build.setItem(46, "sort", ItemBuild.build(CustomHead.get("Cyan_Reverse_Exclamation_Mark")).setDisplayName(gui.get("friend.sort", String.class)));
        build.setItem(53, "add", ItemBuild.build(CustomHead.get("Green_Plus")).setDisplayName(gui.get("global.add", String.class)));

        List<String> friends = Lists.newArrayList(beehive.getFriends());

        for (String string : Lists.newArrayList(friends)) {
            OfflinePlayer _player = Bukkit.getOfflinePlayer(UUIDs.fromString(string));
            if (beehive.isOwner(_player.getUniqueId())) friends.remove(string);
            if (sort == Sort.OFFLINE && _player.isOnline()) friends.remove(string);
            if (sort == Sort.ONLINE && !_player.isOnline()) friends.remove(string);
        }

        for (int i = 0; i < friends.size(); i++) {
            if ((i < (page*28)) || (i >= ((page+1)*28))) continue;
            OfflinePlayer _player = Bukkit.getOfflinePlayer(UUIDs.fromString(friends.get(i)));
            ItemBuild item = ItemBuild.build(Material.PLAYER_HEAD);
            item.setDisplayName(gui.get("friend.element", String.class));
            item.setPlaceholder(PlaceholderBuild.build().set("{player-name}", _player.getName()));

            build.addItem(_player.getUniqueId().toString(), item);
        }

        if (page > 0) build.setItem(52, ItemBuild.build(CustomHead.get("Quartz_Arrow_Left")).setDisplayName(gui.get("global.previous", String.class)));
        if (friends.size() > ((page+1)*28)) build.setItem(53, ItemBuild.build(CustomHead.get("Quartz_Arrow_Right")).setDisplayName(gui.get("global.next", String.class)));

        build.setClickEvent((e) -> {
            e.setLocked(true);

            if (e.isKeyed("back")) beehive.getMenu().open(player);
            if (e.isKeyed("add")) addFriend.open(player, beehive, 0, Sort.ALL);

            if (UUIDs.isUUID(e.getKeyed())) {
                beehive.removeFriend(UUIDs.fromString(e.getKeyed()));
                this.open(player, beehive, page, sort);
            }
            if (e.isKeyed("sort")) {
                if (sort == Sort.ALL) this.open(player, beehive, page, Sort.ONLINE);
                if (sort == Sort.ONLINE) this.open(player, beehive, page, Sort.OFFLINE);
                if (sort == Sort.OFFLINE) this.open(player, beehive, page, Sort.ALL);
            }
        });

        build.setOpenedEvent((e) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < build.getSize(); i++) {
                    if (Objects.isNull(e.getKeyed(i)) || !UUIDs.isUUID(e.getKeyed(i))) continue;
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(e.getKeyed(i)));
                    HeadObject head = CustomHead.getByPlayer(build.getItem(i), owner);
                    e.getInventory().setItem(i, head.getItem(gui.get("friend.add-friend.element", String.class).replace("{player-name}", owner.getName())));
                }
            }, 0);
        });

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{sort}", gui.get("friend.sort-type." + sort.name().toLowerCase(), String.class));

        build.setPlaceholder(placeholder);
        build.open(player);
    }
}

class AddFriend extends Inventory {
    private final Friend friend;

    public AddFriend(EmptyPlugin plugin, Friend friend) {
        super(plugin);

        this.friend = friend;
    }

    public void open(HumanEntity player, BeehiveObject beehive, int page, Sort sort) {
        InventoryBuild build = this.builders.getInventoryBuilder().register("").setSize(54).setTitle("");

        build.setBorder(ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
        build.setItem(4, ItemBuild.build(CustomHead.get("Kirby_Surprised")).setDisplayName(gui.get("friend.add-friend.info", String.class)));
        build.setItem(45, "back", ItemBuild.build(CustomHead.get("Red_Backward_2")).setDisplayName(gui.get("global.back", String.class)));
        build.setItem(46, "sort", ItemBuild.build(CustomHead.get("Cyan_Reverse_Exclamation_Mark")).setDisplayName(gui.get("friend.sort", String.class)));
        
        List<OfflinePlayer> players = Lists.newLinkedList();

        if (sort == Sort.ALL) {
            players.addAll(Lists.newArrayList(Bukkit.getOfflinePlayers()));
            players.addAll(Lists.newArrayList(Bukkit.getOnlinePlayers()));
        }
        if (sort == Sort.ONLINE) players.addAll(Lists.newArrayList(Bukkit.getOnlinePlayers()));
        if (sort == Sort.OFFLINE) players.addAll(Lists.newArrayList(Bukkit.getOfflinePlayers()));

        Collection<String> friends = beehive.getFriends();

        for (OfflinePlayer _player : Lists.newArrayList(players)) {
            if (friends.contains(_player.getUniqueId().toString())) {
                players.remove(_player);
            }
        }

        for (int i = 0; i < players.size(); i++) {
            if ((i < (page*28)) || (i > ((page+1)*28))) continue;
            OfflinePlayer _player = players.get(i);
            ItemBuild item = ItemBuild.build(Material.PLAYER_HEAD);
            item.setDisplayName(gui.get("friend.add-friend.element", String.class));
            item.setPlaceholder(PlaceholderBuild.build().set("{player-name}", _player.getName()));

            build.addItem(_player.getUniqueId().toString(), item);
        }

        if (page > 0) build.setItem(52, ItemBuild.build(CustomHead.get("Quartz_Arrow_Left")).setDisplayName(gui.get("global.previous", String.class)));
        if (friends.size() > ((page+1)*28)) build.setItem(53, ItemBuild.build(CustomHead.get("Quartz_Arrow_Right")).setDisplayName(gui.get("global.next", String.class)));

        build.setClickEvent((e) -> {
            e.setLocked(true);
            if (e.isKeyed("back")) this.friend.open(player, beehive, 0, Sort.ALL);
            
            if (UUIDs.isUUID(e.getKeyed())) {
                beehive.addFriend(UUIDs.fromString(e.getKeyed()));
                this.open(player, beehive, page, sort);
            }

            if (e.isKeyed("sort")) {
                if (sort == Sort.ALL) this.open(player, beehive, page, Sort.ONLINE);
                if (sort == Sort.ONLINE) this.open(player, beehive, page, Sort.OFFLINE);
                if (sort == Sort.OFFLINE) this.open(player, beehive, page, Sort.ALL);
            }
        });

        build.setOpenedEvent((e) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < build.getSize(); i++) {
                    if (Objects.isNull(e.getKeyed(i)) || !UUIDs.isUUID(e.getKeyed(i))) continue;
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(e.getKeyed(i)));
                    HeadObject head = CustomHead.getByPlayer(build.getItem(i), owner);
                    e.getInventory().setItem(i, head.getItem(gui.get("friend.add-friend.element", String.class).replace("{player-name}", owner.getName())));
                }
            }, 0);
        });

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{sort}", gui.get("friend.sort-type." + sort.name().toLowerCase(), String.class));

        build.setPlaceholder(placeholder);
        build.open(player);
    }
}
