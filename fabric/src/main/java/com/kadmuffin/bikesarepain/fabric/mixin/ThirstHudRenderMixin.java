package com.kadmuffin.bikesarepain.fabric.mixin;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.dehydration.thirst.ThirstHudRender;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ThirstHudRender.class)
public class ThirstHudRenderMixin {

    @Inject(method = "getHeartCount(Lnet/minecraft/world/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    private static void inGetHeartCount(Entity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof AbstractBike) {
            cir.setReturnValue(0);
        }
    }
}