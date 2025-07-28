package com.kadmuffin.bikesarepain.server.pipelines.physics.drivetrain;

import com.kadmuffin.bikesarepain.server.interfaces.StateComponent;

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
) implements StateComponent {

    public static DrivetrainState defaultState() {
        return new DrivetrainState(40f, 0.97f, 44, 16, 80f, 90f, 0.3f, 300f, 0.1f);
    }

    public DrivetrainState withMaxPedalTorque(float maxPedalTorque) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withDrivetrainEfficiency(float drivetrainEfficiency) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withChainringTeeth(int chainringTeeth) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withSprocketTeeth(int sprocketTeeth) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withCadenceRpm(float cadenceRpm) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withOptimalCadenceRpm(float optimalCadenceRpm) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withMaxCadenceDrop(float maxCadenceDrop) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withRiderPowerWatts(float riderPowerWatts) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }

    public DrivetrainState withMinSpeedMps(float minSpeedMps) {
        return new DrivetrainState(maxPedalTorque, drivetrainEfficiency, chainringTeeth, sprocketTeeth,
                cadenceRpm, optimalCadenceRpm, maxCadenceDrop, riderPowerWatts, minSpeedMps);
    }
}