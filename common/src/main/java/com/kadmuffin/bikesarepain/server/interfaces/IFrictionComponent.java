package com.kadmuffin.bikesarepain.server.interfaces;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;

public interface IFrictionComponent {
    String getID();
    float calculateForce(BikeState currentState, AbstractBike bike, EventHandler event, float nonFrictionNetForce);
}
