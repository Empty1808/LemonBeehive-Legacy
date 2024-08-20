package nade.lemon.beehive.utils.bukkit.item;

import java.util.Map;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import nade.empty.configuration.serialization.ConfigurationSerializable;

public class EItemMeta implements ConfigurationSerializable, org.bukkit.configuration.serialization.ConfigurationSerializable{
    
    private ItemMeta meta;

    private EItemMeta(ItemMeta meta) {
        this.meta = meta;
    }

    public ItemMeta getMeta() {
        return meta;
    }

    @Override
    public Map<String, Object> serialize() {
        return meta.serialize();
    }

    public static String encode(ItemStack item) {
        try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return new String(Base64Coder.encode(outputStream.toByteArray()));
        } catch (Exception exception) {
            return "";
        }
    }
}
