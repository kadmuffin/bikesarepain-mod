package com.kadmuffin.bikesarepain.server.pipelines.events.physics;

import com.kadmuffin.bikesarepain.server.interfaces.IGameEvent;

public record BrakeAppliedEvent(float newtonsForce) implements IGameEvent {
}
