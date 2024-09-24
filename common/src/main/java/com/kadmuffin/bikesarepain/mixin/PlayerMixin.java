package com.kadmuffin.bikesarepain.mixin;

import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements PlayerAccessor {
    // For arduino link
    @Unique
    private static final EntityDataAccessor<Boolean> BAPAIN_JSC_ACTIVE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Float> BAPAIN_JSC_SPEED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BAPAIN_JSC_REAL_SPEED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BAPAIN_JSC_DISTANCE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BAPAIN_JSC_CALORIES = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BAPAIN_JSC_WHEELRADIUS = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Integer> BAPAIN_JSC_TIMESINCEUPDATE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BAPAIN_JSC_TIME_PEDALLED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);


    @Inject(at = @At("TAIL"), method = "defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V")
    protected void afterDefineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BAPAIN_JSC_ACTIVE, false);
        builder.define(BAPAIN_JSC_SPEED, 0.0F);
        builder.define(BAPAIN_JSC_REAL_SPEED, 0.0F);
        builder.define(BAPAIN_JSC_DISTANCE, 0.0F);
        builder.define(BAPAIN_JSC_CALORIES, 0.0F);
        builder.define(BAPAIN_JSC_WHEELRADIUS, 0.0F);
        builder.define(BAPAIN_JSC_TIMESINCEUPDATE, 0);
        builder.define(BAPAIN_JSC_TIME_PEDALLED, 0);
    }

    @Unique
    public boolean bikesarepain$isJSCActive() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_ACTIVE);
    }

    @Unique
    public void bikesarepain$setJSCActive(boolean active) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_ACTIVE, active);
    }

    @Unique
    public float bikesarepain$getJSCSpeed() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_SPEED);
    }

    @Unique
    public void bikesarepain$setJSCSpeed(float speed) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_SPEED, speed);
    }

    @Unique
    public float bikesarepain$getJSCRealSpeed() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_REAL_SPEED);
    }

    @Unique
    public void bikesarepain$setJSCRealSpeed(float speed) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_REAL_SPEED, speed);
    }

    @Unique
    public float bikesarepain$getJSCDistance() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_DISTANCE);
    }

    @Unique
    public void bikesarepain$setJSCDistance(float distance) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_DISTANCE, distance);
    }

    @Unique
    public float bikesarepain$getJSCCalories() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_CALORIES);
    }

    @Unique
    public void bikesarepain$setJSCCalories(float calories) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_CALORIES, calories);
    }

    @Unique
    public float bikesarepain$getJSCWheelRadius() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_WHEELRADIUS);
    }

    @Unique
    public void bikesarepain$setJSCWheelRadius(float wheelRadius) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_WHEELRADIUS, wheelRadius);
    }

    @Unique
    public int bikesarepain$getJSCSinceUpdate() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_TIMESINCEUPDATE);
    }

    @Unique
    public void bikesarepain$setJSCSinceUpdate(int timesinceupdate) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_TIMESINCEUPDATE, timesinceupdate);
    }

    @Unique
    public int bikesarepain$getJSCTimePedalled() {
        return ((Player) (Object) this).getEntityData().get(BAPAIN_JSC_TIME_PEDALLED);
    }

    @Unique
    public void bikesarepain$setJSCTimePedalled(int timePedalled) {
        ((Player) (Object) this).getEntityData().set(BAPAIN_JSC_TIME_PEDALLED, timePedalled);
    }
}

