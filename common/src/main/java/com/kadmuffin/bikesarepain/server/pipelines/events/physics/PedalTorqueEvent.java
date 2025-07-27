package com.kadmuffin.bikesarepain.server.pipelines.events.physics;

import com.kadmuffin.bikesarepain.server.interfaces.PipelineEvent;


public record PedalTorqueEvent(float wheelAngularMomentum, float wheelTorque, float cadenceRPM) implements PipelineEvent {
    float angularMomentumDegrees() {
        return (float) Math.toDegrees(wheelAngularMomentum);
    }
}
