package com.kadmuffin.bikesarepain.neoforge;

import com.kadmuffin.bikesarepain.BikesArePain;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.level.entity.forge.EntityRendererRegistryImpl;
import net.neoforged.fml.common.Mod;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

@Mod(MOD_ID)
public final class BikesArePainNeoForge {
    public BikesArePainNeoForge() {
        // Run our common setup.
        BikesArePain.init();
    }
}
