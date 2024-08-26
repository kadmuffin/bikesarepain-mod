package com.kadmuffin.bikesarepain;

import com.kadmuffin.bikesarepain.client.KeybindManager;
import com.kadmuffin.bikesarepain.common.SoundManager;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import com.kadmuffin.bikesarepain.server.item.TooltipManager;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class BikesArePain {
    public static final String MOD_ID = "bikesarepain";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MOD_ID, Registries.SOUND_EVENT);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static void init() {
        EntityManager.init();
        ItemManager.init();
        PacketManager.init();
        KeybindManager.init();
        TooltipManager.init();
        SoundManager.init();
    }
}
