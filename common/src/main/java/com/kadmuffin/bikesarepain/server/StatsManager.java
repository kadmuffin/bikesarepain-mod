package com.kadmuffin.bikesarepain.server;

import com.kadmuffin.bikesarepain.BikesArePain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import java.util.function.Supplier;

public class StatsManager {
    public static final Supplier<ResourceLocation> DISTANCE_TRAVELED = BikesArePain.STATS.register("distance_traveled", () -> new ResourceLocation(BikesArePain.MOD_ID, "distance_traveled"));
    public static final Supplier<ResourceLocation> DISTANCE_TRAVELED_JSC = BikesArePain.STATS.register("distance_traveled_jsc", () -> new ResourceLocation(BikesArePain.MOD_ID, "distance_traveled_jsc"));
    public static final Supplier<ResourceLocation> CALORIES_BURNED_JSC = BikesArePain.STATS.register("calories_burned_jsc", () -> new ResourceLocation(BikesArePain.MOD_ID, "calories_burned_jsc"));

    public static void init() {
        BikesArePain.STATS.register();

        // Register on StatsScreen
        // Stats.CUSTOM.get(DISTANCE_TRAVELED.get(), StatFormatter.DISTANCE);
        // Stats.CUSTOM.get(DISTANCE_TRAVELED_JSC.get(), StatFormatter.DISTANCE);
        // Stats.CUSTOM.get(CALORIES_BURNED_JSC.get(), StatFormatter.DEFAULT);
    }
}
