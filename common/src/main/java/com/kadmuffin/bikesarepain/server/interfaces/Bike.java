package com.kadmuffin.bikesarepain.server.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Supplier;

public interface Bike {
    <T extends StateComponent> void registerStateFactory(Class<T> type, Supplier<T> factory);
    <T extends StateComponent> boolean unregisterStateFactory(Class<T> type);
    <T extends StateComponent> Optional<T> getStateComponent(Class<T> componentType);

    float getYRot();
    float getSpeed();
    float getWheelRadius();
    boolean onGround();
    boolean isBraking();
    BlockPos getBlockPosBelowThatAffectsMyMovement();
    Vec3 getDeltaMovement();
    Level level();
}
