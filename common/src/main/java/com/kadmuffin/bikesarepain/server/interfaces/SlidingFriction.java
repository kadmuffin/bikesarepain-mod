package com.kadmuffin.bikesarepain.server.interfaces;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.records.physics.SurfaceData;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;

public interface SlidingFriction extends ForceSource {
    float calculateKineticForce(BikeState currentState, AbstractBike bike, SurfaceData data, EventHandler event);
    float calculateMaxStaticForce(BikeState currentState, AbstractBike bike, SurfaceData data, EventHandler event);
}
