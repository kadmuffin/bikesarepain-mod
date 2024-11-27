package com.kadmuffin.bikesarepain.mixin;

import com.kadmuffin.bikesarepain.accessor.EntityInvoker;
import com.kadmuffin.bikesarepain.accessor.PlayerInvoker;
import com.kadmuffin.bikesarepain.server.StatsManager;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerInvoker, EntityInvoker {

    @Inject(
            method = "checkRidingStatistics(DDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getVehicle()Lnet/minecraft/world/entity/Entity;"
            ),
            cancellable = true)
    private void beforeAbstractHorseCheck(double dx, double dy, double dz, CallbackInfo ci) {
        Entity entity = this.getVehicle();
        if (entity instanceof AbstractBike) {
            this.awardStat(StatsManager.DISTANCE_TRAVELED, Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F));
            ci.cancel();
        }
    }
}
