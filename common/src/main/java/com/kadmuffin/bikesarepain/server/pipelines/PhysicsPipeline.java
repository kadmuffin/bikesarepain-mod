package com.kadmuffin.bikesarepain.server.pipelines;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.records.physics.ScaledInput;
import com.kadmuffin.bikesarepain.records.physics.SurfaceData;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import com.kadmuffin.bikesarepain.server.interfaces.ForceSource;
import com.kadmuffin.bikesarepain.server.interfaces.GenericForce;
import com.kadmuffin.bikesarepain.server.interfaces.SlidingFriction;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventHandler;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PhysicsPipeline {
    public static final float ticksPerSecond = 20f;
    public static final float deltaSeconds = 1 / ticksPerSecond;
    public static final float TRANSITION_TO_STATIC_MPS = 0.1f;

    private final List<GenericForce> genericForces;
    private final List<SlidingFriction> frictionForces;

    private final CenterMass mass;

    public PhysicsPipeline(List<ForceSource> forces, CenterMass mass) {
        this.genericForces = new ArrayList<>();
        this.frictionForces = new ArrayList<>();
        this.mass = mass;

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
            netActiveForces += force.calculateForce(currentState, bike, event);
        }

        SurfaceData data = SurfaceData.buildFrom(currentState, bike);

        if (currentState.currentSpeedMps() < 1e-5f) {
            float maxStaticFriction = 0f;
            for (SlidingFriction friction : this.frictionForces) {
                maxStaticFriction += friction.calculateMaxStaticForce(currentState, bike, data, event);
            }

            if (Math.abs(netActiveForces) <= maxStaticFriction) {
                return new PhysicsResult(0.0f, true);
            }
        }

        float netKineticFriction = 0f;
        for (SlidingFriction friction : this.frictionForces) {
            netKineticFriction += friction.calculateKineticForce(currentState, bike, data, event);
        }

        return new PhysicsResult(netActiveForces + netKineticFriction, false);
    }

    public static float speedToMps(float blocksPerTick) {
        return blocksPerTick * PhysicsPipeline.ticksPerSecond;
    }

    public static float speedToBpt(float metersPerSec) {
        return metersPerSec / PhysicsPipeline.ticksPerSecond;
    }

    public CenterMass getCenterOfMass() {
        return this.mass;
    }

}
