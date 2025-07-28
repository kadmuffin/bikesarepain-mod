package com.kadmuffin.bikesarepain.server.pipelines;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.records.physics.SurfaceData;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.ForceSource;
import com.kadmuffin.bikesarepain.server.interfaces.GenericForce;
import com.kadmuffin.bikesarepain.server.interfaces.SlidingFriction;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class PhysicsPipeline {
    public static final float ticksPerSecond = 20f;
    public static final float deltaSeconds = 1 / ticksPerSecond;
    public static final float TRANSITION_TO_STATIC_MPS = 0.1f;

    private final List<GenericForce> genericForces;
    private final List<SlidingFriction> frictionForces;

    public PhysicsPipeline(List<ForceSource> forces) {
        this.genericForces = new ArrayList<>();
        this.frictionForces = new ArrayList<>();

        for (ForceSource force : forces) {
            this.addForce(force);
        }
    }

    public void addForce(ForceSource component) {
        if (component instanceof GenericForce c) {
            this.genericForces.add(c);
        } else if (component instanceof SlidingFriction c) {
            this.frictionForces.add(c);
        }
    }

    public void removeForce(Class<? extends ForceSource> componentType) {
        if (componentType == GenericForce.class) {
            this.genericForces.removeIf(componentType::isInstance);
        } else if (componentType == SlidingFriction.class) {
            this.frictionForces.removeIf(componentType::isInstance);
        }
    }

    public record PhysicsResult(float netForce, boolean zeroVelocity) {}

    public PhysicsResult calculateNetForceVel(BikeState currentState, AbstractBike bike, EventHandler event) {
        float netActiveForces = 0f;
        for (GenericForce force : this.genericForces) {
            float result = force.calculateForce(currentState, bike, event);
            System.out.println(force.getID()+": "+result+" Newtons");
            netActiveForces += result;
        }

        SurfaceData data = SurfaceData.buildFrom(currentState, bike);
        boolean zeroVelocity = false;
        float netFrictionForces = 0f;
        if (currentState.currentSpeedMps() > TRANSITION_TO_STATIC_MPS) {
            for (SlidingFriction friction : this.frictionForces) {
                float result = friction.calculateKineticForce(currentState, bike, data, event);
                System.out.println(friction.getID()+": "+result+" Newtons");
                netFrictionForces += result;
            }
        } else {
            float maxStaticFriction = 0f;
            for (SlidingFriction friction : this.frictionForces) {
                float result = friction.calculateMaxStaticForce(currentState, bike, data, event);
                System.out.println(friction.getID()+": "+result+" Newtons");
                maxStaticFriction += result;
            }

            if (Math.abs(netActiveForces) <= maxStaticFriction) {
                return new PhysicsResult(0.0f, true);
            } else {
                float kineticFriction = 0f;
                for (SlidingFriction friction : this.frictionForces) {
                    float result = friction.calculateKineticForce(currentState, bike, data, event);
                    System.out.println(friction.getID()+": "+result+" Newtons");
                    kineticFriction += result;
                }
                return new PhysicsResult(netActiveForces + kineticFriction, false);
            }
        }


        return new PhysicsResult(netActiveForces + netFrictionForces, zeroVelocity);
    }
}
