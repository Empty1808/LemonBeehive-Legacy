package nade.lemon.beehive.features.editors.upgrades;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.plugin.EmptyPlugin;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.features.AdminInventory;
import nade.lemon.beehive.upgrades.Upgrade;
import nade.lemon.beehive.upgrades.Requirement;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.beehive.utils.Utilities;
import nade.lemon.builders.Builders;
import nade.lemon.builders.input.chat.ChatInputBuild;
import nade.lemon.builders.inventory.InventoryBuild;
import nade.lemon.builders.item.ItemBuild;
import nade.lemon.builders.placeholder.Placeholder;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.head.CustomHead;
import nade.lemon.utils.Logger;
import nade.lemon.utils.bukkit.ItemStacks;

public class UpgradeEditor{
    private CategoryEditor category;

    public UpgradeEditor(EmptyPlugin plugin) {
        category = new CategoryEditor(plugin);
    }

    public void openCategory(HumanEntity player, String category) {
        this.category.openCategory(player, category);
    }
}

class Editor {
    protected final EmptyPlugin plugin;
    protected final Builders builders;

    protected final ConfigBuild language;
    protected final ConfigBuild admin;
    protected final ConfigBuild message;

    protected final UpgradeSystem system;

    protected final Logger logger;

    public Editor(EmptyPlugin plugin) {
        this.plugin = plugin;
        this.builders = plugin.get(Builders.class);

        this.language = plugin.get(Language.class).get("language");
        this.admin = plugin.get(Language.class).get("admin");
        this.message = plugin.get(Language.class).get("message");

        this.system = plugin.get(UpgradeSystem.class);

        this.logger = plugin.get(Logger.class);
    }
}

class CategoryEditor extends Editor {
    private final ElementEditor element;

    public CategoryEditor(EmptyPlugin plugin) {
        super(plugin);

        this.element = new ElementEditor(plugin, this);
    }

    public void openCategory(HumanEntity player, String category) {
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(45).setTitle(admin.get("upgrade-editor.category.title", String.class));
        
        ItemBuild glass = ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuild back = ItemBuild.build(CustomHead.get("red-backward-2")).setDisplayName(admin.get("global.back", String.class));
        ItemBuild redGlass = ItemBuild.build(Material.RED_STAINED_GLASS_PANE).setDisplayName(admin.get("upgrade-editor.locked", String.class));

        ItemBuild beehives = ItemBuild.build(Material.BEE_NEST).setDisplayName(admin.get("upgrade-editor.category.beehives", String.class));
        if (category.equalsIgnoreCase("beehive")) {
            beehives.setEnchantment(Enchantment.DURABILITY, 1);
            beehives.setItemFlag(ItemFlag.HIDE_ENCHANTS);
        }
        ItemBuild bees = ItemBuild.build(Material.HONEYCOMB_BLOCK).setDisplayName(admin.get("upgrade-editor.category.bees", String.class));
        if (category.equalsIgnoreCase("bees")) {
            bees.setEnchantment(Enchantment.DURABILITY, 1);
            bees.setItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{category}", language.get("parameter." + category, String.class));

        build.setBorder(glass);
        build.setItems(new int[] {28, 34}, glass);

        build.setItems(new int[] {31, 32, 33}, redGlass);

        build.setItem(29, "beehive-category", beehives);
        build.setItem(30, "bees-category", bees);

        build.setItem(36, "back", back);

        for (UpgradeType type : system.keySet()) {
            if (!type.getCategory().equalsIgnoreCase(category)) continue;
            ItemBuild item = ItemBuild.build(type.getIcon());
            item.setDisplayName(admin.get("upgrade-editor.category.element", String.class).replace("{upgrade-type}", type.getName()));
            item.setItemFlag(ItemFlag.values());
            build.addItem(type.getType(), item);
        }

        build.setClickEvent((listener) -> {
            listener.setLocked(true);

            if (Objects.isNull(listener.getKeyed())) return;
            if (listener.isKeyed("back")) plugin.get(AdminInventory.class).open(player);
            if (listener.isKeyed("beehive-category") || listener.isKeyed("bees-category")) {
                if (!category.equalsIgnoreCase(listener.getKeyed().replace("-category", ""))) {
                    this.openCategory(player, listener.getKeyed().replace("-category", ""));
                }
                return;
            }

            if (UpgradeType.contains(listener.getKeyed())) {
                this.openUpgrades(player, UpgradeType.valueOf(listener.getKeyed()), 0);
            }
        });

        build.setClosedEvent((listener) -> {
            builders.getInventoryBuilder().remove(build.getUniqueId());
        });

        build.setPlaceholder(placeholder);
        build.open(player);
    }

    void openUpgrades(HumanEntity player, UpgradeType type, int page) {
        InventoryBuild build = builders.getInventoryBuilder().register("upgrade-editor.upgrades").setSize(54).setTitle(admin.get("upgrade-editor.upgrades.title", String.class));

        ItemBuild redGlass = ItemBuild.build(Material.RED_STAINED_GLASS_PANE).setDisplayName(admin.get("upgrade-editor.locked", String.class));

        ItemBuild back = ItemBuild.build(CustomHead.get("red-backward-2")).setDisplayName(admin.get("global.back", String.class));
        
        ItemBuild create = ItemBuild.build(CustomHead  .get("green-plus")).setDisplayName(admin.get("global.create", String.class));

        ItemBuild previous = ItemBuild.build(CustomHead.get("quartz-arrow-left")).setDisplayName(admin.get("global.previous", String.class));
        ItemBuild next = ItemBuild.build(CustomHead.get("quartz-arrow-right")).setDisplayName(admin.get("global.next", String.class));

        build.setBorder(ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{upgrade-type}", language.get("parameter." + type.getType(), String.class));

        system.forEach(type, (upgrade) -> {
            if (upgrade.getLevel() <= (page *28)) return;
            ItemBuild item = ItemBuild.build(type.getIcon());
            item.setDisplayName(admin.get("upgrade-editor.upgrades.element", String.class));
            PlaceholderBuild itemPlaceholder = PlaceholderBuild.build();
            itemPlaceholder.set("{upgrade-level}", upgrade.getLevel());
            item.setPlaceholder(itemPlaceholder);
            item.setItemFlag(ItemFlag.values());
            build.addItem(String.valueOf(upgrade.getLevel()), item);
        });

        for (int i = 0; i < build.getSize(); i++) {
            if (Objects.isNull(build.getItem(i))) {
                build.setItem(i, redGlass);
            }
        }

        build.setItem(53, "create", create);
        build.setItem(45, "back", back);
        if (page > 0) build.setItem(51, "previous", previous);
        if (system.size(type) > ((page+1) * 28)) build.setItem(52, "next", next);
        

        build.setClickEvent((listener) -> {
            listener.setLocked(true);
            if (listener.getRawSlot() < 0) return;
            if (listener.isKeyed("previous")) this.openUpgrades(player, type, page-1);
            else if (listener.isKeyed("next")) this.openUpgrades(player, type, page+1);
            else if (listener.isKeyed("create")) {
                system.add(type);
                element.open(player, system.last(type));
            }
            else if (listener.isKeyed("back")) this.openCategory(player, type.getCategory());
            else if (!Objects.isNull(listener.getKeyed())) {
                this.element.open(player, system.getByLevel(type, Utilities.getInt(listener.getKeyed())));
            }
        });

        build.setPlaceholder(placeholder);
        build.open(player);
    }

}

class ElementEditor extends Editor{
    private CategoryEditor category;
    private RequirementEditor requirement;

    public ElementEditor(EmptyPlugin plugin, CategoryEditor category) {
        super(plugin);

        this.category = category;
        this.requirement = new RequirementEditor(plugin, this);
    }

    public void open(HumanEntity player, Upgrade upgrade) {
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(9).setTitle(admin.get("upgrade-editor.upgrades.editor.title", String.class));

        PlaceholderBuild placeholder = PlaceholderBuild.build();
        placeholder.set("{upgrade-type}", language.get("parameter." + upgrade.getType().getType()));
        placeholder.set("{upgrade-level}", upgrade.getLevel());
        placeholder.set("{value}", () -> new DecimalFormat("#.###").format(build.getProperties("value", 0, Number.class)));

        build.setProperties("value", upgrade.getOrDefault("value", 0, Number.class));

        build.setBorder(ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));

        build.setItem(0,"back", ItemBuild.build(CustomHead.get("red-backward-2")).setDisplayName(admin.get("global.back", String.class)));
        build.setItem(3, "value", ItemBuild.build(Material.PAPER).setDisplayName(admin.get("upgrade-editor.click-set", String.class))
                                                                .addLore(admin.get("upgrade-editor.value", String.class)));
        build.setItem(5, "requirement", ItemBuild.build(Material.EXPERIENCE_BOTTLE).setDisplayName(admin.get("upgrade-editor.upgrades.editor.requirements", String.class)));

        if (system.hasLower(upgrade)) build.setItem(7, "previous", ItemBuild.build(CustomHead.get("quartz-arrow-left")).setDisplayName(admin.get("global.previous", String.class)));
        if (system.hasHigher(upgrade)) build.setItem(8, "next", ItemBuild.build(CustomHead.get("quartz-arrow-right")).setDisplayName(admin.get("global.next", String.class)));
        if (system.isLast(upgrade)) build.setItem(8, "create", ItemBuild.build(CustomHead .get("green-plus")).setDisplayName(admin.get("global.create", String.class)));
        if (system.isLast(upgrade)) build.setItem(1, "delete", ItemBuild.build(CustomHead.get("red-x")).setDisplayName(admin.get("global.delete", String.class)));
        
        EConsumer save = () -> {
            upgrade.set("value", build.getProperties("value", Number.class));
            upgrade.update();
            upgrade.save();
            build.setProperties("save", true);
        };

        build.setClickEvent((e) -> {
            e.setLocked(true);

            if (!Objects.isNull(e.getKeyed()) && !e.getKeyed().isEmpty() && !e.isKeyed("value") && !e.isKeyed("delete")) save.accept();

            if (e.isKeyed("back")) category.openUpgrades(player, upgrade.getType(), ((upgrade.getLevel()-1)/28));
            if (e.isKeyed("delete")) {
                system.remove(upgrade.getType());
                build.setProperties("save", true);

                if (upgrade.getLevel() <= 1) category.openUpgrades(player, upgrade.getType(), 0);
                else this.open(player, system.last(upgrade.getType()));
            }
            if (e.isKeyed("value")) {
                ChatInputBuild input = builders.getInputBuilder().register("");
                
                input.setCancelledMessage(message.get("input.message", String.class));

                input.setCompleted((completed) -> {
                    if (completed.getMessage().equalsIgnoreCase("-cancel")) {
                        build.open(player);
                        return true;
                    }
                    if (!Utilities.isDoubles(completed.getMessage())) {
                        completed.sendMessage(message.get("input.invalid", String.class).replace("value", completed.getMessage()));
                        return false;
                    }
                    build.setProperties("value", Utilities.getDouble(completed.getMessage()));
                    build.setProperties("save", false);
                    build.open(player);
                    return true;
                });
                
                logger.sendInfo(player, message.get("input.message", String.class));
                build.setProperties("save", true);
                input.enter(player);
            }
            if (e.isKeyed("requirement")) requirement.open(player, upgrade);
            if (e.isKeyed("previous")) this.open(player, system.lower(upgrade));
            if (e.isKeyed("next")) this.open(player, system.higher(upgrade));
            if (e.isKeyed("create")) {
                system.add(upgrade.getType());
                this.open(player, system.last(upgrade.getType()));
            }
        });

        build.setClosedEvent((e) -> {
            if (!build.getProperties("save", false, Boolean.class)) {
                save.accept();
            }
        });

        build.setPlaceholder(placeholder);
        build.open(player);
    }

}

class RequirementEditor extends Editor{
    private ElementEditor element;

    RequirementEditor(EmptyPlugin plugin, ElementEditor element) {
        super(plugin);

        this.element = element;
    }

    public void open(HumanEntity player, Upgrade upgrade) {
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(45).setTitle(admin.get("upgrade-editor.requirements.title", String.class));

        Map<String, Requirement> requirements = upgrade.getRequirements();

        build.setBorder(ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
        build.setItem(36, "back", ItemBuild.build(CustomHead.get("red-backward-2")).setDisplayName(admin.get("global.back", String.class)));
        if (requirements.size() < 21) build.setItem(44, "create", ItemBuild.build(CustomHead.get("green-plus")).setDisplayName(admin.get("global.create", String.class)));

        for (Requirement requirement : requirements.values()) {
            ItemBuild item = ItemBuild.build(requirement.getIcon());
            item.setDisplayName(admin.get("upgrade-editor.requirements.type", String.class).replace("{requirement-type}", language.get("parameter." + requirement.getType().toLowerCase(), String.class)));
            String lore = admin.get("upgrade-editor.value", String.class);
            if (requirement.getType().equalsIgnoreCase("item")) {
                ItemStack value = requirement.getValue(ItemStack.class);
                String result = admin.get("upgrade-editor.requirements.item-unset", String.class);
                if (!Objects.isNull(value)) {
                    ItemMeta meta = value.getItemMeta();
                    result =  (meta.hasDisplayName() ? meta.getDisplayName() : value.getType().toString().toLowerCase().replace("_", " "));
                    result += " &fx" + value.getAmount();
                }
                lore = lore.replace("{value}", result);
            }else {
                lore = lore.replace("{value}", new DecimalFormat("#,###").format(requirement.getValue(0, Number.class)));
            }
            item.addLore(lore);
            build.addItem(requirement.getId(), item);
        }

        build.setClickEvent((e) -> {
            e.setLocked(true);
            if (e.getRawSlot() < 0) return;
            if (!Objects.isNull(e.getKeyed()) && requirements.containsKey(e.getKeyed())) {
                this.openEditor(player, upgrade, requirements.get(e.getKeyed()));
            }

            if (e.isKeyed("create")) {
                Requirement requirement = upgrade.addRequirement("exp");
                upgrade.update();
                upgrade.getType().getConfig().save();
                this.openEditor(player, upgrade, requirement);
            }

            if (e.isKeyed("back")) {
                element.open(player, upgrade);
            }
        });

        build.open(player);
    }

    private void openEditor(HumanEntity player, Upgrade upgrade, Requirement requirement) {//
        InventoryBuild build = builders.getInventoryBuilder().register("").setSize(9).setTitle(admin.get("upgrade-editor.requirements.editor.title", String.class));
            
        build.setBorder(ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));

        build.setProperties("style", requirement.getType());
        build.setProperties("value", requirement.getValue());
        if (requirement.is("item")) {
            ItemStack item = requirement.getValue(ItemStack.class);
            if (!Objects.isNull(item) && item.getType() != Material.AIR) {
                build.setProperties("amount", item.getAmount());
            }
        }

        build.setItem(0, "back", ItemBuild.build(CustomHead.get("red-backward-2")).setDisplayName(admin.get("global.back", String.class)));
        build.setItem(1, "delete", ItemBuild.build(CustomHead.get("red-x")).setDisplayName(admin.get("global.delete", String.class)));

        Consumer<InventoryBuild> update = (ignore) -> {
            ignore.setItem(4, "style", ItemBuild.build(Requirement.getIconByType(ignore.getProperties("style", String.class)))
                .setDisplayName(admin.get("upgrade-editor.requirements.hotbar-set", String.class))
                .addLore("&8[&71&8] {exp}")
                .addLore("&8[&72&8] {money}")
                .addLore("&8[&73&8] {item}"));


            ItemBuild item = ItemBuild.build();
            if (ignore.getProperties("style", String.class).equalsIgnoreCase("item")) {
                ItemStack value = ignore.getProperties("value", new ItemStack(Material.BARRIER), ItemStack.class);
                if (value.getEnchantments().size() > 0) {
                    item.setEnchantment(Enchantment.DURABILITY, 1);
                    item.setItemFlag(ItemFlag.HIDE_ENCHANTS);
                }
                item.setMaterial(value.getType());
                item.setAmount(value.getAmount());
                item.setDisplayName(admin.get("upgrade-editor.requirements.drag_item-set", String.class));

                ItemBuild amount = ItemBuild.build(Material.PAPER);
                amount.setDisplayName(admin.get("upgrade-editor.click-set", String.class));
                amount.addLore(admin.get("upgrade-editor.amount", String.class));

                build.setItem(7, "amount", amount);
            }else {
                item.setMaterial(Material.PAPER);
                item.setDisplayName(admin.get("upgrade-editor.click-set", String.class));
                
                build.setItem(7, ItemBuild.build(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
            }
            item.setAmount(1);
            item.addLore(admin.get("upgrade-editor.value", String.class));
            ignore.setItem(6, "value", item);
        };
        update.accept(build);
        build.setUpdate(update);

        Placeholder placeholder = PlaceholderBuild.build();

        placeholder.set("{exp}", () -> {
            if (build.getProperties("style", String.class).equalsIgnoreCase("exp")) return "&2" + language.get("parameter.exp", String.class);
            return "&c" + language.get("parameter.exp", String.class);
        });

        placeholder.set("{money}", () -> {
            if (build.getProperties("style", String.class).equalsIgnoreCase("money")) return "&2" + language.get("parameter.money", String.class);
            return "&c" + language.get("parameter.money", String.class);
        });

        placeholder.set("{item}", () -> {
            if (build.getProperties("style", String.class).equalsIgnoreCase("item")) return "&2" + language.get("parameter.item", String.class);
            return "&c" + language.get("parameter.item", String.class);
        });

        placeholder.set("{value}", () -> {
            String value = admin.get("upgrade-editor.value", String.class);
            if (build.getProperties("style", String.class).equalsIgnoreCase("item")) {
                ItemStack item = build.getProperties("value", ItemStack.class);
                if (!Objects.isNull(item)) {
                    ItemMeta meta = item.getItemMeta();
                    value = meta.hasDisplayName() ? meta.getDisplayName() : "&f" + item.getType().toString().toLowerCase().replace("_", " ");
                }else {
                    value = admin.get("upgrade-editor.requirements.item-unset", String.class);
                }
            }else {
                value = new DecimalFormat("#,###").format(build.getProperties("value", 0d, Number.class));
            }
            return value;
        });

        placeholder.set("{amount}", () -> {
            return build.getProperties("amount", 0, Integer.class).toString();
        });

        build.setPlaceholder(placeholder);

        EConsumer save = () -> {
            requirement.setType(build.getProperties("style", String.class));
            if (build.getProperties("style", String.class).equalsIgnoreCase("item")) {
                ItemStack item = build.getProperties("value", ItemStack.class);
                if (!Objects.isNull(item) && item.getType() != Material.AIR) {
                    item.setAmount(build.getProperties("amount", 1, Integer.class));
                    requirement.setValue(item);
                }
            }else {
                requirement.setValue(build.getProperties("value", Object.class));
            }
            upgrade.update();
            upgrade.getType().getConfig().save();
            build.setProperties("saved", true);
        };

        build.setClickEvent((e) -> {
            if (e.getRawSlot() >= 0 && e.getRawSlot() < 9) e.setLocked(true);
            if (e.isKeyed("style") && (e.getEvent().getHotbarButton() >= 0) && (e.getEvent().getHotbarButton() <= 2)) {
                switch (e.getEvent().getHotbarButton()) {
                    case 0: build.setProperties("style", "EXP"); break;
                    case 1: build.setProperties("style", "MONEY"); break;
                    case 2: build.setProperties("style", "ITEM"); break;
                    default: build.setProperties("style", "EXP"); break;
                }
                build.update();
            }
            if (e.isKeyed("amount")) {
                ChatInputBuild input = builders.getInputBuilder().register("");
                
                    input.setCancelledMessage(message.get("input.message", String.class));

                    input.setCompleted((completed) -> {
                        if (completed.getMessage().equalsIgnoreCase("-cancel")) {
                            build.open(player);
                            return true;
                        }
                        if (!Utilities.isIntegers(completed.getMessage())) {
                            completed.sendMessage(message.get("input.invalid", String.class).replace("value", completed.getMessage()));
                            return false;
                        }
                        build.setProperties("amount", Utilities.getInt(completed.getMessage()));
                        build.open(player);
                        return true;
                    });
                    
                    logger.sendInfo(player, message.get("input.message", String.class));
                    input.enter(player);
            }
            if (e.isKeyed("value")) {
                if (build.getProperties("style", String.class).equalsIgnoreCase("item")) {
                    ItemStack cursor = e.getEvent().getCursor();
                    if (Objects.nonNull(cursor) && cursor.getType() != Material.AIR) {
                        build.setProperties("value", ItemStacks.newItemStack(e.getEvent().getCursor()));
                        build.update();
                    }
                }else {
                    ChatInputBuild input = builders.getInputBuilder().register("");
                
                    input.setCancelledMessage(message.get("input.message", String.class));

                    input.setCompleted((completed) -> {
                        if (completed.getMessage().equalsIgnoreCase("-cancel")) {
                            build.open(player);
                            return true;
                        }
                        if (!Utilities.isDoubles(completed.getMessage())) {
                            completed.sendMessage(message.get("input.invalid", String.class).replace("value", completed.getMessage()));
                            return false;
                        }
                        build.setProperties("value", Utilities.getDouble(completed.getMessage()));
                        build.open(player);
                        return true;
                    });
                    
                    logger.sendInfo(player, message.get("input.message", String.class));
                    input.enter(player);
                }
            }
            if (e.isKeyed("back")) {
                save.accept();
                this.open(player, upgrade);
            }
            if (e.isKeyed("delete")) {
                upgrade.removeRequirement(requirement);
                upgrade.update();
                upgrade.getType().getConfig().save();
                build.setProperties("saved", true);
                this.open(player, upgrade);
            }
        });

        build.setClosedEvent((e) -> {
            if (!build.getProperties("saved", false, Boolean.class)) {
                save.accept();
            }
        });

        build.open(player);
    }
}