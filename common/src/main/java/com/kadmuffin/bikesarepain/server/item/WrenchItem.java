package com.kadmuffin.bikesarepain.server.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WrenchItem extends BaseItem {
    public WrenchItem(ResourceLocation model, Properties properties) {
        super(model, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.bikesarepain.wrench.tooltip"));
    }
}
