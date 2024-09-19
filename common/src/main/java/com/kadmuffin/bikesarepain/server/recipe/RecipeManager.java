package com.kadmuffin.bikesarepain.server.recipe;

import com.kadmuffin.bikesarepain.BikesArePain;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeManager {
    public static RegistrySupplier<RecipeSerializer<?>> BICYCLE_RECIPE_SERIALIZER = BikesArePain.RECIPE_SERIALIZER.register(BicycleRecipeBase.Serializer.ID, () -> BicycleRecipeBase.Serializer.INSTANCE);

    public static void init() {
        BikesArePain.RECIPE_SERIALIZER.register();
    }
}
