package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.client.item.BaseItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ResourceLocation model;
    private final Map<String, Function<ItemStack, Integer>> bonesToColor;
    private final List<String> bonesToIgnore;

    public BaseItem(ResourceLocation model, Map<String, Function<ItemStack, Integer>> bonesToColor, List<String> bonesToIgnore, Properties properties) {
        super(properties);
        this.model = model;
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

    public ResourceLocation getModel() {
        return model;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        BaseItem item = this;
        consumer.accept(new GeoRenderProvider() {
            private BaseItemRenderer<BaseItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new BaseItemRenderer<>(model, getBonesToColor(), bonesToIgnore);

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
