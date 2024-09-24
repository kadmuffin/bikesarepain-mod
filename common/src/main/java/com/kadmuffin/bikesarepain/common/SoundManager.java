package com.kadmuffin.bikesarepain.common;

import com.kadmuffin.bikesarepain.BikesArePain;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundManager {
    public static final RegistrySupplier<SoundEvent> BICYCLE_SPOKES =
            BikesArePain.SOUNDS.register(
                    "bicycle_spokes",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_spokes"))
            );

    public static final RegistrySupplier<SoundEvent> BICYCLE_BELL =
            BikesArePain.SOUNDS.register(
                    "bicycle_bell",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_bell"))
            );

    public static final RegistrySupplier<SoundEvent> BICYCLE_LAND =
            BikesArePain.SOUNDS.register(
                    "bicycle_land",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_land"))
            );


    public static final RegistrySupplier<SoundEvent> BICYCLE_MOVEMENT =
            BikesArePain.SOUNDS.register(
                    "bicycle_movement",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_movement"))
            );

    public static void init() {
        BikesArePain.SOUNDS.register();
    }

}
