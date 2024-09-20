package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.client.item.BicycleItemRenderer;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
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
    public void placementHook(AbstractBike entity, ItemStack itemStack) {
        super.placementHook(entity, itemStack);
        if (entity instanceof Bicycle bicycle) {
            if (itemStack.has(ItemManager.HAS_BALLOON.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.HAS_BALLOON.get()))) {
                bicycle.setHasBalloon(true);
            }

            if (itemStack.has(ItemManager.HAS_DISPLAY.get()) && Boolean.TRUE.equals(itemStack.get(ItemManager.HAS_DISPLAY.get()))) {
                bicycle.setHasDisplay(true);
            }

            if (itemStack.has(ItemManager.BICYCLE_COLORS.get())) {
                List<Integer> bicycleColors = Utils.completeRest(itemStack.getOrDefault(ItemManager.BICYCLE_COLORS.get(), ItemManager.bicycleColors), ItemManager.bicycleColors);

                int frontWheelColor = bicycleColors.getFirst();
                int backWheelColor = bicycleColors.get(1);
                int gearboxColor = bicycleColors.get(3);
                int frameColor = bicycleColors.get(2);

                bicycle.setFWheelColor(frontWheelColor);
                bicycle.setRWheelColor(backWheelColor);
                bicycle.setGearboxColor(gearboxColor);
                bicycle.setFrameColor(frameColor);
            }
        }
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
