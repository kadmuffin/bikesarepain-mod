package com.kadmuffin.bikesarepain.server;

import com.kadmuffin.bikesarepain.BikesArePain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class StatsManager {
    public static final ResourceLocation DISTANCE_TRAVELED = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "distance_traveled");
    // JSerialComm
    public static final ResourceLocation DISTANCE_TRAVELED_JSC = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "distance_traveled_jsc");
    public static final ResourceLocation CALORIES_BURNED_JSC = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "calories_burned_jsc");

    public static void init() {
        BikesArePain.STATS.register(DISTANCE_TRAVELED.getPath(), () -> DISTANCE_TRAVELED);
        BikesArePain.STATS.register(DISTANCE_TRAVELED_JSC.getPath(), () -> DISTANCE_TRAVELED_JSC);
        BikesArePain.STATS.register(CALORIES_BURNED_JSC.getPath(), () -> CALORIES_BURNED_JSC);

        // Register on StatsScreen
        //Stats.CUSTOM.get(DISTANCE_TRAVELED, StatFormatter.DISTANCE);
        //Stats.CUSTOM.get(DISTANCE_TRAVELED_JSC, StatFormatter.DISTANCE);
        //Stats.CUSTOM.get(CALORIES_BURNED_JSC, StatFormatter.DEFAULT);

        BikesArePain.STATS.register();
    }
}
