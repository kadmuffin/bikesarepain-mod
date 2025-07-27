package com.kadmuffin.bikesarepain.server.pipelines.sounds;

import com.kadmuffin.bikesarepain.server.interfaces.PipelineEvent;
import com.kadmuffin.bikesarepain.server.pipelines.event.TypedEventListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public abstract class SoundEventListener<T extends PipelineEvent> extends TypedEventListener<T> {
    protected SoundEvent soundToPlay;
    protected SoundType soundType;

    protected SoundEventListener(Class<T> eventType, SoundEvent sound, SoundType type) {
        super(eventType);
        this.soundToPlay = sound;
        this.soundType = type;
    }
}
