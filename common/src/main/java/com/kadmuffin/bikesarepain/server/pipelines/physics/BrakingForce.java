package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.Bike;
import com.kadmuffin.bikesarepain.server.interfaces.GenericForce;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import com.kadmuffin.bikesarepain.server.pipelines.events.physics.BrakeAppliedEvent;

public class BrakingForce implements GenericForce {
    @Override
    public String getID() {
        return "BrakingForce";
    }

    @Override
    public float calculateForce(BikeState currentState, Bike entity, EventHandler event) {
        if (!(entity instanceof AbstractBike bike)) return 0f;

        if (currentState.isBraking() && bike.onGround()) {
            float force = (float) (currentState.totalMassKg() * currentState.currentSpeedMps() * (Math.exp(-bike.getBrakeMultiplier() * 0.25F) - 1));
            event.publish(new BrakeAppliedEvent(force), currentState, entity);
            return force;
        }
        return 0F;
    }
}
