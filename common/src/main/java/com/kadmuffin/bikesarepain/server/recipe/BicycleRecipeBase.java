package com.kadmuffin.bikesarepain.server.recipe;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.server.item.ComponentManager;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class BicycleRecipeBase implements CraftingRecipe, RecipeInput {
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String nbtCopyInstructions;
    final CraftingBookCategory category;
    final String group;

    public BicycleRecipeBase(String group, String nbtCopyInstructions, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
        this.nbtCopyInstructions = nbtCopyInstructions;
        this.category = category;
        this.pattern = pattern;
        this.result = result;
        this.group = group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeManager.BICYCLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.pattern.matches(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = this.getResultItem(registries).copy();

        this.parseInstruction(result, input);

        return result;
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonNullList = this.getIngredients();
        return nonNullList.isEmpty() || nonNullList.stream().filter((ingredient) -> !ingredient.isEmpty()).anyMatch((ingredient) -> ingredient.getItems().length == 0);
    }

    @Override
    public ItemStack getItem(int index) {
        return this.getIngredients().get(index).getItems()[0];
    }

    @Override
    public int size() {
        return this.getIngredients().size();
    }

    /**
     * Parses the dye_copy string into a list of instructions.
     * An instruction might look like this: "0:1", where "source:target"
     * This function parses the string, gets the ingredients, and given a result, it applies the instructions to the result.
     */
    public void parseInstruction(ItemStack result, CraftingInput input) {
        String[] instructions = this.nbtCopyInstructions.split(";");
        ArrayList<Integer> targetColors = new ArrayList<>(ItemManager.bicycleColors);

        for (String instruction : instructions) {
            String[] parts = instruction.split(":");
            int index = Integer.parseInt(parts[0]);
            int targetIndex = Integer.parseInt(parts[1]);
            ItemStack ingredient = input.getItem(index);
            DyedItemColor sourceColor = ingredient.get(DataComponents.DYED_COLOR);
            if (sourceColor != null && targetIndex < targetColors.size() && targetIndex >= 0) {
                targetColors.set(targetIndex, sourceColor.rgb());
            }
        }

        result.set(ComponentManager.BICYCLE_COLORS.get(), targetColors);
    }

    public static class Serializer implements RecipeSerializer<BicycleRecipeBase> {
        public static final MapCodec<BicycleRecipeBase> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.fieldOf("group").forGetter((shapedRecipe) -> {
                return shapedRecipe.group;
            }),Codec.STRING.fieldOf("dye_copy").forGetter((shapedRecipe) -> {
                return shapedRecipe.nbtCopyInstructions;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedRecipe) -> {
                return shapedRecipe.category;
            }), ShapedRecipePattern.MAP_CODEC.forGetter((shapedRecipe) -> {
                return shapedRecipe.pattern;
            }), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapedRecipe) -> {
                return shapedRecipe.result;
            })).apply(instance, BicycleRecipeBase::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, BicycleRecipeBase> STREAM_CODEC = StreamCodec.of(BicycleRecipeBase.Serializer::toNetwork, BicycleRecipeBase.Serializer::fromNetwork);

        public Serializer() {
        }

        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "shaped_crafting_bicycle");

        public MapCodec<BicycleRecipeBase> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, BicycleRecipeBase> streamCodec() {
            return STREAM_CODEC;
        }

        private static BicycleRecipeBase fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            String nbtCopyInstructions = buffer.readUtf();
            CraftingBookCategory craftingBookCategory = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack itemStack = ItemStack.STREAM_CODEC.decode(buffer);
            return new BicycleRecipeBase(group, nbtCopyInstructions, craftingBookCategory, shapedRecipePattern, itemStack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, BicycleRecipeBase recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeUtf(recipe.nbtCopyInstructions);
            buffer.writeEnum(recipe.category);
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}