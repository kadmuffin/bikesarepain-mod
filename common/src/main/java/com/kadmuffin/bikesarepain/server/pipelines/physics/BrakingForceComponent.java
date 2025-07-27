package com.kadmuffin.bikesarepain.server.pipelines.physics;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IForceComponent;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import com.kadmuffin.bikesarepain.server.pipelines.events.physics.BrakeAppliedEvent;

public class BrakingForceComponent implements IForceComponent {
    @Override
    public String getID() {
        return "BrakingForce";
    }

    @Override
    public float calculateForce(BikeState currentState, AbstractBike bike, EventHandler event) {
        if (currentState.isBraking() && bike.onGround()) {
            return (float) (currentState.totalMass() * currentState.currentSpeed() * (Math.exp(-bike.getBrakeMultiplier() * 0.25F) - 1));
        }
        return 0F;
    }
}
