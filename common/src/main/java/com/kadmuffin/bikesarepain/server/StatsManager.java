package com.kadmuffin.bikesarepain.server;

import com.kadmuffin.bikesarepain.BikesArePain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class StatsManager {
    public static final ResourceLocation DISTANCE_TRAVELED = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "distance_traveled");

    public static void init() {
        BikesArePain.STATS.register(DISTANCE_TRAVELED.getPath(), () -> DISTANCE_TRAVELED);
        BikesArePain.STATS.register();

        Stats.CUSTOM.get(DISTANCE_TRAVELED, StatFormatter.DISTANCE);
    }
}
