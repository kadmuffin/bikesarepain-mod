package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IForceComponent;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import com.kadmuffin.bikesarepain.server.pipelines.events.physics.PedalTorqueEvent;

public class DriveForceComponent implements IForceComponent {
    private static final float MAX_PEDAL_TORQUE = 40f;  // N·m
    private static final float DRIVETRAIN_EFFICIENCY = 0.97f;
    private static final int CHAINRING_TEETH = 44;
    private static final int SPROCKET_TEETH = 16;

    private static final float OPTIMAL_CADENCE_RPM = 90f;
    private static final float MAX_CADENCE_DROP = 0.3f;

    private static final float RIDER_POWER_WATTS = 300f;
    private static final float MIN_SPEED_MPS = 0.1f;

    private record ForceResult(float force, float wheelTorque) {}

    private static ForceResult getForce(AbstractBike bike, float cad, float input) {
        float dev = Math.abs(cad - OPTIMAL_CADENCE_RPM) / OPTIMAL_CADENCE_RPM;
        float cadenceF = 1f - MAX_CADENCE_DROP * dev;
        cadenceF = Math.max(1f - MAX_CADENCE_DROP, cadenceF);

        float pedalTorque = MAX_PEDAL_TORQUE * input * cadenceF;

        float gearRatio = (float) CHAINRING_TEETH / SPROCKET_TEETH;
        float wheelTorque = pedalTorque * gearRatio * DRIVETRAIN_EFFICIENCY;

        return new ForceResult(wheelTorque / bike.getWheelRadius(), wheelTorque);
    }

    @Override
    public String getID() {
        return "DriveForce";
    }

    @Override
    public float calculateForce(BikeState state, AbstractBike bike, EventHandler event) {
        float input = state.playerInput().forward();
        if (input <= 0f) return 0f;

        float cad = state.cadenceRPM();
        ForceResult result = getForce(bike, cad, input);

        float speed = state.currentSpeed();
        float effSpeed = Math.max(speed, MIN_SPEED_MPS);
        float maxForce = (RIDER_POWER_WATTS * input) / effSpeed;

        float wheelAngularMomentum = state.currentSpeed() / bike.getWheelRadius();
        event.publish(new PedalTorqueEvent(wheelAngularMomentum, result.wheelTorque, state.cadenceRPM()), state, bike);

        return Math.min(result.force, maxForce);
    }
}
