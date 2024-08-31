package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.client.helper.DodecagonDisplayManager;
import com.kadmuffin.bikesarepain.common.SoundManager;
import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import oshi.util.tuples.Pair;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;

public class Bicycle extends AbstractBike implements GeoEntity {
    public boolean showGears = false;
    private boolean ringAlreadyPressed = false;
    private int ticksSinceLastRing = 0;
    private int ticksSinceLastClick = 0;
    private int ticksSinceLastBrake = 0;
    private SoundType soundType = SoundType.WOOD;
    private final DodecagonDisplayManager displayManager = new DodecagonDisplayManager();
    private int ticksPedalling = 0;
    private float distanceMoved = 0;


    private static final EntityDataAccessor<Integer> DISPLAYSTAT = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DIGITCOUNT = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CACHED_TARGET = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISPLAY_1 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISPLAY_2 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISPLAY_3 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISPLAY_4 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISPLAY_5 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISPLAY_6 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);


    protected static final RawAnimation DIE_ANIM = RawAnimation.begin().thenPlayAndHold("bike.die");
    protected static final RawAnimation RING_BELL_ANIM = RawAnimation.begin().thenPlay("bike.bell");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final CenterMass centerMass = new CenterMass(
            new Vector3d(0.0F, 1.35F, 0.0F),
            new Vector3d(0.0F, 1.85F, -0.66F),
            7,
            60
    );

    protected Bicycle(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DISPLAY_1, -1);
        builder.define(DISPLAY_2, -1);
        builder.define(DISPLAY_3, -1);
        builder.define(DISPLAY_4, -1);
        builder.define(DISPLAY_5, -1);
        builder.define(DISPLAY_6, -1);
        builder.define(CACHED_TARGET, -1);
        builder.define(DIGITCOUNT, 0);
        builder.define(DISPLAYSTAT, 0);
    }

    // Reads the nbt data
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.showGears = compound.getBoolean("ShowGears");
        // Read health percentage
        float health = compound.getFloat("Health");
        health = Mth.clamp(health, 0.0F, 1.0F);

        // Set the health
        this.setHealth(health * this.getMaxHealth());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.ticksSinceLastRing <= 6) {
                this.ticksSinceLastRing++;
            }
            this.updateDisplayTarget();

        }
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        this.ticksPedalling = 0;
        return super.getDismountLocationForPassenger(passenger);
    }

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
        ItemStack itemStack = this.getBicycleItem(false);

        this.spawnAtLocation(itemStack);
    }


    @Override
    protected @NotNull Vec3 getRiddenInput(Player controllingPlayer, Vec3 movementInput) {
        Vec3 vec3d = super.getRiddenInput(controllingPlayer, movementInput);

        if (!this.level().isClientSide()) {
            float g = controllingPlayer.zza;
            if (g <= 0.0F) {
                g *= 0.25F;
            }
            float speed = Math.abs(this.getSpeed());
            boolean isReverse = g < 0.0F;

            if (speed > 0.05) {
                ticksPedalling++;

                // Calculate the distance moved in meters
                // 1 block = 1 meter
                this.distanceMoved += (float) (Math.abs(this.getSpeed()) * 20 / 3.6);

                // Depending on the speed, we'll scale the volume and pitch
                // with a sprinkle of randomness
                final float pitch = 0.85F + Math.min(speed, 2.0F) + (float) Math.random() * 0.1F * this.soundType.getPitch();
                float volume = this.soundType.getVolume() * 0.07F * (0.7F-speed);
                float wheelRotationSpeed = speed;
                if (speed < 0.08F && g == 0 || isReverse) {
                    wheelRotationSpeed *= 10;
                }
                float ticksPerClick = 1/ wheelRotationSpeed * 3F;
                if ((speed > 0.25F && g == 0) || isReverse) {
                    volume *= 1.5F;
                    ticksPerClick /= 2F;
                }

                // ticksPerClick = Math.max(1, ticksPerClick);
                // System.out.printf("Speed: %f, Volume: %f, Pitch: %f, TicksPerClick: %f\n", speed, volume, pitch, ticksPerClick);

                if (this.getSpeed() > 0.1F) {
                    this.playSound(SoundManager.BICYCLE_MOVEMENT.get(), this.getSpeed() * this.soundType.getVolume(), Mth.nextFloat(this.random, 0.8F, 1.3F) * this.soundType.getPitch());
                }

                if (this.ticksSinceLastClick > ticksPerClick && speed > 0.05F) {
                    this.playSound(SoundManager.BICYCLE_SPOKES.get(), volume, pitch);
                    this.ticksSinceLastClick = 0;
                } else {
                    this.ticksSinceLastClick++;
                }
            }
        }

        return vec3d;
    }

    @Override
    protected void playJumpSound() {
        this.playSound(SoundManager.BICYCLE_LAND.get(), 0.8F, 1.0F);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (fallDistance > 1.0F) {
            this.playSound(SoundManager.BICYCLE_LAND.get(), 0.8F, 1.0F);
        }

        int i = this.calculateFallDamage(fallDistance, multiplier);
        if (i <= 0) {
            return false;
        } else {
            this.hurt(source, (float)i);
            if (this.isVehicle()) {
                for (Entity entity : this.getIndirectPassengers()) {
                    entity.hurt(source, (float)i);
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!state.liquid()) {
            BlockState blockState = this.level().getBlockState(pos.above());
            this.soundType = state.getSoundType();
            if (blockState.is(Blocks.SNOW)) {
                this.soundType = blockState.getSoundType();
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.getPassengers().isEmpty() && !this.canAddPassenger(player)){
            return super.mobInteract(player, hand);
        } else if (player.isShiftKeyDown()) {
            // Each nugget repairs 1 health
            if (player.getItemInHand(hand).getItem() == Items.IRON_NUGGET && this.showGears
                && this.getHealth() < this.getMaxHealth()
            ) {
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }

                this.actuallyHeal(1.0F);

                // Play the repair sound
                this.playSound(SoundEvents.ANVIL_USE, 1.0F, Mth.nextFloat(this.random, 1F, 1.5F));

                // Do some particles
                this.level().broadcastEntityEvent(this, (byte) 7);

                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (player.getItemInHand(hand).getItem() == Items.STICK) {
                // Toggle the showGears state
                this.showGears = !this.showGears;

                // Determine the sound to play based on the new state
                SoundEvent soundEvent = this.showGears ? SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF : SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON;

                // Play the corresponding sound
                this.playSound(soundEvent, 1.0F, Mth.nextFloat(this.random, 1F, 1.5F));

                // Return the interaction result
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
            // Summon an item
            this.spawnAtLocation(this.getBicycleItem(true));
            this.remove(RemovalReason.DISCARDED);

            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        this.doPlayerRide(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    // Play bell sound
    public void ringBell() {
        if (this.ticksSinceLastRing < 6) {
            return;
        }
        this.ticksSinceLastRing = 0;

        this.triggerAnim("finalAnim", "bell");

        // Scare nearby entities
        AABB aABB = this.getBoundingBox().inflate(2.1D);

        // Push it box position forward a bit
        aABB = aABB.move(this.getLookAngle().scale(0.67F));

        // Check for PathAwareEntities
        this.level().getEntities(this, aABB).stream().filter((entity) -> entity instanceof PathfinderMob).forEach((entity) -> {
            if (entity.onGround()) {
                // Give velocity in opposite direction with slight 0.1F upwards
                Vec3 vec3 = entity.position().subtract(this.position()).normalize();

                // Add velocity either to the right or left randomly
                // the velocity is always 0.8, only the sign changes
                float direction = (float) (this.random.nextBoolean() ? 1.2 : -1.2);
                vec3.add(new Vec3(
                        vec3.z * direction,
                        0.2F,
                        vec3.x * direction
                ));


                entity.addDeltaMovement(new Vec3(vec3.x * 0.8F, 0.2F, vec3.z * 0.8F));
            }
        });
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor)  {
        int i = Math.max(this.getPassengers().indexOf(passenger), 0);
        boolean primaryPassenger = i == 0;
        float horizontalOffset = -0.4F;
        float verticalOffset = 0.9625F;
        if (this.getPassengers().size() > 1) {
            if (!primaryPassenger) {
                horizontalOffset = -0.575F;
                verticalOffset = 0.797F;
            }
        }

        return (new Vec3(0.0F, verticalOffset * this.getModelScalingFactor(), horizontalOffset * this.getModelScalingFactor() * scaleFactor)).yRot(-this.getYRot() * 0.017453292F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "finalAnim", event -> PlayState.CONTINUE)
                .triggerableAnim("die", DIE_ANIM)
                .triggerableAnim("bell", RING_BELL_ANIM).setSoundKeyframeHandler(
                        state -> {
                            Player player = ClientUtil.getClientPlayer();

                            if (player != null) {
                                player.playSound(SoundManager.BICYCLE_BELL.get(), Mth.nextFloat(
                                        player.getRandom(), 0.8F, 1.2F
                                ), Mth.nextFloat(player.getRandom(), 1.01F, 1.04F));
                            }
                        }
                )
        );
    }

    public PlayerAccessor getRiderPlayerAccessor() {
        Player player = this.getRider();
        if (player != null) {
            return (PlayerAccessor) player;
        }

        return null;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public ItemStack getBicycleItem(boolean includeSaddle) {
        ItemStack itemStack = new ItemStack(ItemManager.BICYCLE_ITEM.get());
        itemStack.setDamageValue(itemStack.getMaxDamage() - (int) (this.getHealth() / this.getMaxHealth() * itemStack.getMaxDamage()));

        if (includeSaddle) {
            itemStack.set(ItemManager.SADDLED.get(), this.isSaddled());
        }
        return itemStack;
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
    public float getModelWheelRadius() {
        return 0.3515625F;
    }

    @Override
    public float getWheelRadius() {
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                return mixPlayer.bikesarepain$getJSCWheelRadius();
            }
        }
        // 0.5F means the wheel measures 1 block
        return 0.5F;
    }

    @Override
    public float getPedalMultiplier() {
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                return 1F;
            }
        }

        return 3F;
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
        return (float) (Math.PI);
    }

    @Override
    public float getMaxTurnRate() {
        return (float) (Math.PI);
    }

    public float getTurnScalingFactor() {
        return 40.0F;
    }

    public float inertiaFactor() {
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                return 0.98F;
            }
        }
        return 0.95F;
    }

    @Override
    public float getBrakeMultiplier() {
        return 0.7F;
    }

    @Override
    public void playBrakeSound() {
        this.ticksSinceLastBrake++;
        if (this.ticksSinceLastBrake < 5) {
            return;
        }
        float speed = Math.abs(this.getSpeed());
        float volume = 0.8F * this.soundType.getVolume() * (0.7F-speed);
        float pitch = 0.85F + Math.min(speed, 2.0F) + (float) Math.random() * 0.1F * this.soundType.getPitch();

        this.playSound(SoundManager.BICYCLE_LAND.get(), volume, pitch);
        ticksSinceLastBrake = 0;
    }

    @Override
    public Vec3 getFrontWheelPos() {
        return new Vec3(0, 0, -0.59);
    }

    @Override
    public Vec3 getBackWheelPos() {
        return new Vec3(0, 0, 0.56);
    }

    public int getTicksSinceLastRing() {
        return ticksSinceLastRing;
    }

    public void setTicksSinceLastRing(int ticksSinceLastRing) {
        this.ticksSinceLastRing = ticksSinceLastRing;
    }

    public boolean isRingAlreadyPressed() {
        return ringAlreadyPressed;
    }

    public void setRingAlreadyPressed(boolean ringAlreadyPressed) {
        this.ringAlreadyPressed = ringAlreadyPressed;
    }

    public DodecagonDisplayManager getDisplayManager() {
        return displayManager;
    }

    public int getCurrentDisplayStat() {
        return this.entityData.get(DISPLAYSTAT);
    }

    public void setCurrentDisplayStat(int currentDisplayStat) {
        this.entityData.set(DISPLAYSTAT, currentDisplayStat);
    }

    public void chooseNextDisplayStat() {
        // In range of 0-3
        // 0: Distance
        // 1: Time in minutes
        // 2: Speed
        // 3: Calories
        int stat = DodecagonDisplayManager.DisplayType.fromType(this.getCurrentDisplayStat()).getSubType().getType() + 1;

        // Check if JSC is active
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (!mixPlayer.bikesarepain$isJSCActive()) {
                // If it is not active, skip the calories (id 3)
                if (stat == 3) {
                    stat = 0;
                }
            }
        }

        DodecagonDisplayManager.DisplayType type = DodecagonDisplayManager.DisplayType.fromSubType(
                DodecagonDisplayManager.DisplaySubType.fromType(stat % 4)
        );

        this.setCurrentDisplayStat(type.getType());
    }

    public float getTicksPedalled() {
        return this.ticksPedalling;
    }

    public float getDistanceMoved() {
        return this.distanceMoved;
    }

    public Pair<DodecagonDisplayManager.DisplayType, Float> autoCastUnitDistance(float distance) {
        DodecagonDisplayManager.DisplayType displayType = DodecagonDisplayManager.DisplayType.DISTANCE_METERS;
        if (distance > 1000) {
            displayType = DodecagonDisplayManager.DisplayType.DISTANCE_KM;
            distance /= 1000;
        }

        if (this.getRiderPlayerAccessor() instanceof PlayerAccessor mixPlayer && mixPlayer.bikesarepain$wantsAmericaUnits()) {
            if (displayType == DodecagonDisplayManager.DisplayType.DISTANCE_KM) {
                displayType = DodecagonDisplayManager.DisplayType.DISTANCE_MI;
                distance *= 0.621371F;
            } else {
                displayType = DodecagonDisplayManager.DisplayType.DISTANCE_FT;
                distance *= 3.28084F;
            }
        }

        return new Pair<>(displayType, distance);
    }

    public Pair<DodecagonDisplayManager.DisplayType, Float> autoCastUnitSpeed(float speed, boolean forceInitialKMH) {
        DodecagonDisplayManager.DisplayType displayType = DodecagonDisplayManager.DisplayType.SPEED_MS;

        if (forceInitialKMH) {
            displayType = DodecagonDisplayManager.DisplayType.SPEED_KMH;
        } else if (speed > 3.6F) {
            displayType = DodecagonDisplayManager.DisplayType.SPEED_KMH;
            speed *= 3.6F;
        }

        if (this.getRiderPlayerAccessor() instanceof PlayerAccessor mixPlayer && mixPlayer.bikesarepain$wantsAmericaUnits()) {
            displayType = DodecagonDisplayManager.DisplayType.SPEED_MPH;
            speed *= 0.621371F;
        }

        return new Pair<>(displayType, speed);
    }

    public Pair<DodecagonDisplayManager.DisplayType, Float> autoCastUnitTime(float timeInTicks) {
        DodecagonDisplayManager.DisplayType displayType = DodecagonDisplayManager.DisplayType.TIME_SEC;
        float time = timeInTicks / 20F;
        if (time > 60) {
            displayType = DodecagonDisplayManager.DisplayType.TIME_MIN;
            time /= 60;
            if (time > 60) {
                displayType = DodecagonDisplayManager.DisplayType.TIME_HR;
                time /= 60;
                if (time > 24) {
                    displayType = DodecagonDisplayManager.DisplayType.TIME_DAY;
                    time /= 24;
                }
            }
        }

        System.out.println("Time: " + time + " " + displayType);

        return new Pair<>(displayType, time);
    }

    public Pair<DodecagonDisplayManager.DisplayType, Float> getTargetDisplayScore() {
        DodecagonDisplayManager.DisplaySubType subType = DodecagonDisplayManager.DisplayType.fromType(this.getCurrentDisplayStat()).getSubType();
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                return switch (subType) {
                    case DISTANCE -> this.autoCastUnitDistance(mixPlayer.bikesarepain$getJSCDistance());
                    case TIME -> this.autoCastUnitTime(this.getTicksPedalled());
                    case SPEED -> this.autoCastUnitSpeed(mixPlayer.bikesarepain$getJSCSpeed(), true);
                    case CALORIES ->
                            new Pair<>(DodecagonDisplayManager.DisplayType.CALORIES_KCAL, mixPlayer.bikesarepain$getJSCCalories());
                };
            }
        }
        return switch (subType) {
            case DISTANCE -> this.autoCastUnitDistance(this.getDistanceMoved());
            case TIME -> this.autoCastUnitTime(this.getTicksPedalled());
            default -> this.autoCastUnitSpeed(this.getSpeedInMetersPerSecond(), false);
        };
    }

    public void updateDisplayTarget() {
        Pair<DodecagonDisplayManager.DisplayType, Float> result = this.getTargetDisplayScore();
        int value = (int) Math.floor(result.getB());

        this.setCurrentDisplayStat(result.getA().getType());
        this.displayManager.preprocessTarget(value, this);
    }

    public int getCachedIntDisplay(int displayIndex) {
        return switch (displayIndex) {
            case 0 -> this.entityData.get(DISPLAY_1);
            case 1 -> this.entityData.get(DISPLAY_2);
            case 2 -> this.entityData.get(DISPLAY_3);
            case 3 -> this.entityData.get(DISPLAY_4);
            case 4 -> this.entityData.get(DISPLAY_5);
            case 5 -> this.entityData.get(DISPLAY_6);
            default -> -1;
        };
    }

    public void setCachedIntDisplay(int displayIndex, int value) {
        switch (displayIndex) {
            case 0 -> this.entityData.set(DISPLAY_1, value);
            case 1 -> this.entityData.set(DISPLAY_2, value);
            case 2 -> this.entityData.set(DISPLAY_3, value);
            case 3 -> this.entityData.set(DISPLAY_4, value);
            case 4 -> this.entityData.set(DISPLAY_5, value);
            case 5 -> this.entityData.set(DISPLAY_6, value);
        }
    }

    public int getCachedTarget() {
        return this.entityData.get(CACHED_TARGET);
    }

    public void setCachedTarget(int target) {
        this.entityData.set(CACHED_TARGET, target);
    }

    public int getDigitCount() {
        return this.entityData.get(DIGITCOUNT);
    }

    public void setDigitCount(int count) {
        this.entityData.set(DIGITCOUNT, count);
    }
}
