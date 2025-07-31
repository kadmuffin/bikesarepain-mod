package com.kadmuffin.bikesarepain.server.pipelines.event;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.Bike;
import com.kadmuffin.bikesarepain.server.interfaces.EventListener;
import com.kadmuffin.bikesarepain.server.interfaces.PipelineEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler {
    private final Map<Class<? extends PipelineEvent>, List<EventListener>> subscribers = new HashMap<>();

    public void subscribe(Class<? extends PipelineEvent> eventType, EventListener listener) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>());
        subscribers.get(eventType).add(listener);
    }

    public void unsubscribe(Class<? extends PipelineEvent> eventType, EventListener listener) {
        List<EventListener> list = subscribers.get(eventType);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                subscribers.remove(eventType);
            }
        }
    }

    public void publish(PipelineEvent event, BikeState state, Bike bike) {
        // Find all subscribers for this event's specific type
        List<EventListener> listeners = subscribers.get(event.getClass());
        if (listeners != null) {
            // Tell each listener to handle the event
            for (EventListener listener : listeners) {
                listener.handleEvent(event, state, bike);
            }
        }
    }
}
