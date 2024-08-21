package com.kadmuffin.bikesarepain;

import com.google.common.base.Suppliers;
import com.kadmuffin.bikesarepain.client.entity.BicycleRenderer;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import javax.swing.text.html.parser.Entity;
import java.util.function.Supplier;

public final class BikesArePain {
    public static final String MOD_ID = "bikesarepain";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);
    public static final RegistrySupplier<EntityType<Bicycle>> BICYCLE = ENTITIES.register("bicycle", () -> EntityManager.BICYCLE);
    public static void init() {
        ENTITIES.register();
        EntityAttributeRegistry.register(BICYCLE, Bicycle::createBaseHorseAttributes);
        EntityRendererRegistry.register(BICYCLE, BicycleRenderer::new);
    }
}
