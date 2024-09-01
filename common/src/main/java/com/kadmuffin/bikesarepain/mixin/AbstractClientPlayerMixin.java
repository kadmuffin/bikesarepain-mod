package com.kadmuffin.bikesarepain.mixin;

import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Inject(at = @At("TAIL"), method = "getFieldOfViewModifier()F", cancellable = true)
    public void inGetFieldOfViewModifier(CallbackInfoReturnable<Float> cir, @Local float f) {
        AbstractClientPlayer playerAbs = ((AbstractClientPlayer) (Object) this);
        Entity vehicle = playerAbs.getVehicle();
        if (vehicle instanceof Bicycle) {
            float fov = ((Bicycle) vehicle).modifyFOV(playerAbs, f);
            cir.setReturnValue(fov);
        }
    }
}
