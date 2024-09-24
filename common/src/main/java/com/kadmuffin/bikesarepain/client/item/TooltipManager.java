package com.kadmuffin.bikesarepain.client.item;

import com.kadmuffin.bikesarepain.server.item.BikeItem;
import com.kadmuffin.bikesarepain.server.item.ComponentManager;
import dev.architectury.event.events.client.ClientTooltipEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import software.bernie.geckolib.util.Color;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class TooltipManager {
    public static void init() {
        ClientTooltipEvent.ITEM.register((stack, lines, context, tooltipFlag) -> {
            if (stack.has(ComponentManager.SAVE_TIME.get()) && Boolean.TRUE.equals(stack.get(ComponentManager.SAVE_TIME.get()))) {
                if (stack.has(ComponentManager.TICKS_MOVED.get())) {
                    // Unpack in safe way
                    float ticksMoved = Objects.requireNonNullElse(stack.get(ComponentManager.TICKS_MOVED.get()), 0);
                    ticksMoved /= 20.0F; // Convert to seconds
                    if (ticksMoved >= 60) {
                        int minutes = (int) (ticksMoved / 60);
                        int seconds = (int) (ticksMoved % 60);
                        lines.add(
                                Component.translatable("item.bikesarepain.bicycle.tooltip.time_pedalled")
                                        .withColor(CommonColors.GRAY)
                                        .append(Component.literal(minutes + "m " + seconds + "s")
                                                .withColor(CommonColors.GREEN))
                        );
                    } else {
                        lines.add(
                                Component.translatable("item.bikesarepain.bicycle.tooltip.time_pedalled")
                                        .withColor(CommonColors.GRAY)
                                        .append(Component.literal(ticksMoved + "s")
                                                .withColor(CommonColors.GREEN))
                        );
                    }
                }
            }

            if (stack.has(ComponentManager.SAVE_DISTANCE.get()) && Boolean.TRUE.equals(stack.get(ComponentManager.SAVE_DISTANCE.get()))) {
                if (stack.has(ComponentManager.DISTANCE_MOVED.get())) {
                    float distanceMoved = Objects.requireNonNullElse(stack.get(ComponentManager.DISTANCE_MOVED.get()), 0.0F);
                    MutableComponent msg = Component.translatable("item.bikesarepain.bicycle.tooltip.distance_moved")
                            .withColor(CommonColors.GRAY);

                    if (distanceMoved >= 1000) {
                        int km = (int) (distanceMoved / 1000);
                        int m = (int) (distanceMoved % 1000);
                        lines.add(
                                msg.append(Component.literal(km + "km " + m + "m")
                                        .withColor(CommonColors.GREEN))
                        );
                    } else {
                        lines.add(
                                msg.append(Component.literal(distanceMoved + "m")
                                        .withColor(CommonColors.GREEN))
                        );
                    }
                }
            }

            if (stack.getItem() instanceof BikeItem) {
                int durability = stack.getDamageValue();
                int maxDurability = stack.getMaxDamage();

                // We will show a percentage of durability left
                int durabilityPercentage = (int) (100 - (((float) durability / (float) maxDurability) * 100));

                if (durabilityPercentage == 100) {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.brand_new")
                            .withColor(CommonColors.GREEN));
                } else {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.repair")
                            .withColor(CommonColors.GREEN));
                }

                // Check if contains a saddle
                if (stack.has(ComponentManager.SADDLED.get()) && Boolean.TRUE.equals(stack.get(ComponentManager.SADDLED.get()))) {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.saddled")
                            .withColor(CommonColors.GRAY).append(Component.translatable("item.bikesarepain.bicycle.tooltip.yes")
                                    .withColor(Color.ofRGB(255, 149, 0).argbInt())));
                } else {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.saddled")
                            .withColor(CommonColors.GRAY).append(Component.translatable("item.bikesarepain.bicycle.tooltip.no")
                                    .withColor(CommonColors.RED)));
                }

                lines.add(2, Component.translatable("item.bikesarepain.bicycle.tooltip.health")
                                .withColor(CommonColors.GRAY)
                        .append(Component.literal(durabilityPercentage + "%")
                                .withStyle(durabilityPercentage == 100 ? net.minecraft.ChatFormatting.GREEN
                                        : durabilityPercentage > 75 ? net.minecraft.ChatFormatting.DARK_GREEN
                                        : durabilityPercentage > 50 ? net.minecraft.ChatFormatting.YELLOW
                                        : durabilityPercentage > 25 ? net.minecraft.ChatFormatting.RED
                                        : net.minecraft.ChatFormatting.DARK_RED)));
            }
        });
    }
}
