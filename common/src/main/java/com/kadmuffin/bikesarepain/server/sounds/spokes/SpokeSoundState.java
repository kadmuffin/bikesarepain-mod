package com.kadmuffin.bikesarepain.server.sounds.spokes;

import com.kadmuffin.bikesarepain.common.SoundManager;
import com.kadmuffin.bikesarepain.server.interfaces.StateComponent;
import net.minecraft.sounds.SoundEvent;

public record SpokeSoundState(SoundEvent audio, float ogAudioClicksInASec, int virtualFreehubTeeth, float minSpeedForSound, float mpsSpeedToFadeSound, float peakLoudnessSpeed, float volumeMultiplier, float parabolicCoefficient) implements StateComponent {
    public static SpokeSoundState defaultState() {
        float minSpeedForSound = 0.1f;
        float peakLoudnessSpeed = 9f;
        float parabolicCoeff = SpokeSoundState.calculateParabolicCoeff(minSpeedForSound, peakLoudnessSpeed);

        return new SpokeSoundState(
                SoundManager.BICYCLE_SPOKES.get(),
                7, 3, minSpeedForSound, 15f, peakLoudnessSpeed, 0.35f,
                parabolicCoeff
                );
    }

    public static SpokeSoundState simple(SoundEvent audio, float ogAudioClicksInASec, int virtualFreehubTeeth, float mpsSpeedToFadeSound, float peakLoudnessSpeed, float volumeMultiplier) {
        float parabolicCoeff = SpokeSoundState.calculateParabolicCoeff(0.1f, peakLoudnessSpeed);

        return new SpokeSoundState(
                audio,
                ogAudioClicksInASec, virtualFreehubTeeth, 0.1f, mpsSpeedToFadeSound, peakLoudnessSpeed, volumeMultiplier,
                parabolicCoeff
        );
    }

    public static float calculateParabolicCoeff(float minSpeedForSound, float peakLoudnessSpeed) {
        float k = 1.0f;
        float a = -k / ((minSpeedForSound - peakLoudnessSpeed) * (minSpeedForSound - peakLoudnessSpeed));

        return (Math.abs(a) < 1e-6) ? 0f : (-k / a);
    }
}
