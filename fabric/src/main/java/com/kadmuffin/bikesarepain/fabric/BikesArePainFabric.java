package com.kadmuffin.bikesarepain.fabric;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

public final class BikesArePainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        BikesArePain.init();
        /*if (FabricLoader.getInstance().isModLoaded("dehydration")) {
            AbstractBike.addOnMoveListener((bike, speed, moving) -> {
                if (moving && bike.getFirstPassenger() instanceof Player player) {
                    ThirstManagerAccess playerAcc = (ThirstManagerAccess) player;
                    float reducedSpeed = speed * bike.getSpeedFactor(bike.getHealth() / bike.getMaxHealth());
                    float effortToSpeedRatio = Math.clamp(reducedSpeed / speed, 0, 1);

                    thirstTick++;

                    if (thirstTick > 20) {
                        thirstTick = 0;
                        playerAcc.getThirstManager().addDehydration(0.1F + (1F - effortToSpeedRatio) * 0.9F);
                    }
                }
            });
        }*/
    }
}
