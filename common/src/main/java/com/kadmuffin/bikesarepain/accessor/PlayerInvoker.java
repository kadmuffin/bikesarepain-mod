package com.kadmuffin.bikesarepain.accessor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerInvoker {
    @Invoker("awardStat")
    void awardStat(ResourceLocation stat, int increment);

}
