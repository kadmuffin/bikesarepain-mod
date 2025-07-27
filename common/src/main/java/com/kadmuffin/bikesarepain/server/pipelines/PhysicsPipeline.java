package com.kadmuffin.bikesarepain.server.pipelines;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.IForceComponent;
import com.kadmuffin.bikesarepain.server.interfaces.IFrictionComponent;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class PhysicsPipeline {
    public static final float ticksPerSecond = 20f;
    public static final float deltaSeconds = 1 / ticksPerSecond;
    private final List<IForceComponent> forceComponents;
    private final List<IFrictionComponent> frictionComponents;

    public PhysicsPipeline(List<IForceComponent> forceComponents, List<IFrictionComponent> frictionComponents) {
        this.forceComponents = new ArrayList<>(forceComponents);
        this.frictionComponents = new ArrayList<>(frictionComponents);
    }

    public void addForceComponent(IForceComponent component) {
        this.forceComponents.add(component);
    }

    public void removeForceComponent(Class<? extends IForceComponent> componentType) {
        this.forceComponents.removeIf(componentType::isInstance);
    }

    public void addFrictionComponent(IForceComponent component) {
        this.forceComponents.add(component);
    }

    public void removeFrictionComponent(Class<? extends IForceComponent> componentType) {
        this.forceComponents.removeIf(componentType::isInstance);
    }

    public float calculateNonFrictionForce(BikeState currentState, AbstractBike bike, EventHandler event) {
        float netForce = 0.0f;
        for (IForceComponent component : this.forceComponents) {
            float thisForce = component.calculateForce(currentState, bike, event);
            netForce += thisForce;
            System.out.println(component.getID() + ": " + thisForce);
        }
        return netForce;
    }

    public float calculateNetFriction(BikeState currentState, AbstractBike bike, EventHandler event, float nonFrictionNetForce) {
        float netForce = 0.0f;
        for (IFrictionComponent component : this.frictionComponents) {
            float thisForce = component.calculateForce(currentState, bike, event, nonFrictionNetForce);
            netForce += thisForce;
            System.out.println(component.getID() + ": " + thisForce);
        }
        return netForce;
    }

    public float calculateNetForce(BikeState currentState, AbstractBike bike, EventHandler event) {
        float nonFrNetForce = this.calculateNonFrictionForce(currentState, bike, event);
        return nonFrNetForce + this.calculateNetFriction(currentState, bike, event, nonFrNetForce);
    }
}
