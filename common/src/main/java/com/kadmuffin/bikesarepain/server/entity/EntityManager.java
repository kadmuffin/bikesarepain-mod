package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.BikesArePain;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;

public class EntityManager {
    public static EntityType<Bicycle> BICYCLE = EntityType.Builder.of(Bicycle::new, MobCategory.MISC).sized(1.0F, 1.0F).build(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle").toString());
}