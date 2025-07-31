package com.kadmuffin.bikesarepain.server.pipelines.physics.drivetrain;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.Bike;
import com.kadmuffin.bikesarepain.server.interfaces.GenericForce;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import com.kadmuffin.bikesarepain.server.pipelines.events.physics.PedalTorqueEvent;

public class DrivetrainForce implements GenericForce {
    private record ForceResult(float force, float wheelTorque) {}

    private static ForceResult getForce(Bike bike, DrivetrainState drivetrain, float input) {
        float dev = Math.abs(drivetrain.cadenceRpm() - drivetrain.optimalCadenceRpm()) / drivetrain.optimalCadenceRpm();
        float cadenceF = 1f - drivetrain.maxCadenceDrop() * dev;
        cadenceF = Math.max(1f - drivetrain.maxCadenceDrop(), cadenceF);

        float pedalTorque = drivetrain.maxPedalTorque() * input * cadenceF;

        float gearRatio = (float) drivetrain.chainringTeeth() / drivetrain.sprocketTeeth();
        float wheelTorque = pedalTorque * gearRatio * drivetrain.drivetrainEfficiency();

        return new ForceResult(wheelTorque / bike.getWheelRadius(), wheelTorque);
    }

    @Override
    public String getID() {
        return "DriveForce";
    }

    @Override
    public float calculateForce(BikeState state, Bike bike, EventHandler event) {
        DrivetrainState drivetrain = bike.getStateComponent(DrivetrainState.class)
                .orElse(null);
        if (drivetrain == null) return 0f;

        float input = state.playerInput().forward();
        if (input <= 0f) return 0f;

        ForceResult result = getForce(bike, drivetrain, input);

        float speed = state.currentSpeedMps();
        float effSpeed = Math.max(speed, drivetrain.minSpeedMps());
        float maxForce = (drivetrain.riderPowerWatts() * input) / effSpeed;

        float wheelAngularMomentum = state.currentSpeedMps() / bike.getWheelRadius();
        event.publish(new PedalTorqueEvent(wheelAngularMomentum, result.wheelTorque, drivetrain.cadenceRpm()), state, bike);

        return Math.min(result.force, maxForce);
    }
}
