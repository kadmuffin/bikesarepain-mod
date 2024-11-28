package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.client.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Objects;

public class PedometerItem extends BaseItem {
    public PedometerItem(ResourceLocation model, Properties properties) {
        super(model, properties);
    }

    @Environment(EnvType.CLIENT)
    public static void addTimeHover(ItemStack stack, List<Component> tooltipComponents) {
        if (stack.has(ComponentManager.TICKS_MOVED.get())) {
            // Unpack in safe way
            float ticksMoved = Objects.requireNonNullElse(stack.get(ComponentManager.TICKS_MOVED.get()), 0);
            ticksMoved /= 20.0F; // Convert to seconds
            if (ticksMoved >= 60) {
                int minutes = (int) (ticksMoved / 60);
                int seconds = (int) (ticksMoved % 60);
                tooltipComponents.add(
                        Component.translatable("item.bikesarepain.bicycle.tooltip.time_pedalled")
                                .withColor(CommonColors.GRAY)
                                .append(Component.literal(minutes + "m " + seconds + "s")
                                        .withColor(CommonColors.GREEN))
                );
            } else {
                tooltipComponents.add(
                        Component.translatable("item.bikesarepain.bicycle.tooltip.time_pedalled")
                                .withColor(CommonColors.GRAY)
                                .append(Component.literal(ticksMoved + "s")
                                        .withColor(CommonColors.GREEN))
                );
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static void addDistanceHover(ItemStack stack, List<Component> tooltipComponents) {
        if (stack.has(ComponentManager.DISTANCE_MOVED.get())) {
            float distanceMoved = Objects.requireNonNullElse(stack.get(ComponentManager.DISTANCE_MOVED.get()), 0.0F);
            MutableComponent msg = Component.translatable("item.bikesarepain.bicycle.tooltip.distance_moved")
                    .withColor(CommonColors.GRAY);

            if (ClientConfig.CONFIG.instance().isImperial()) {
                // 1609 meters ≈ 1 mile
                if (distanceMoved >= 1609) {
                    double miles = distanceMoved / 1609.344;
                    int wholeMiles = (int) miles;
                    int feet = (int) ((miles - wholeMiles) * 5280);
                    tooltipComponents.add(
                            msg.append(Component.literal(wholeMiles + "mi " + feet + "ft")
                                    .withColor(CommonColors.GREEN))
                    );
                } else {
                    int feet = (int) (distanceMoved * 3.28084);
                    tooltipComponents.add(
                            msg.append(Component.literal(feet + "ft")
                                    .withColor(CommonColors.GREEN))
                    );
                }
            } else {
                if (distanceMoved >= 1000) {
                    int km = (int) (distanceMoved / 1000);
                    int m = (int) (distanceMoved % 1000);
                    tooltipComponents.add(
                            msg.append(Component.literal(km + "km " + m + "m")
                                    .withColor(CommonColors.GREEN))
                    );
                } else {
                    tooltipComponents.add(
                            msg.append(Component.literal(distanceMoved + "m")
                                    .withColor(CommonColors.GREEN))
                    );
                }
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.bikesarepain.pedometer.tooltip").withColor(CommonColors.LIGHT_GRAY));
        addTimeHover(stack, tooltipComponents);
        addDistanceHover(stack, tooltipComponents);
    }
}
