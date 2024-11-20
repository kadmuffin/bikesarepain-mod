package com.kadmuffin.bikesarepain.server.recipe;

import com.kadmuffin.bikesarepain.BikesArePain;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeManager {
    public static final RegistrySupplier<RecipeSerializer<? extends CraftingRecipe>> BICYCLE_RECIPE_SERIALIZER = BikesArePain.RECIPE_SERIALIZER.register(BicycleRecipeBase.Serializer.ID, () -> BicycleRecipeBase.Serializer.INSTANCE);

    public static void init() {
        BikesArePain.RECIPE_SERIALIZER.register();
    }
}
