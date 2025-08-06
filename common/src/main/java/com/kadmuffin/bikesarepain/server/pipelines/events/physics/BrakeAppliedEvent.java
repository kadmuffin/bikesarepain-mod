package com.kadmuffin.bikesarepain.server.pipelines.events.physics;

import com.kadmuffin.bikesarepain.server.interfaces.PipelineEvent;

public record BrakeAppliedEvent(float newtonsForce) implements PipelineEvent {
}
