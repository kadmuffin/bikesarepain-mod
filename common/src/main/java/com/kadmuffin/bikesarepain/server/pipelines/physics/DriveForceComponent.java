package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IForceComponent;

public class DriveForceComponent implements IForceComponent {
    private static final float MAX_PEDAL_TORQUE     = 40f;  // N·m
    private static final float DRIVETRAIN_EFFICIENCY= 0.97f;
    private static final int   CHAINRING_TEETH      = 44;
    private static final int   SPROCKET_TEETH       = 16;

    private static final float OPTIMAL_CADENCE_RPM  = 90f;
    private static final float MAX_CADENCE_DROP     = 0.3f;

    private static final float RIDER_POWER_WATTS    = 300f;
    private static final float MIN_SPEED_MPS        = 0.1f;

    @Override public String getID() { return "DriveForce"; }

    @Override
    public float calculateForce(BikeState state, AbstractBike bike) {
        float input = state.playerInput().forward();
        if (input <= 0f) input = Math.abs(input);

        float cad       = state.cadenceRPM();
        float force = getForce(bike, cad, input);

        float speed       = state.currentSpeed();
        float effSpeed    = Math.max(speed, MIN_SPEED_MPS);
        float maxForce    = (RIDER_POWER_WATTS * input) / effSpeed;

        if (state.playerInput().forward() <= 0f) return -Math.min(force, maxForce);

        return Math.min(force, maxForce);
    }

    private static float getForce(AbstractBike bike, float cad, float input) {
        float dev       = Math.abs(cad - OPTIMAL_CADENCE_RPM) / OPTIMAL_CADENCE_RPM;
        float cadenceF  = 1f - MAX_CADENCE_DROP * dev;
        cadenceF        = Math.max(1f - MAX_CADENCE_DROP, cadenceF);

        float pedalTorque = MAX_PEDAL_TORQUE * input * cadenceF;

        float gearRatio   = (float)CHAINRING_TEETH / SPROCKET_TEETH;
        float wheelTorque = pedalTorque * gearRatio * DRIVETRAIN_EFFICIENCY;

        return wheelTorque / bike.getWheelRadius();
    }
}
