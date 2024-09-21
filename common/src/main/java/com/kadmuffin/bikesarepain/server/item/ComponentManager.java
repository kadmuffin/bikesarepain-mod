package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ComponentManager {
    public static final RegistrySupplier<DataComponentType<List<Integer>>> BICYCLE_COLORS = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "colors"),
            () -> DataComponentType.<List<Integer>>builder().persistent(Codec.INT.listOf()).build()
    );


    public static final RegistrySupplier<DataComponentType<Boolean>> HAS_DISPLAY = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "has_display"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SADDLED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "saddled"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HEALTH_AFFECTS_SPEED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "health_affect_speed"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SAVE_TIME = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "save_time"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SAVE_DISTANCE = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "save_distance"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Float>> DISTANCE_MOVED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "distance_moved"),
            () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).build()
    );
    public static final RegistrySupplier<DataComponentType<Integer>> TICKS_MOVED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "ticks_ridden"),
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HAS_BALLOON = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "has_balloon"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static void init() {
        BikesArePain.DATA_COMPONENTS.register();
    }
}
