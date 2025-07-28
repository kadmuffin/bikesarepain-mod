package com.kadmuffin.bikesarepain.records.physics;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.minecraft.core.BlockPos;

public record SurfaceData(float normalForce, float effectiveRollingCoeff, float effectiveStaticCoeff) {

    public static SurfaceData buildFrom(BikeState state, AbstractBike bike) {
        BlockPos blockPos = bike.getBlockPosBelowThatAffectsMyMovement();
        float blockFrictionCoeff = 1 / bike.level()
                .getBlockState(blockPos)
                .getBlock()
                .getFriction();

        blockFrictionCoeff = Math.clamp(blockFrictionCoeff, 0.5f, 5.0f);

        float normalForce = state.totalMassKg() * state.gravityMps2();
        float effectiveRollingCoeff = 0.008f * blockFrictionCoeff;
        float effectiveStaticCoeff = 0.1f * blockFrictionCoeff;

        return new SurfaceData(normalForce, effectiveRollingCoeff, effectiveStaticCoeff);
    }
}
