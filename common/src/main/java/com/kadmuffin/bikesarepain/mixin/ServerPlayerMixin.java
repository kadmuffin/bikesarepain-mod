package com.kadmuffin.bikesarepain.mixin;

import com.kadmuffin.bikesarepain.server.StatsManager;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow public abstract void awardStat(Stat<?> stat, int increment);

    @Inject(
            method = "checkRidingStatistics(DDD)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/stats/Stats;HORSE_ONE_CM:Lnet/minecraft/resources/ResourceLocation;",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void onGetVehicle(double dx, double dy, double dz, CallbackInfo ci, @Local Entity entity, @Local int i) {
        if (entity instanceof AbstractBike) {
            this.awardStat(Stats.CUSTOM.get(StatsManager.DISTANCE_TRAVELED), i);
            ci.cancel();
        }
    }
}
