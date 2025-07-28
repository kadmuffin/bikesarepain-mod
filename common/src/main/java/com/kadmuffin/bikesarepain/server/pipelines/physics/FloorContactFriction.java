package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.records.physics.SurfaceData;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.SlidingFriction;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import net.minecraft.core.BlockPos;

public class FloorContactFriction implements SlidingFriction {

    @Override
    public String getID() {
        return "FloorContactFriction";
    }

    @Override
    public float calculateKineticForce(BikeState currentState, AbstractBike bike, SurfaceData data, EventHandler event) {
        if (!bike.onGround()) {
            return 0.0f;
        }

        float direction = Math.signum(currentState.currentSpeedMps());
        float rollingForce = data.effectiveRollingCoeff() * data.normalForce();
        return -direction * rollingForce;
    }

    @Override
    public float calculateMaxStaticForce(BikeState currentState, AbstractBike bike, SurfaceData data, EventHandler event) {
        if (!bike.onGround()) {
            return 0.0f;
        }

        return data.effectiveStaticCoeff() * data.normalForce();
    }
}