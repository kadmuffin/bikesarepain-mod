package com.kadmuffin.bikesarepain;

import com.kadmuffin.bikesarepain.client.entity.BicycleRenderer;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class BikesArePain {
    public static final String MOD_ID = "bikesarepain";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static void init() {
        EntityManager.init();
        ItemManager.init();
    }
}
