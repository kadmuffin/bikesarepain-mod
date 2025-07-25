package com.kadmuffin.bikesarepain.server.pipelines;

import com.kadmuffin.bikesarepain.server.helper.BikeState;
import com.kadmuffin.bikesarepain.server.interfaces.IForceComponent;

import java.util.ArrayList;
import java.util.List;

public class PhysicsPipeline {
    private final List<IForceComponent> forceComponents = new ArrayList<>();

    public void addComponent(IForceComponent component) {
        this.forceComponents.add(component);
    }

    public float calculateNetForce(BikeState currentState) {
        float netForce = 0.0f;
        for (IForceComponent component : this.forceComponents) {
            netForce += component.calculateForce(currentState);
        }
        return netForce;
    }
}
