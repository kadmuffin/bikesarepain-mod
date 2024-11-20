package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.BikesArePain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemKeyManager {
    public static final ResourceLocation BICYCLE_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle");
    public static final ResourceLocation NUT_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "nut");
    public static final ResourceLocation WRENCH_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "wrench");
    public static final ResourceLocation PEDOMETER_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "pedometer");
    public static final ResourceLocation BICYCLE_FRAME_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_frame");
    public static final ResourceLocation BICYCLE_GEARBOX_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_gearbox");
    public static final ResourceLocation BICYCLE_HANDLE_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_handlebar");
    public static final ResourceLocation BICYCLE_WHEEL_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_wheel");
    public static final ResourceLocation FLOAT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "float_on_water_modifier");

    // ResourceKeys
    public static final ResourceKey<Item> BICYCLE_KEY = ResourceKey.create(Registries.ITEM, BICYCLE_ID);
    public static final ResourceKey<Item> NUT_KEY = ResourceKey.create(Registries.ITEM, NUT_ID);
    public static final ResourceKey<Item> WRENCH_KEY = ResourceKey.create(Registries.ITEM, WRENCH_ID);
    public static final ResourceKey<Item> PEDOMETER_KEY = ResourceKey.create(Registries.ITEM, PEDOMETER_ID);
    public static final ResourceKey<Item> BICYCLE_FRAME_KEY = ResourceKey.create(Registries.ITEM, BICYCLE_FRAME_ID);
    public static final ResourceKey<Item> BICYCLE_GEARBOX_KEY = ResourceKey.create(Registries.ITEM, BICYCLE_GEARBOX_ID);
    public static final ResourceKey<Item> BICYCLE_HANDLE_KEY = ResourceKey.create(Registries.ITEM, BICYCLE_HANDLE_ID);
    public static final ResourceKey<Item> BICYCLE_WHEEL_KEY = ResourceKey.create(Registries.ITEM, BICYCLE_WHEEL_ID);
    public static final ResourceKey<Item> FLOAT_MODIFIER_KEY = ResourceKey.create(Registries.ITEM, FLOAT_MODIFIER_ID);

}
