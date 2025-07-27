package com.kadmuffin.bikesarepain.server.pipelines.particles;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.pipelines.event.EventListener;
import com.kadmuffin.bikesarepain.server.pipelines.events.physics.BrakeAppliedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BrakeParticleListener extends EventListener<BrakeAppliedEvent> {
    public BrakeParticleListener() {
        super(BrakeAppliedEvent.class);
    }

    @Override
    public void handleSpecificEvent(BrakeAppliedEvent event, BikeState state, AbstractBike bike) {
        BlockPos floorPos = bike.blockPosition().below();
        BlockState floorState = bike.level().getBlockState(floorPos);

        // Scale the amount based on the speed
        int amount = (int) Math.ceil(bike.getSpeed() * 10);
        amount = Math.min(amount, 100);

        Vec3 frontWheelPos = bike.getFrontWheelPos();
        Vec3 backWheelPos = bike.getBackWheelPos();

        double yRot = Math.toRadians(bike.getYRot());

        double cosYRot = Math.cos(yRot);
        double sinYRot = Math.sin(yRot);

        // Calculate the particle positions from the local wheel (taking into account the yaw)
        Vec3 frontWheelParticlePos = new Vec3(
                frontWheelPos.x * cosYRot - frontWheelPos.z * sinYRot,
                frontWheelPos.y,
                frontWheelPos.x * sinYRot + frontWheelPos.z * cosYRot
        );
        Vec3 backWheelParticlePos = new Vec3(
                backWheelPos.x * cosYRot - backWheelPos.z * sinYRot,
                backWheelPos.y,
                backWheelPos.x * sinYRot + backWheelPos.z * cosYRot
        );

        ParticleOptions particle = new BlockParticleOption(ParticleTypes.BLOCK, floorState);

        // Add particles to the front and back wheel
        for (int i = 0; i < amount * 2; i++) {
            bike.level().addParticle(particle, bike.getX() + frontWheelParticlePos.x, bike.getY() + frontWheelParticlePos.y, bike.getZ() + frontWheelParticlePos.z, 0, 0, 0);
            bike.level().addParticle(particle, bike.getX() + backWheelParticlePos.x, bike.getY() + backWheelParticlePos.y, bike.getZ() + backWheelParticlePos.z, 0, 0, 0);
        }
    }
}
