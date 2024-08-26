package com.kadmuffin.bikesarepain.server.item;

import dev.architectury.event.events.client.ClientTooltipEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import software.bernie.geckolib.util.Color;

public class TooltipManager {
    public static void init() {
        ClientTooltipEvent.ITEM.register((stack, lines, context, tooltipFlag) -> {
            if (stack.getItem() instanceof BikeItem) {
                int durability = stack.getDamageValue();
                int maxDurability = stack.getMaxDamage();

                // We will show a percentage of durability left
                int durabilityPercentage = (int) (100 - (((float) durability / (float) maxDurability) * 100));

                if (durabilityPercentage == 100) {
                    lines.add(Component.translatable("item.bikesarepain.bicycle_item.tooltip.brand_new")
                            .withColor(CommonColors.GREEN));
                } else {
                    lines.add(Component.translatable("item.bikesarepain.bicycle_item.tooltip.repair")
                            .withColor(CommonColors.GREEN));
                }

                // Check if contains a saddle
                if (stack.has(ItemManager.SADDLED.get()) && Boolean.TRUE.equals(stack.get(ItemManager.SADDLED.get()))) {
                    lines.add(Component.translatable("item.bikesarepain.bicycle_item.tooltip.saddled")
                            .withColor(CommonColors.GRAY).append(Component.translatable("item.bikesarepain.bicycle_item.tooltip.yes")
                                    .withColor(Color.ofRGB(255, 149, 0).argbInt())));
                } else {
                    lines.add(Component.translatable("item.bikesarepain.bicycle_item.tooltip.saddled")
                            .withColor(CommonColors.GRAY).append(Component.translatable("item.bikesarepain.bicycle_item.tooltip.no")
                                    .withColor(CommonColors.RED)));
                }


                lines.add(2, Component.translatable("item.bikesarepain.bicycle_item.tooltip.health")
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
