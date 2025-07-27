package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.GenericForce;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;

public class AirDragForce implements GenericForce {
    private final float dragCoefficient;
    private final float frontalArea;

    public AirDragForce(float dragCoefficient, float frontalArea) {
        this.dragCoefficient = dragCoefficient;
        this.frontalArea = frontalArea;
    }

    @Override
    public String getID() {
        return "AirDragForce";
    }

    @Override
    public float calculateForce(BikeState currentState, AbstractBike bike, EventHandler event) {
        float v = currentState.currentSpeedMps();
        float airDensity = 1.225f;
        float drag = 0.5f * airDensity * dragCoefficient * frontalArea * v * v;
        return -Math.signum(v) * drag;
    }
}
