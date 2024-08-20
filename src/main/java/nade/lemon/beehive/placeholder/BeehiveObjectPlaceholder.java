package nade.lemon.beehive.placeholder;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nade.empty.configuration.simple.ConfigBuild;
import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.StaticField;
import nade.lemon.beehive.StaticPlaceholder;
import nade.lemon.beehive.configuration.Language;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.upgrades.Upgrade;
import nade.lemon.beehive.upgrades.Requirement;
import nade.lemon.beehive.upgrades.UpgradeSystem;
import nade.lemon.beehive.upgrades.UpgradeType;
import nade.lemon.builders.placeholder.PlaceholderBuild;
import nade.lemon.utils.string.Color;

public class BeehiveObjectPlaceholder extends PlaceholderBuild{
	private BeehiveObject beehive;
	
	Map<String, String> replaces = Maps.newHashMap();
	
	private DecimalFormat format = new DecimalFormat("#,###");
	
	private final UpgradeSystem upgradeSystem;

	private final ConfigBuild language;

	public BeehiveObjectPlaceholder(BeehiveObject beehive) {
		this.beehive = beehive;

		this.upgradeSystem = LemonBeehive.getInstance().get(UpgradeSystem.class);
		this.language = LemonBeehive.getInstance().get(Language.class).get("language");
	}
	
	@Override
	public String replace(String request) {
		for (String key : replaces.keySet()) {
			request = request.replace(key, replaces.get(key));
		}

		for (UpgradeType type : UpgradeType.values()) {
			Upgrade upgrade = beehive.getUpgrade(type);
			if (request.contains("{" + type.getType() + "_upgrade}")) {
				if (upgradeSystem.isLast(upgrade) || !upgradeSystem.higher(upgrade).isValid()) {
					request = language.get("upgrade.max-level", String.class);
				}else {
					request = request.replace("{" + type.getType() + "_requirements}", "");
				}
			}
			request = request.replace("{" + type.getType() + "_level}", String.valueOf(upgrade.getLevel()))
							 .replace("{" + type.getType() + "_value}", new DecimalFormat("0.#").format(upgrade.getValue()));
		}
		return Color.hex(placeBeehive(request));
	}

	@Override
	public Collection<String> replace(Collection<String> lores) {
		if (Objects.isNull(lores)) return Lists.newArrayList();
		List<String> result = Lists.newArrayList();
		for (String request : lores) {
			for (String key : replaces.keySet()) {
				request = request.replace(key, replaces.get(key));
			}
			boolean bypass = false;
			for (UpgradeType type : UpgradeType.values()) {
				Upgrade upgrade = beehive.getUpgrade(type);
				if (request.contains("{" + type.getType() + "_requirements}")) {
					if (upgradeSystem.isLast(upgrade) || !upgradeSystem.higher(upgrade).isValid()) {
						request = language.get("upgrade.max-level", String.class);
					}else {
						String start = language.get("upgrade.requirements.start", String.class);
						String line = language.get("upgrade.requirements.line", String.class);
						String end = language.get("upgrade.requirements.end", String.class);
						result.add(Color.hex(start));
						for (Requirement method : upgrade.getRequirements().values()) {
							if (!method.isValid()) continue;
							String amountColor = language.get("upgrade.color." + method.getType().toLowerCase(), String.class);
							String amountString;
							String methodString;
							if (method.is("item")) {
								ItemStack item = method.getValue(ItemStack.class);
								amountString = format.format(item.getAmount());
								methodString = item.hasItemMeta() ? item.getItemMeta().getDisplayName() : item.getType().toString();
							}else {
								amountString = format.format(method.getValue());
								methodString = language.get("parameter." + method.getType().toLowerCase(), String.class);
							}
							result.add(Color.hex(line.replace("{requirement-value}", amountColor + amountString)
													 .replace("{requirement-type}", methodString)));
						}
						result.add(Color.hex(end));
						bypass = true;
					}
				}
				request = this.placeUpgradeType(request, type);
			}
			if (bypass) continue;
			result.add(Color.hex(placeBeehive(request)));
		}
		return result;
	}

	private String placeUpgradeType(String require, UpgradeType types) {
		Upgrade upgrade = beehive.getUpgrade(types);
		return require.replace("{" + types.getType() + "_level}", String.valueOf(upgrade.getLevel()))
					.replace("{" + types.getType() + "_value}", new DecimalFormat("0.#").format(upgrade.getValue()));
	}

	private String placeBeehive(String request) {
		return request.replace("{beehive-owner}", beehive.getOwner().getName())
						.replace("{beehive-displayname}", beehive.getDisplayName())
						.replace("{storage-location}", StaticPlaceholder.xyzStyle(beehive))
						.replace("{area}", StaticPlaceholder.area())
						.replace("{honey-multiplier}", StaticField.HONEY_MULTIPLIER.toString())

						.replace("{entity}", String.valueOf(beehive.getHiveBees().getEnters().size()))
						.replace("{honey}", new DecimalFormat("0.#").format(beehive.getHoney()))
						.replace("{max-honey}", new DecimalFormat("0.#").format(beehive.getUpgrade(UpgradeType.valueOf("honey-capacity")).getValue()))
						.replace("{max-entity}", beehive.getUpgrade(UpgradeType.valueOf("beehive-capacity")).getValue().toString());
	}
}
