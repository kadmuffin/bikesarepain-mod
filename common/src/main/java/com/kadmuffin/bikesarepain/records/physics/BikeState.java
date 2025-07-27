package com.kadmuffin.bikesarepain.records.physics;

public record BikeState(
        ScaledInput playerInput,
        float totalMass,
        float currentSpeed,
        boolean isBraking,
        float gravity,
        float bikePitch
        ) {
}
