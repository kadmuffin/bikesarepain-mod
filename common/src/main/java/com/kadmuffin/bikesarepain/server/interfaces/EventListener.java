package com.kadmuffin.bikesarepain.server.interfaces;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;

public interface EventListener {
    void handleEvent(PipelineEvent event, BikeState state, Bike bike);
}
