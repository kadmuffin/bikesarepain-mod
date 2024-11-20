package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.client.entity.BicycleRenderer;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static com.kadmuffin.bikesarepain.BikesArePain.ENTITIES;
import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class EntityManager {

    public static final RegistrySupplier<EntityType<Bicycle>> BICYCLE = ENTITIES.register("bicycle", () ->
            EntityType.Builder.of(Bicycle::new, MobCategory.MISC)
                    .sized(1F, 1F).build(
                            ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "bicycle"))
                    ));

    public static void init() {
        ENTITIES.register();
        EntityAttributeRegistry.register(EntityManager.BICYCLE, Bicycle::createBaseHorseAttributes);
        EntityRendererRegistry.register(EntityManager.BICYCLE, BicycleRenderer::new);
    }
}