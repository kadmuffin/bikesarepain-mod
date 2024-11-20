package com.kadmuffin.bikesarepain.server.recipe;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.server.item.ComponentManager;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import com.kadmuffin.bikesarepain.utils.ColorUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BicycleRecipeBase implements CraftingRecipe, RecipeInput {
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String nbtCopyInstructions;
    final CraftingBookCategory category;
    final String group;
    PlacementInfo info;

    public BicycleRecipeBase(String group, String nbtCopyInstructions, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result) {
        this.nbtCopyInstructions = nbtCopyInstructions;
        this.category = category;
        this.pattern = pattern;
        this.result = result;
        this.group = group;
    }

    @Override
    public @NotNull RecipeSerializer<? extends CraftingRecipe> getSerializer() {
        return RecipeManager.BICYCLE_RECIPE_SERIALIZER.get();
    }

    public String getGroup() {
        return group;
    }

    @Override
    public @NotNull RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        if (this.info == null) {
            // Create placement info
            this.info = PlacementInfo.createFromOptionals(this.pattern.ingredients());
        }

        return this.info;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return this.category;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public List<Optional<Ingredient>> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ArrayList<ItemStack> items = new ArrayList<>();

        input.items().forEach(item -> {
            ItemStack itemCopy = item.copy();
            itemCopy.remove(DataComponents.DYED_COLOR);

            items.add(itemCopy);
        });

        return this.pattern.matches(CraftingInput.of(
                input.width(),
                input.height(),
                items
        ));
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = this.getResult().copy();

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
    public @NotNull ItemStack getItem(int index) {
        return this.getIngredients().get(index).orElse(Ingredient.of(Items.AIR)).items().getFirst().value().getDefaultInstance();
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

        result.set(ComponentManager.BICYCLE_COLORS.get(), ColorUtil.toRGB(targetColors));
    }

    public static class Serializer implements RecipeSerializer<BicycleRecipeBase> {
        public static final MapCodec<BicycleRecipeBase> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.fieldOf("group").forGetter((shapedRecipe) -> {
                return shapedRecipe.group;
            }), Codec.STRING.fieldOf("dye_copy").forGetter((shapedRecipe) -> {
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
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "shaped_crafting_bicycle");
        public Serializer() {
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

        public MapCodec<BicycleRecipeBase> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, BicycleRecipeBase> streamCodec() {
            return STREAM_CODEC;
        }
    }
}