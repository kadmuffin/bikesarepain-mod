package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Bicycle extends AbstractBike implements GeoEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int timeSinceLastRing = 0;
    public boolean showGears = false;

    protected static final RawAnimation DIE_ANIM = RawAnimation.begin().thenPlayAndHold("bike.die");
    protected static final RawAnimation RING_BELL_ANIM = RawAnimation.begin().thenPlay("bike.bell");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final CenterMass centerMass = new CenterMass(
            new Vector3d(0.0F, 1.35F, 0.0F),
            new Vector3d(0.0F, 1.85F, -0.66F),
            7,
            60
    );

    @Override
    public void die(DamageSource damageSource) {
        if (!this.isRemoved() && !this.dead) {
            Entity entity = damageSource.getEntity();

            this.dead = true;
            this.getCombatTracker().recheckStatus();
            if (this.level() instanceof ServerLevel serverLevel) {
                if (entity == null || entity.killedEntity(serverLevel, this)) {
                    this.gameEvent(GameEvent.ENTITY_DIE);
                    this.dropAllDeathLoot(serverLevel, damageSource);
                }

                this.level().broadcastEntityEvent(this, (byte)3);
            }

            this.triggerAnim("finalAnim", "die");
        }
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource damageSource) {
        this.dropEquipment();

        // Summon a bicycle item
        ItemStack itemStack = new ItemStack(ItemManager.BICYCLE_ITEM.get());
        itemStack.setCount(1);

        this.spawnAtLocation(itemStack);
    }

    protected Bicycle(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.getPassengers().isEmpty()) {
            return super.mobInteract(player, hand);

            // Check if the player is doing a right click
        } else if (player.isShiftKeyDown()) {
            // Each nugget repairs 1 health
            if (player.getItemInHand(hand).getItem() == Items.IRON_NUGGET && this.showGears
                && this.getHealth() < this.getMaxHealth()
            ) {
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }

                this.heal(1.0F);

                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (player.getItemInHand(hand).getItem() == Items.STICK && this.showGears) {
                this.showGears = false;
                this.playSound(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, 1.0F, Mth.nextFloat(this.random, 1F, 1.5F));
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (!this.showGears){
                this.showGears = true;
                this.playSound(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, 1.0F, Mth.nextFloat(this.random, 1F, 1.5F));
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
            this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        this.doPlayerRide(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public void tick() {
        super.tick();
        this.timeSinceLastRing++;
    }

    // Play bell sound
    public void ringBell() {
        // Check if the bell has been rung in the last 20 ticks
        if (this.timeSinceLastRing < 4) {
            return;
        }
        this.triggerAnim("finalAnim", "bell");

        // Scare nearby entities
        AABB aABB = this.getBoundingBox().inflate(3.6D);
        // Check for PathAwareEntities
        this.level().getEntities(this, aABB).stream().filter((entity) -> entity instanceof PathfinderMob).forEach((entity) -> {
            // Give velocity in opposite direction with slight 0.1F upwards
            Vec3 vec3 = entity.position().subtract(this.position()).normalize();
            entity.addDeltaMovement(new Vec3(vec3.x * 0.5F, 0.4F, vec3.z * 0.5F));
        });

        this.timeSinceLastRing = 0;
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor)  {
        int i = Math.max(this.getPassengers().indexOf(passenger), 0);
        boolean primaryPassenger = i == 0;
        float horizontalOffset = -0.66F;
        float verticalOffset = 1.85F;
        if (this.getPassengers().size() > 1) {
            if (!primaryPassenger) {
                horizontalOffset -= 0.5F;
                verticalOffset -= 0.4F;
            }
        }

        return (new Vec3(0.0F, verticalOffset, horizontalOffset * scaleFactor)).yRot(-this.getYRot() * 0.017453292F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "finalAnim", event -> PlayState.CONTINUE)
                .triggerableAnim("die", DIE_ANIM)
                .triggerableAnim("bell", RING_BELL_ANIM).setSoundKeyframeHandler(
                        state -> {
                            Player player = ClientUtil.getClientPlayer();

                            if (player != null) {
                                player.playSound(SoundEvents.BELL_BLOCK, 1.0F, (float) Math.random() * 0.1F + 1.9F);
                            }
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public Vec3 getModelSize() {
        return new Vec3(3.78 / 16, 0.5, 30.0 / 16);
    }

    @Override
    public CenterMass getCenterMass() {
        return this.centerMass;
    }

    @Override
    public float getBackWheelRadius() {
        return 2F;
    }

    @Override
    public float getMaxTiltAngle() {
        return (float) Math.toRadians(13F);
    }

    @Override
    public float getMaxSteeringAngle() {
        return (float) Math.toRadians(45F);
    }

    @Override
    public float getMaxPedalAnglePerSecond() {
        return (float) Math.PI/1.32F;
    }

    @Override
    public float getMaxTurnRate() {
        return (float) (2*Math.PI/3);
    }

    public float getTurnScalingFactor() {
        return 15.0F;
    }
}
