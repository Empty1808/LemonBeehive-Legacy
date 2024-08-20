package nade.lemon.beehive.objects.ally;

import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

public class BeehiveAlly {
    private Set<UUID> allys = Sets.newHashSet();

    public BeehiveAlly(UUID... allys) {
        for (UUID uuid : allys) {   
            this.allys.add(uuid);
        }
    };

    public void add(UUID... uuids) {
        for (UUID uuid : uuids) {
            this.allys.add(uuid);
        }
    }

    public void add(OfflinePlayer... offlinePlayers) {
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            this.allys.add(offlinePlayer.getUniqueId());
        }
    }

    public void add(Player... players) {
        for (Player player : players) {
            this.allys.add(player.getUniqueId());
        }
    }

    public void add(String... strings) {
        for (String string : strings) {
            this.allys.add(UUID.fromString(string));
        }
    }

    public void remove(UUID... uuids) {
        for (UUID uuid : uuids) {
            this.allys.remove(uuid);
        }
    }

    public void remove(Player... players) {
        for (Player player : players) {
            this.allys.remove(player.getUniqueId());
        }
    }

    public void remove(String... strings) {
        for (String string : strings) {
            this.allys.remove(UUID.fromString(string));
        }
    }

    public Set<UUID> getAllys() {
        return Sets.newHashSet(allys);
    }

    public boolean contains(UUID uuid) {
        return allys.contains(uuid);
    }

    public boolean contains(OfflinePlayer offlinePlayer) {
        return allys.contains(offlinePlayer.getUniqueId());
    }

    public boolean contains(Player player) {
        return allys.contains(player.getUniqueId());
    }
}
