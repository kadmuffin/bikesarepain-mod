package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IFrictionComponent;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import net.minecraft.core.BlockPos;

public class FloorFrictionComponent implements IFrictionComponent {

    private static final float BASE_ROLLING_COEFF = 0.008f;
    private static final float BASE_STATIC_COEFF = 0.1f;

    @Override
    public String getID() {
        return "FloorFriction";
    }

    @Override
    public float calculateForce(BikeState currentState, AbstractBike bike, EventHandler event, float nonFrictionNetForce) {
        // No friction if not on ground
        if (!bike.onGround()) {
            return 0.0f;
        }

        BlockPos blockPos = bike.getBlockPosBelowThatAffectsMyMovement();
        float blockFrictionCoeff = bike.level().getBlockState(blockPos)
                .getBlock().getFriction() + ((float) Math.random() * 0.1f);

        System.out.println("Block friction: " + blockFrictionCoeff);

        float mass = currentState.totalMass();
        float gravity = currentState.gravity();
        float normalForce = mass * gravity;  // N = m * g
        float speed = currentState.currentSpeed();

        // Scale coefficients by block friction
        float rollingCoeff = BASE_ROLLING_COEFF * (blockFrictionCoeff);
        float staticCoeff = BASE_STATIC_COEFF * blockFrictionCoeff;
        if (speed < 2f) {
            rollingCoeff *= 7f;
        }

        // Stationary: apply static friction if drive force insufficient
        if (speed < 0.3f) {
            float maxStatic = staticCoeff * normalForce;
            if (Math.abs(nonFrictionNetForce) < maxStatic) {
                // Apply enough force to decelerate, else, the bike never stops moving
                return -Math.signum(speed) * maxStatic;
            }
            // Non friction forces overcome static force
        }

        float direction = (speed != 0f) ? Math.signum(speed) : Math.signum(nonFrictionNetForce);
        float rollingForce = rollingCoeff * normalForce;
        System.out.println("RollingCoeff: " + rollingCoeff + " Normal: " + normalForce + " RollingForce: " + rollingForce);
        return -direction * rollingForce;
    }
}