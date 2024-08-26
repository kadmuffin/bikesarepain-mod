package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.entity.BicycleRenderer;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;

import static com.kadmuffin.bikesarepain.BikesArePain.ENTITIES;

public class EntityManager {
    public static final RegistrySupplier<EntityType<Bicycle>> BICYCLE = ENTITIES.register("bicycle", () ->
                    EntityType.Builder.of(Bicycle::new, MobCategory.MISC)
                            .sized(1F, 1F)
                            .build(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle").toString())
            );

    public static void init() {
        ENTITIES.register();
        EntityAttributeRegistry.register(EntityManager.BICYCLE, Bicycle::createBaseHorseAttributes);
        EntityRendererRegistry.register(EntityManager.BICYCLE, BicycleRenderer::new);
    }
}