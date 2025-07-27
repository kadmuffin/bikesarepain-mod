package com.kadmuffin.bikesarepain.server.pipelines.events.physics;

import com.kadmuffin.bikesarepain.server.interfaces.IGameEvent;


public record PedalTorqueEvent(float wheelAngularMomentum, float wheelTorque, float cadenceRPM) implements IGameEvent {
    float angularMomentumDegrees() {
        return (float) Math.toDegrees(wheelAngularMomentum);
    }
}
