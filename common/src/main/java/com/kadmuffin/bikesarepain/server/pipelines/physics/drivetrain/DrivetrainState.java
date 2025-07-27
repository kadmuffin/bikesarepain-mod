package com.kadmuffin.bikesarepain.server.pipelines.physics.drivetrain;

import com.kadmuffin.bikesarepain.server.interfaces.IStateComponent;

public record DrivetrainState(
        float maxPedalTorque,
        float drivetrainEfficiency,
        int chainringTeeth,
        int sprocketTeeth,
        float cadenceRpm,
        float optimalCadenceRpm,
        float maxCadenceDrop,
        float riderPowerWatts,
        float minSpeedMps
) implements IStateComponent {

    public static DrivetrainState defaultState() {
        return new DrivetrainState(40f, 0.97f, 44, 16, 80f, 90f, 0.3f, 300f, 0.1f);
    }
}