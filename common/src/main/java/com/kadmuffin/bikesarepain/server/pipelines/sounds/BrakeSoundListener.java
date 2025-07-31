package com.kadmuffin.bikesarepain.server.pipelines.sounds;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.interfaces.Bike;
import com.kadmuffin.bikesarepain.server.pipelines.events.physics.BrakeAppliedEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;

public class BrakeSoundListener extends SoundEventListener<BrakeAppliedEvent> {
    private int ticksSinceLastBrake = 0;

    public BrakeSoundListener(SoundEvent sound, SoundType type) {
        super(BrakeAppliedEvent.class, sound, type);
    }

    @Override
    public void handleSpecificEvent(BrakeAppliedEvent event, BikeState state, Bike bike) {
        float speed = Math.abs(bike.getSpeed());
        int interval = Math.max(1, (int) (5 - Math.min(state.currentSpeedMps(), 4)));

        this.ticksSinceLastBrake++;
        if (this.ticksSinceLastBrake < interval) {
            return;
        }
        float volume = Math.clamp(0.8F * this.soundType.getVolume() * speed, 0, 2);
        float pitch = 0.85F + Math.min(speed, 2.0F) + (float) Math.random() * 0.1F * this.soundType.getPitch();

        if (bike.getSpeed() > 0f) {
            bike.level().playLocalSound((Entity) bike, this.soundToPlay, SoundSource.AMBIENT, volume, pitch);
        }
        ticksSinceLastBrake = 0;
    }
}
