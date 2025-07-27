package com.kadmuffin.bikesarepain.server.pipelines.event;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IEventListener;
import com.kadmuffin.bikesarepain.server.interfaces.IGameEvent;

public abstract class EventListener<T extends IGameEvent> implements IEventListener {
    private final Class<T> eventType;

    protected EventListener(Class<T> eventType) {
        this.eventType = eventType;
    }

    @Override
    public final void handleEvent(IGameEvent event, BikeState state, AbstractBike bike) {
        if (eventType.isInstance(event)) {
            handleSpecificEvent(eventType.cast(event), state, bike);
        }
    }

    public abstract void handleSpecificEvent(T event, BikeState state, AbstractBike bike);
}