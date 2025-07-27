package com.kadmuffin.bikesarepain.server.pipelines.event;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.EventListener;
import com.kadmuffin.bikesarepain.server.interfaces.PipelineEvent;

public abstract class TypedEventListener<T extends PipelineEvent> implements EventListener {
    private final Class<T> eventType;

    protected TypedEventListener(Class<T> eventType) {
        this.eventType = eventType;
    }

    @Override
    public final void handleEvent(PipelineEvent event, BikeState state, AbstractBike bike) {
        if (eventType.isInstance(event)) {
            handleSpecificEvent(eventType.cast(event), state, bike);
        }
    }

    public abstract void handleSpecificEvent(T event, BikeState state, AbstractBike bike);
}