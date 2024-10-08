package com.kadmuffin.bikesarepain.server.entity.ai;


import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.player.Player;

public class BikeBondWithPlayerGoal extends RunAroundLikeCrazyGoal {
    public final AbstractBike bike;

    public BikeBondWithPlayerGoal(AbstractBike bikeEntity, double speed) {
        super(bikeEntity, speed);
        this.bike = bikeEntity;
    }

    @Override
    public boolean canUse() {
        if (!this.bike.getPassengers().isEmpty()) {
            return super.canUse();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.bike.isSaddled() && this.bike.getFirstPassenger() instanceof Player playerEntity) {
            playerEntity.hurt(new DamageSources(this.bike.registryAccess()).sting(this.bike), 1F);
        }
        return super.canContinueToUse();
    }
}
