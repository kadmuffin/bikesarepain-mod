package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import com.mojang.serialization.Codec;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public class ItemManager {
    public static final RegistrySupplier<CreativeModeTab> BIKES_MOD_TAB = BikesArePain.TABS.register("bikes_mod_tab", () ->
            CreativeTabRegistry.create(
                    Component.literal("Bikes Are Pain"),
                    () -> new ItemStack(ItemManager.BICYCLE_ITEM.get())
            )
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HAS_DISPLAY = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "has_display"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SADDLED = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "saddled"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HEALTH_AFFECTS_SPEED = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "health_affect_speed"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SAVE_TIME = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "save_time"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SAVE_DISTANCE = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "save_distance"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Float>> DISTANCE_MOVED = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "distance_moved"),
            () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).build()
    );
    public static final RegistrySupplier<DataComponentType<Integer>> TICKS_MOVED = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "ticks_ridden"),
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HAS_BALLOON = BikesArePain.DATA_COMPONENTS.register(
            new ResourceLocation(BikesArePain.MOD_ID, "has_balloon"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static final RegistrySupplier<Item> BICYCLE_ITEM = BikesArePain.ITEMS.register("bicycle", () ->
            new BikeItem(EntityManager.BICYCLE.get(),
                    new ResourceLocation(BikesArePain.MOD_ID, "bicycle"),
                    new BikeItem.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .durability(100)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(SADDLED.get(), false)
                            .component(SAVE_TIME.get(), true)
                            .component(SAVE_DISTANCE.get(), true)
                            .component(DISTANCE_MOVED.get(), 0.0F)
                            .component(TICKS_MOVED.get(), 0)
                            .component(HEALTH_AFFECTS_SPEED.get(), true)
                            .component(HAS_BALLOON.get(), false)
                            .component(HAS_DISPLAY.get(), false)
            )
    );

    public static final RegistrySupplier<Item> NUT_ITEM = BikesArePain.ITEMS.register("nut", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "nut"),
                    new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.COMMON)
                    .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> WRENCH_ITEM = BikesArePain.ITEMS.register("wrench", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "wrench"),
                    new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .arch$tab(ItemManager.BIKES_MOD_TAB)
                    .durability(100)
            )
    );

    public static final RegistrySupplier<Item> PEDOMETER_ITEM = BikesArePain.ITEMS.register("pedometer", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "pedometer"),
                    new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> FRAME_ITEM = BikesArePain.ITEMS.register("bicycle_frame", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "bicycle_frame"),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> GEARBOX_ITEM = BikesArePain.ITEMS.register("bicycle_gearbox", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "bicycle_gearbox"),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> HANDLEBAR_ITEM = BikesArePain.ITEMS.register("bicycle_handlebar", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "bicycle_handlebar"),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> WHEEL_ITEM = BikesArePain.ITEMS.register("bicycle_wheel", () ->
            new BaseItem(
                    new ResourceLocation(BikesArePain.MOD_ID, "bicycle_wheel"),
                    new Item.Properties()
                            .stacksTo(2)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );


    public static void init() {
        BikesArePain.TABS.register();
        BikesArePain.DATA_COMPONENTS.register();
        BikesArePain.ITEMS.register();
    }

}
