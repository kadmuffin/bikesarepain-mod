package com.kadmuffin.bikesarepain.server.sounds.spokes;

import com.kadmuffin.bikesarepain.records.physics.BikeState;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.pipelines.PhysicsPipeline;

public class SpokeSoundLogic {
    private float clicksToPlay = 0;

    public void process(SpokeSoundState soundState, BikeState physicsState, AbstractBike bike) {
        float currentSpeed = Math.abs(physicsState.currentSpeedMps());

        if (physicsState.playerInput().forward() > 0f
                || currentSpeed <= soundState.minSpeedForSound()) {
            return;
        }

        float revsPerSec = (float) (Math.abs(physicsState.currentSpeedMps()) / (2 * Math.PI * bike.getWheelRadius()));
        float clicksPerSec  = revsPerSec * soundState.virtualFreehubTeeth();
        float clicksThisTick= clicksPerSec * PhysicsPipeline.deltaSeconds;

        this.clicksToPlay += clicksThisTick;
        int clickCount = (int) Math.floor(this.clicksToPlay);
        this.clicksToPlay -= clickCount;

        float vol = calculateParabolicVolume(currentSpeed, soundState);
        float pitch = 1.0f + clicksPerSec / soundState.ogAudioClicksInASec();

        for (int i = 0; i < clickCount; i++) {
            bike.playSound(soundState.audio(), vol * soundState.volumeMultiplier(), pitch);
        }
    }

    private float calculateParabolicVolume(float currentSpeed, SpokeSoundState soundState) {
        float h = soundState.peakLoudnessSpeed();
        float k = 1.0f;
        float a = soundState.parabolicCoefficient();

        float volume = a * (currentSpeed - h) * (currentSpeed - h) + k;

        return Math.clamp(volume, 0f, 1f) * soundState.volumeMultiplier();
    }

}
