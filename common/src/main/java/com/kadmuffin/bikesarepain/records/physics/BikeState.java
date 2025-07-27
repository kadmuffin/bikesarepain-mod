package com.kadmuffin.bikesarepain.records.physics;

import com.kadmuffin.bikesarepain.server.interfaces.IStateComponent;

public record BikeState(
        ScaledInput playerInput, // from -1 to 1
        float totalMassKg,
        float currentSpeedMps,
        boolean isBraking,
        float gravityMps2,
        float bikePitchRad
        ) implements IStateComponent {
}
