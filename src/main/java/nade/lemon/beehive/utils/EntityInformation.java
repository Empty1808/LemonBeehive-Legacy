package nade.lemon.beehive.utils;

import java.util.Objects;

import org.bukkit.entity.Bee;

import nade.lemon.utils.bukkit.Locations;

public class EntityInformation {
    
    public static void information(Bee entity) {
		System.out.println("------------ Information ------------");
		System.out.println("Anger: " + entity.getAnger());
		System.out.println("Cannot Enter Hive Ticks: " + entity.getCannotEnterHiveTicks());
		System.out.println("Flower: " + (Objects.isNull(entity.getFlower()) ? null : Locations.toJson(entity.getFlower())));
		System.out.println("Hive: " + (Objects.isNull(entity.getHive()) ? null : Locations.toJson(entity.getHive())));
		System.out.println("Nectar: " + entity.hasNectar());
		System.out.println("Stung: " + entity.hasStung());
		System.out.println(" ");
	}
}
