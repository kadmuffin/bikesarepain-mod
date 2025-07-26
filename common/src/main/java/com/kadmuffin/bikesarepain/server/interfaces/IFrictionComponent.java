package com.kadmuffin.bikesarepain.server.interfaces;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;

public interface IFrictionComponent {
    String getID();
    float calculateForce(BikeState currentState, AbstractBike bike, float nonFrictionNetForce);
}
