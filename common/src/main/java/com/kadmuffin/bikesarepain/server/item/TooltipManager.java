package com.kadmuffin.bikesarepain.server.item;

import dev.architectury.event.events.client.ClientTooltipEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class TooltipManager {
    public static void init() {
        ClientTooltipEvent.ITEM.register((stack, lines, context, tooltipFlag) -> {
            if (stack.getItem() instanceof BikeItem) {
                int durability = stack.getDamageValue();
                int maxDurability = stack.getMaxDamage();

                // We will show a percentage of durability left
                int durabilityPercentage = (int) (100 - (((float) durability / (float) maxDurability) * 100));

                if (durabilityPercentage == 100) {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.brand_new")
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.repair")
                            .withStyle(ChatFormatting.GOLD));
                }

                // Check if contains a saddle
                if (stack.has(ItemManager.SADDLED.get()) && Boolean.TRUE.equals(stack.get(ItemManager.SADDLED.get()))) {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.saddled")
                            .withColor(CommonColors.GRAY).append(Component.translatable("item.bikesarepain.bicycle.tooltip.yes")
                                    .withColor(Color.ofRGB(255, 149, 0).argbInt())));
                } else {
                    lines.add(Component.translatable("item.bikesarepain.bicycle.tooltip.saddled")
                            .withColor(CommonColors.GRAY).append(Component.translatable("item.bikesarepain.bicycle.tooltip.no")
                                    .withColor(CommonColors.RED)));
                }

                if (stack.has(ItemManager.SAVE_TIME.get()) && Boolean.TRUE.equals(stack.get(ItemManager.SAVE_TIME.get()))) {
                    if (stack.has(ItemManager.TICKS_MOVED.get())) {
                        // Unpack in safe way
                        float ticksMoved = Objects.requireNonNullElse(stack.get(ItemManager.TICKS_MOVED.get()), 0);
                        ticksMoved /= 20.0F; // Convert to seconds
                        if (ticksMoved >= 60) {
                            int minutes = (int) (ticksMoved / 60);
                            int seconds = (int) (ticksMoved % 60);
                            lines.add(
                                    Component.translatable("item.bikesarepain.bicycle.tooltip.time_pedalled")
                                            .withColor(CommonColors.GRAY)
                                            .append(Component.literal(minutes + "m " + seconds + "s")
                                                    .withStyle(ChatFormatting.GREEN))
                            );
                        } else {
                            lines.add(
                                    Component.translatable("item.bikesarepain.bicycle.tooltip.time_pedalled")
                                            .withColor(CommonColors.GRAY)
                                            .append(Component.literal(ticksMoved + "s")
                                                    .withStyle(ChatFormatting.GREEN))
                            );
                        }
                    }
                }

                if (stack.has(ItemManager.SAVE_DISTANCE.get()) && Boolean.TRUE.equals(stack.get(ItemManager.SAVE_DISTANCE.get()))) {
                    if (stack.has(ItemManager.DISTANCE_MOVED.get())) {
                        float distanceMoved = Objects.requireNonNullElse(stack.get(ItemManager.DISTANCE_MOVED.get()), 0.0F);
                        MutableComponent msg = Component.translatable("item.bikesarepain.bicycle.tooltip.distance_moved")
                                .withColor(CommonColors.GRAY);

                        if (distanceMoved >= 1000) {
                            int km = (int) (distanceMoved / 1000);
                            int m = (int) (distanceMoved % 1000);
                            lines.add(
                                    msg.append(Component.literal(km + "km " + m + "m")
                                            .withStyle(ChatFormatting.GREEN))
                            );
                        } else {
                            lines.add(
                                    msg.append(Component.literal(distanceMoved + "m")
                                            .withStyle(ChatFormatting.GREEN))
                            );
                        }
                    }
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
