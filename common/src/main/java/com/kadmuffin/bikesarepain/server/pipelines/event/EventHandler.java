package com.kadmuffin.bikesarepain.server.pipelines.event;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IEventListener;
import com.kadmuffin.bikesarepain.server.interfaces.IGameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler {
    private final Map<Class<? extends IGameEvent>, List<IEventListener>> subscribers = new HashMap<>();

    public void subscribe(Class<? extends IGameEvent> eventType, IEventListener listener) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>());
        subscribers.get(eventType).add(listener);
    }

    public void unsubscribe(Class<? extends IGameEvent> eventType, IEventListener listener) {
        List<IEventListener> list = subscribers.get(eventType);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                subscribers.remove(eventType);
            }
        }
    }

    public void publish(IGameEvent event, BikeState state, AbstractBike bike) {
        // Find all subscribers for this event's specific type
        List<IEventListener> listeners = subscribers.get(event.getClass());
        if (listeners != null) {
            // Tell each listener to handle the event
            for (IEventListener listener : listeners) {
                listener.handleEvent(event, state, bike);
            }
        }
    }
}
