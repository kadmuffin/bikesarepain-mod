package com.kadmuffin.bikesarepain.server.item;
import com.kadmuffin.bikesarepain.client.item.TintedItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class TintedItem extends BaseItem {
    private final Map<String, Function<ItemStack, Integer>> bonesToColor;
    private final List<String> bonesToIgnore;

    public TintedItem(ResourceLocation model, Map<String, Function<ItemStack, Integer>> bonesToColor, List<String> bonesToIgnore, Properties properties) {
        super(model, properties);
        this.bonesToColor = bonesToColor;
        this.bonesToIgnore = bonesToIgnore;
    }

    public int evaluateBoneColor(String bone, ItemStack stack) {
        return this.bonesToColor.get(bone).apply(stack);
    }

    public boolean hasBoneColor(String bone) {
        return bonesToColor.containsKey(bone);
    }

    public List<String> getBonesToIgnore() {
        return bonesToIgnore;
    }

    public List<String> getBonesToColor() {
        return bonesToColor.keySet().stream().toList();
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private TintedItemRenderer<TintedItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new TintedItemRenderer<>(getModel(), getBonesToColor(), bonesToIgnore);

                return this.renderer;
            }
        });
    }

}
