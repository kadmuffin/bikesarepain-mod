package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.client.helper.DodecagonDisplayManager;
import com.kadmuffin.bikesarepain.common.SoundManager;
import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import com.kadmuffin.bikesarepain.server.item.ItemManager;
import net.minecraft.client.player.AbstractClientPlayer;
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
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Bicycle extends AbstractBike implements GeoEntity {
    public boolean showGears = false;
    private boolean ringAlreadyPressed = false;
    private int ticksSinceLastRing = 0;
    private int ticksSinceLastClick = 0;
    private int ticksSinceLastBrake = 0;
    private SoundType soundType = SoundType.WOOD;
    private final DodecagonDisplayManager displayManager = new DodecagonDisplayManager();

    private static final EntityDataAccessor<Boolean> HAS_BALLOON = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TICKS_OUT_OF_WATER = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BALLOON_INFLATED = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DISPLAYSTAT = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DIGITCOUNT = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CACHED_TARGET = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISPLAY_1 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISPLAY_2 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISPLAY_3 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISPLAY_4 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISPLAY_5 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISPLAY_6 = SynchedEntityData.defineId(Bicycle.class, EntityDataSerializers.FLOAT);


    protected static final RawAnimation DIE_ANIM = RawAnimation.begin().thenPlayAndHold("bike.die");
    protected static final RawAnimation RING_BELL_ANIM = RawAnimation.begin().thenPlay("bike.bell");
    protected static final RawAnimation BALLOON_INFLATE_ANIM = RawAnimation.begin().thenPlay("bike.balloon.inflate");
    protected static final RawAnimation BALLOON_DEFLATE_ANIM = RawAnimation.begin().thenPlay("bike.balloon.deflate");

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
        builder.define(DISPLAY_1, -1F);
        builder.define(DISPLAY_2, -1F);
        builder.define(DISPLAY_3, -1F);
        builder.define(DISPLAY_4, -1F);
        builder.define(DISPLAY_5, -1F);
        builder.define(DISPLAY_6, -1F);
        builder.define(CACHED_TARGET, -1F);
        builder.define(DIGITCOUNT, 0);
        builder.define(DISPLAYSTAT, 0);
        builder.define(BALLOON_INFLATED, false);
        builder.define(HAS_BALLOON, false);
        builder.define(TICKS_OUT_OF_WATER, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("ShowGears", this.showGears);
        compound.putBoolean("HasBalloon", this.hasBalloon());

    }

    // Reads the nbt data
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.showGears = compound.getBoolean("ShowGears");
        this.setHasBalloon(compound.getBoolean("HasBalloon"));
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

                if (this.getSpeed() > 0.1F) {
                    float minPitch = 0.8F;
                    float maxPitch = 1.3F;

                    // Make the range a bit higher depending on our health, if it is 100%, then we have the default range
                    if (this.getHealth() < this.getMaxHealth()) {
                        float healthPercentage = this.getHealth() / this.getMaxHealth();
                        minPitch = 0.8F + healthPercentage * 0.5F;
                        maxPitch = 1.3F - healthPercentage * 0.3F;
                    }

                    this.playSound(SoundManager.BICYCLE_MOVEMENT.get(), this.getSpeed() * this.soundType.getVolume(), Mth.nextFloat(this.random, minPitch, maxPitch) * this.soundType.getPitch());
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
            if (player.getItemInHand(hand).getItem() == Items.IRON_NUGGET) {
                if (this.showGears
                        && this.getHealth() < this.getMaxHealth()) {
                    if (!player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }

                    this.actuallyHeal(1.0F);
                    float healthPercentage = (this.getHealth() / this.getMaxHealth());

                    if (this.getHealth() == this.getMaxHealth()) {
                        this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 1.0F);
                    } else {
                        // Play the repair sound (the lower the health, the lower the pitch)
                        this.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.8F * healthPercentage);
                    }

                    // Do some particles
                    this.level().broadcastEntityEvent(this, (byte) 7);

                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                return InteractionResult.PASS;
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

            if (player.getItemInHand(hand).getItem() == Items.SADDLE) {
                this.openCustomInventoryScreen(player);
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

        controllers.add(new AnimationController<>(this, "inflate", 5, this::inflateAnimation)
                .setSoundKeyframeHandler(event -> this.setBalloonInflated(true))
        );
    }

    // Animation Controller that plays and holds the inflate animation (if the balloon is inflated)
    protected <E extends Bicycle> PlayState inflateAnimation(final AnimationState<E> event) {
        if (this.hasBalloon()) {
            if (this.isInWater()) {
                event.setAndContinue(BALLOON_INFLATE_ANIM);
                this.setTicksOutOfWater(0);
            } else if (this.isBalloonInflated()) {
                if (this.getTicksOutOfWater() > 20) {
                    event.setAndContinue(BALLOON_DEFLATE_ANIM);
                    this.setBalloonInflated(false);
                }
                this.setTicksOutOfWater(this.getTicksOutOfWater() + 1);
            }

        }

        return PlayState.CONTINUE;
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

        if (this.isSavingTime()) {
            itemStack.set(ItemManager.SAVE_TIME.get(), true);
            itemStack.set(ItemManager.TICKS_MOVED.get(), this.getTicksTravelled());
        }

        if (this.isSavingDistance()) {
            itemStack.set(ItemManager.SAVE_DISTANCE.get(), true);
            // Save up to two decimal places
            itemStack.set(ItemManager.DISTANCE_MOVED.get(), (float) Math.round(this.getBlocksTravelled() * 100) / 100);
        }

        if (this.hasBalloon()) {
            itemStack.set(ItemManager.HAS_BALLOON.get(), true);
        }

        itemStack.set(ItemManager.HEALTH_AFFECTS_SPEED.get(), this.isHealthAffectingSpeed());

        return itemStack;
    }

    @Override
    protected float getWaterSlowDown() {
        if (this.isBalloonInflated()) {
            return 0.9F;
        }

        return super.getWaterSlowDown();
    }

    @Override
    public @NotNull Vec3 getFluidFallingAdjustedMovement(double gravity, boolean isFalling, Vec3 deltaMovement) {
        if (this.isBalloonInflated()) {
            // Float effect when the balloon is inflated
            float randomness = this.random.nextFloat() * 0.078F;

            // Adapted version of the original code
            // so that we don't sink and allow modifications of gravity
            if (gravity != 0.0 && !this.isSprinting()) {
                double d;
                if (isFalling && Math.abs(deltaMovement.y - 0.005) >= 0.003 && Math.abs(deltaMovement.y - gravity / 16.0) < 0.003) {
                    d = -0.003;
                } else {
                    d = deltaMovement.y - gravity / 16.0;
                }

                return new Vec3(deltaMovement.x, d + randomness, deltaMovement.z);
            } else {
                return deltaMovement;
            }
        }

        return super.getFluidFallingAdjustedMovement(gravity, isFalling, deltaMovement);
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

        // Play the sound
        this.playSound(SoundEvents.BOOK_PAGE_TURN, 0.3F, Math.max(1.65F, (float) Math.random() * 2F));

        DodecagonDisplayManager.DisplayType type = DodecagonDisplayManager.DisplayType.fromSubType(
                DodecagonDisplayManager.DisplaySubType.fromType(stat % 4)
        );

        this.setCurrentDisplayStat(type.getType());
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
        float time = (float) Math.floor(timeInTicks / 20F);
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

        return new Pair<>(displayType, time);
    }

    public Pair<DodecagonDisplayManager.DisplayType, Float> getTargetDisplayScore() {
        DodecagonDisplayManager.DisplaySubType subType = DodecagonDisplayManager.DisplayType.fromType(this.getCurrentDisplayStat()).getSubType();
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                return switch (subType) {
                    case DISTANCE -> this.autoCastUnitDistance(mixPlayer.bikesarepain$getJSCDistance());
                    case TIME -> this.autoCastUnitTime(this.getTicksTravelled());
                    case SPEED -> this.autoCastUnitSpeed(mixPlayer.bikesarepain$getJSCSpeed(), true);
                    case CALORIES ->
                            new Pair<>(DodecagonDisplayManager.DisplayType.CALORIES_KCAL, mixPlayer.bikesarepain$getJSCCalories());
                };
            }
        }
        return switch (subType) {
            case DISTANCE -> this.autoCastUnitDistance(this.getBlocksTravelled());
            case TIME -> this.autoCastUnitTime(this.getTicksTravelled());
            default -> this.autoCastUnitSpeed(this.getSpeedInMetersPerSecond(), false);
        };
    }

    public void updateDisplayTarget() {
        Pair<DodecagonDisplayManager.DisplayType, Float> result = this.getTargetDisplayScore();

        this.setCurrentDisplayStat(result.getA().getType());
        this.displayManager.preprocessTarget(result.getB(), this);
    }

    public float getCachedFloatDisplay(int displayIndex) {
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

    public void setCachedFloatDisplay(int displayIndex, float value) {
        switch (displayIndex) {
            case 0 -> this.entityData.set(DISPLAY_1, value);
            case 1 -> this.entityData.set(DISPLAY_2, value);
            case 2 -> this.entityData.set(DISPLAY_3, value);
            case 3 -> this.entityData.set(DISPLAY_4, value);
            case 4 -> this.entityData.set(DISPLAY_5, value);
            case 5 -> this.entityData.set(DISPLAY_6, value);
        }
    }

    public float getCachedTarget() {
        return this.entityData.get(CACHED_TARGET);
    }

    public void setCachedTarget(float target) {
        this.entityData.set(CACHED_TARGET, target);
    }

    public int getDigitCount() {
        return this.entityData.get(DIGITCOUNT);
    }

    public void setDigitCount(int count) {
        this.entityData.set(DIGITCOUNT, count);
    }

    public boolean hasBalloon() {
        return this.entityData.get(HAS_BALLOON);
    }

    public void setHasBalloon(boolean hasBalloon) {
        this.entityData.set(HAS_BALLOON, hasBalloon);
    }

    public boolean isBalloonInflated() {
        return this.entityData.get(BALLOON_INFLATED);
    }

    public void setBalloonInflated(boolean inflated) {
        this.entityData.set(BALLOON_INFLATED, inflated);
    }

    public void setTicksOutOfWater(int ticks) {
        this.entityData.set(TICKS_OUT_OF_WATER, ticks);
    }

    public int getTicksOutOfWater() {
        return this.entityData.get(TICKS_OUT_OF_WATER);
    }

    public Vec3 getDisplayPos() {
        return new Vec3(0,
                1.07 * this.getModelScalingFactor(),
                0.45F * this.getModelScalingFactor());
    }

    public Vec3 getSeatPos() {
        return new Vec3(0, 0.9F * this.getModelScalingFactor(), -0.4F * this.getModelScalingFactor());
    }

    // Used for zooming into the display
    // When the player is looking at the display, we want to zoom in
    public float modifyFOV(AbstractClientPlayer player, float fov) {
        Vec3 seat = this.modeltoWorldPos(this.getSeatPos());
        Vec3 camera = new Vec3(seat.x, seat.y + player.getEyeHeight()*0.96F, seat.z);
        Vec3 displayPos = this.modeltoWorldPos(this.getDisplayPos());

        float pitch = (float) Math.toRadians(player.getXRot());
        float yaw = (float) Math.toRadians(player.getYRot());

        Vec3 viewDir = new Vec3(
                -Math.sin(yaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch)
        ).normalize();

        Vec3 targetDir = displayPos.subtract(camera).normalize();

        // arcos dot product of viewDir and targetDir
        float angle = (float) Math.acos(viewDir.dot(targetDir));

        // 7.5 degrees -> 0.1309 radians
        if (angle < 0.1309) {
            return fov * 0.6F;
        }

        return fov;
    }
}
