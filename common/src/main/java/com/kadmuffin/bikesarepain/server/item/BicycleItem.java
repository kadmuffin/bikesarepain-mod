package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.client.item.BicycleItemRenderer;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.util.Color;

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
            if (itemStack.has(ComponentManager.HAS_BALLOON.get()) && Boolean.TRUE.equals(itemStack.get(ComponentManager.HAS_BALLOON.get()))) {
                bicycle.setHasBalloon(true);
            }

            if (itemStack.has(ComponentManager.HAS_DISPLAY.get()) && Boolean.TRUE.equals(itemStack.get(ComponentManager.HAS_DISPLAY.get()))) {
                bicycle.setHasDisplay(true);
            }

            if (itemStack.has(ComponentManager.BICYCLE_COLORS.get())) {
                List<Integer> bicycleColors = Utils.completeRest(itemStack.getOrDefault(ComponentManager.BICYCLE_COLORS.get(), ItemManager.bicycleColors), ItemManager.bicycleColors);

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

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int durability = stack.getDamageValue();
        int maxDurability = stack.getMaxDamage();

        // Calculate durability percentage
        int durabilityPercentage = (int) (100 - ((float) durability / maxDurability) * 100);

        // Determine the color based on durability percentage
        ChatFormatting durabilityColor = switch (durabilityPercentage) {
            case 100 -> ChatFormatting.GREEN;
            case 76, 99 -> ChatFormatting.DARK_GREEN;
            case 51, 75 -> ChatFormatting.YELLOW;
            case 26, 50 -> ChatFormatting.RED;
            default -> ChatFormatting.DARK_RED;
        };

        // Add durability tooltip
        tooltipComponents.add(1, Component.translatable("item.bikesarepain.bicycle.tooltip.health")
                .withColor(CommonColors.GRAY)
                .append(Component.literal(durabilityPercentage + "%").withStyle(durabilityColor)));

        // Check if the item is saddled and add the respective tooltip
        boolean isSaddled = stack.has(ComponentManager.SADDLED.get()) && Boolean.TRUE.equals(stack.get(ComponentManager.SADDLED.get()));
        String saddleKey = isSaddled ? "item.bikesarepain.bicycle.tooltip.yes" : "item.bikesarepain.bicycle.tooltip.no";
        int saddleColor = isSaddled ? Color.ofRGB(255, 149, 0).argbInt() : CommonColors.RED;

        tooltipComponents.add(Component.translatable("item.bikesarepain.bicycle.tooltip.saddled")
                .withColor(CommonColors.GRAY)
                .append(Component.translatable(saddleKey).withColor(saddleColor)));

        // Add tooltips for time and distance
        if (stack.has(ComponentManager.SAVE_TIME.get()) && Boolean.TRUE.equals(stack.get(ComponentManager.SAVE_TIME.get()))) {
            PedometerItem.addTimeHover(stack, tooltipComponents);
        }

        if (stack.has(ComponentManager.SAVE_DISTANCE.get()) && Boolean.TRUE.equals(stack.get(ComponentManager.SAVE_DISTANCE.get()))) {
            PedometerItem.addDistanceHover(stack, tooltipComponents);
        }
    }
}
