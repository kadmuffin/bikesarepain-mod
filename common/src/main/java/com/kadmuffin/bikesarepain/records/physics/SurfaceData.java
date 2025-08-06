package com.kadmuffin.bikesarepain.records.physics;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.Bike;
import net.minecraft.core.BlockPos;

public record SurfaceData(BlockPos blockPos, float normalForce, float effectiveRollingCoeff, float effectiveStaticCoeff) {

    public static SurfaceData buildFrom(BikeState state, Bike bike) {
        BlockPos blockPos = bike.getBlockPosBelowThatAffectsMyMovement();
        float blockFrictionCoeff = bike.level()
                .getBlockState(blockPos)
                .getBlock()
                .getFriction();

        blockFrictionCoeff = Math.clamp(blockFrictionCoeff, 0.05f, 5.0f);

        float normalForce = state.totalMassKg() * state.gravityMps2();
        float effectiveRollingCoeff = 0.008f * blockFrictionCoeff;
        float effectiveStaticCoeff = 1.5f * effectiveRollingCoeff * blockFrictionCoeff;

        return new SurfaceData(blockPos, normalForce, effectiveRollingCoeff, effectiveStaticCoeff);
    }
}
