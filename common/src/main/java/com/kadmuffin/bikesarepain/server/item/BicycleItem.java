package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.item.BaseItemRenderer;
import com.kadmuffin.bikesarepain.client.item.BicycleItemRenderer;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BicycleItem extends BikeItem {
    public BicycleItem(EntityType<? extends AbstractBike> entityType, ResourceLocation modelName, Map<String, Function<ItemStack, Integer>> bonesToColor, List<String> bonesToIgnore, Properties properties) {
        super(entityType, modelName, bonesToColor, bonesToIgnore, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BicycleItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new BicycleItemRenderer(getModel(), getBonesToColor(), getBonesToIgnore());

                return this.renderer;
            }
        });
    }
}
