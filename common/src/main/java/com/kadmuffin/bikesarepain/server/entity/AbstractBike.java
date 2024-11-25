package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.server.GameRuleManager;
import com.kadmuffin.bikesarepain.server.entity.ai.BikeBondWithPlayerGoal;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractBike extends AbstractHorse implements PlayerRideableJumping, Saddleable {
    // Let devs add event listener for when the bike is moving
    // This is a list storing those lambdas
    private static final List<TriConsumer<AbstractBike, Float, Boolean>> onMoveListeners = new ArrayList<>();
    // Entity Data
    private static final EntityDataAccessor<Float> SYNCED_BIKE_PITCH = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BLOCKS_TRAVELLED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TICKS_PEDALLED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LAST_ROT_Y = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_CHEST = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> TILT = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEERING_YAW = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BACKWHEEL_ROTATION = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FRONWHEEL_ROTATION = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAST_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> INTERNAL_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BRAKING = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEALTH_AFF_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.BOOLEAN);
    // Store already computed rotations for:
    // backWheelRotation
    // steeringYaw
    // tilt
    // and when the frame it was last updated
    public final Map<String, RotationData> rotations = new HashMap<>();
    private float clientOnlyBikePitch = 0.0F;
    protected boolean jumping;
    private boolean saveTime = false;
    private boolean saveDistance = false;
    private float rearWheelSpeed = 0.0F;
    private BlockPos lastPos = null;
    private float currentPitch = 0;
    private int pitchTickingCount = 0;
    private int pitchTargetTicking = 0;
    private double lastEyeY = 0;
    protected AbstractBike(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
        this.rotations.put("backWheelRotation", new RotationData());
        this.rotations.put("steeringYaw", new RotationData());
        this.rotations.put("tilt", new RotationData());
        this.rotations.put("pitch", new RotationData());
    }

    public static AttributeSupplier.@NotNull Builder createBaseHorseAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22499999403953552)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.JUMP_STRENGTH, 0.5D);
    }

    public static EntityDataAccessor<Boolean> getHasChest() {
        return HAS_CHEST;
    }

    /**
     * Adds a listener for when the bike is moving
     *
     * @param listener A lambda that takes the bike, the speed in meters per tick and if the bike is moving forward
     */
    public static void addOnMoveListener(TriConsumer<AbstractBike, Float, Boolean> listener) {
        onMoveListeners.add(listener);
    }

    /**
     * Removes a listener for when the bike is moving
     *
     * @param listener The listener to remove
     */
    public static void removeOnMoveListener(TriConsumer<AbstractBike, Float, Boolean> listener) {
        onMoveListeners.remove(listener);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("SaveTime", this.saveTime);
        compound.putBoolean("SaveDistance", this.saveDistance);
        compound.putBoolean("HasChest", this.hasChest());
        compound.putFloat("BlocksTravelled", this.getBlocksTravelled());
        compound.putInt("TicksTravelled", this.getTicksPedalled());
        compound.putBoolean("HealthAffectsSpeed", this.isHealthAffectingSpeed());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.saveTime = compound.getBoolean("SaveTime");
        this.saveDistance = compound.getBoolean("SaveDistance");
        this.setChested(compound.getBoolean("HasChest"));
        this.setBlocksTravelled(compound.getFloat("BlocksTravelled"));
        this.setTicksPedalled(compound.getInt("TicksTravelled"));
        this.setHealthAffectsSpeed(compound.getBoolean("HealthAffectsSpeed"));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TILT, 0.0F);
        builder.define(STEERING_YAW, 0.0F);
        builder.define(BACKWHEEL_ROTATION, 0.0F);
        builder.define(FRONWHEEL_ROTATION, 0.0F);
        builder.define(LAST_SPEED, 0.0F);
        builder.define(INTERNAL_SPEED, 0.0F);
        builder.define(BRAKING, false);
        builder.define(HEALTH_AFF_SPEED, false);
        builder.define(HAS_CHEST, false);
        builder.define(LAST_ROT_Y, 0.0F);
        builder.define(TICKS_PEDALLED, 0);
        builder.define(BLOCKS_TRAVELLED, 0.0F);
        builder.define(SYNCED_BIKE_PITCH, 0.0F);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof Player player) {
            return player;
        }

        return super.getControllingPassenger();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BikeBondWithPlayerGoal(this, 1.3));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() || (this.getPassengers().size() < 2 && !this.hasChest());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void heal(float healAmount) {
    }

    @Override
    protected void followMommy() {
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    protected void createInventory() {
        super.createInventory();
    }

    @Override
    protected void executeRidersJump(float strength, Vec3 movementInput) {
        super.executeRidersJump(strength, movementInput);
        // Calculate the pitch of the bike
        // taking into account the strength of the jump
        if (this.level().isClientSide()) {
            this.setClientPitch(this.getClientPitch()+ strength * 0.25F);
        }
    }

    @Override
    public boolean canJump() {
        return !this.isInWater();
    }

    @Override
    public void handleStartJump(int height) {
        this.jumping = true;
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return this.getSpeed();
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        this.setClientPitch(0.0F);
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.getPassengers().isEmpty() && !this.canAddPassenger(player)) {
            return super.mobInteract(player, hand);

        } else if (player.isShiftKeyDown()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        this.doPlayerRide(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Environment(EnvType.CLIENT)
    public float calculateBikePitchDown(Vec3 frontWheel, Vec3 backWheel, boolean showRays) {
        // Do a raycast down to see if we are on the ground
        Vec3 raycastDir = new Vec3(0, -1F, 0);

        BlockHitResult rayFront = this.level().clip(new ClipContext(frontWheel, frontWheel.add(raycastDir), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        BlockHitResult rayBack = this.level().clip(new ClipContext(backWheel, backWheel.add(raycastDir), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        Vec3 rayFrontPos = rayFront.getLocation();
        Vec3 rayBackPos = rayBack.getLocation();

        if (showRays) {
            float particleScale = 0.2F * this.getModelScalingFactor();
            this.level().addParticle(new DustParticleOptions(new Vector3f(255F, 0F, 0F), particleScale), frontWheel.x, frontWheel.y, frontWheel.z, 0, 0, 0);
            this.level().addParticle(new DustParticleOptions(new Vector3f(0F, 0F, 255F), particleScale), backWheel.x, backWheel.y, backWheel.z, 0, 0, 0);

            if (rayFront.getType() == HitResult.Type.BLOCK) {
                this.level().addParticle(new DustParticleOptions(new Vector3f(0F, 255F, 0F), particleScale), rayFrontPos.x, rayFrontPos.y, rayFrontPos.z, 0, 0, 0);
            } else {
                this.level().addParticle(new DustParticleOptions(new Vector3f(255F, 255F, 0F), particleScale), rayFrontPos.x, rayFrontPos.y, rayFrontPos.z, 0, 0, 0);
            }

            if (rayBack.getType() == HitResult.Type.BLOCK) {
                this.level().addParticle(new DustParticleOptions(new Vector3f(0F, 255F, 0F), particleScale), rayBackPos.x, rayBackPos.y, rayBackPos.z, 0, 0, 0);
            } else {
                this.level().addParticle(new DustParticleOptions(new Vector3f(255F, 255F, 0F), particleScale), rayBackPos.x, rayBackPos.y, rayBackPos.z, 0, 0, 0);
            }
        }

        // When there is air below one of the wheels
        // we calculate the pitch based on the distance the raycast hit
        // We pitch the bike negatively when the front wheel is in the air and the back wheel is on the ground
        // and positively when the back wheel is in the air and the front wheel is on the ground
        float newRadiansPitch = 0.0F;
        float distanceFront = (float) (frontWheel.y - rayFrontPos.y);
        float distanceBack = (float) (backWheel.y - rayBackPos.y);
        boolean frontOnGround = rayFront.getType() == HitResult.Type.BLOCK;
        boolean backOnGround = rayBack.getType() == HitResult.Type.BLOCK;

        if (frontOnGround && backOnGround) {
            // Both wheels on ground, calculate pitch based on height difference
            newRadiansPitch = (float) Math.atan2(distanceBack - distanceFront, frontWheel.distanceTo(backWheel));
        } else if (!frontOnGround && backOnGround) {
            // Front wheel in air, back wheel on ground
            newRadiansPitch = -(float) Math.atan2(distanceFront, frontWheel.distanceTo(backWheel));
        } else if (frontOnGround) {
            // Front wheel on ground, back wheel in air
            newRadiansPitch = (float) Math.atan2(distanceBack, frontWheel.distanceTo(backWheel));
        } else {
            newRadiansPitch += (float) (this.getSpeed() * 0.5F * Math.PI);
        }

        return newRadiansPitch;
    }

    @Environment(EnvType.CLIENT)
    private int calculateMinWaitForPitchCalc(float speed, float verticalSpeed) {
        // Get current motion vectors
        final float MAX_DELAY_TICKS = ClientConfig.CONFIG.instance().getMaximumRaycastWait();
        final float MIN_DELAY_TICKS = ClientConfig.CONFIG.instance().getMinimumRaycastWait();
        final float VERTICAL_THRESHOLD = ClientConfig.CONFIG.instance().getVerticalThreshold();
        final float VERTICAL_SENSITIVITY = ClientConfig.CONFIG.instance().getVerticalSensitivity();
        final float SPEED_SENSITIVITY = ClientConfig.CONFIG.instance().getSpeedSensitivity();

        // Calculate base delay from horizontal speed
        float baseDelay = MAX_DELAY_TICKS - (MAX_DELAY_TICKS * (speed * SPEED_SENSITIVITY) /
                (1 + speed * SPEED_SENSITIVITY));

        // Apply vertical movement modifier
        float verticalFactor = 1.0F;
        if (verticalSpeed > 0) {
            verticalFactor = Math.max(0.2F,
                    1.0F - (verticalSpeed / VERTICAL_THRESHOLD) * VERTICAL_SENSITIVITY);
        }

        // Calculate final delay
        float adjustedDelay = baseDelay * verticalFactor;

        // Ensure within bounds and convert to ticks
        return Math.max((int)MIN_DELAY_TICKS,
                Math.min((int)adjustedDelay, (int)MAX_DELAY_TICKS));
    }

    @Environment(EnvType.CLIENT)
    private void syncServerPitch() {
        if (Minecraft.getInstance().player != null) {
            UUID clientPlayer = Minecraft.getInstance().player.getUUID();
            if (this.getFirstPassenger() instanceof Player player && player.getUUID() == clientPlayer) {
                if (ClientConfig.CONFIG.instance().syncPitchWithServer()) {
                    this.setSyncedPitch(this.currentPitch);
                } else {
                    this.setSyncedPitch(0);
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private float calculatePitch(int maxRaycasts) {
        // Convert to diameter (well, a bit less than that), and split it into the amount of raycasts
        final float raycastSteps = this.getWheelRadius() * 1.5F / maxRaycasts;

        // At (0,0,contactpoint)
        Vec3 frontWheelPos = this.getFrontWheelPos().scale(this.getModelScalingFactor());
        Vec3 backWheelPos = this.getBackWheelPos().scale(this.getModelScalingFactor());
        Vec3 pivotPoint = this.getFrontPivotPos().scale(this.getModelScalingFactor());

        // We start at the negative side of the wheel
        float currentRaycastCoordinate = -(maxRaycasts / 2F) * raycastSteps;

        float[] calculatedPitches = new float[maxRaycasts];

        for (int i = 0; i < maxRaycasts; i++) {
            // First we need to calculate the Y coordinate
            // knowing the formula for a circle, we can approximate the height
            // at this point in the circle
            float y = this.getWheelRadius() - (float) Math.sqrt(Math.pow(this.getWheelRadius(), 2) - Math.pow(currentRaycastCoordinate, 2));
            Vec3 frontPos = frontWheelPos.add(0, y, currentRaycastCoordinate);

            // Manually rotate it around pivot point
            Vec3 rotatedFront = frontPos.subtract(pivotPoint).yRot(this.getSteeringYaw()).add(pivotPoint);

            Vec3 frontWheel = this.modelToWorldPos(rotatedFront);
            Vec3 backWheel = this.modelToWorldPos(backWheelPos.add(0, y, currentRaycastCoordinate));

            calculatedPitches[i] = this.calculateBikePitchDown(frontWheel, backWheel, ClientConfig.CONFIG.instance().showDebugWheelRays());

            currentRaycastCoordinate += raycastSteps; // Increment after processing
        }

        // Calculate the average pitch
        float sum = 0;
        for (float pitch : calculatedPitches) {
            sum += pitch;
        }
        return sum / maxRaycasts;
    }

    @Environment(EnvType.CLIENT)
    private void updatePhysicsPitch(float pitch, float speed) {
        this.currentPitch = pitch * (1 - Math.min(speed / 0.5F, 1));
    }

    @Environment(EnvType.CLIENT)
    private void tickPitch(float speed) {
        float verticalSpeed = (float)Math.abs(this.getEyeY() - lastEyeY);
        lastEyeY = this.getEyeY();

        if (this.isInWater() || this.isInLava()) {
            this.currentPitch = 0;
            return;
        }

        // Calculate current target ticking based on current vertical speed
        int currentTargetTicking = this.calculateMinWaitForPitchCalc(speed, verticalSpeed);

        // Adjust stored target if current target is lower (faster updates needed)
        if (currentTargetTicking < this.pitchTargetTicking) {
            this.pitchTargetTicking = currentTargetTicking;
        }

        // Update tick counter
        if (++this.pitchTickingCount > this.pitchTargetTicking) {
            this.pitchTickingCount = 0;
            this.pitchTargetTicking = currentTargetTicking;
            final int maxRaycasts = ClientConfig.CONFIG.instance().getAmountOfRaysPerWheel();

            this.updatePhysicsPitch(this.calculatePitch(maxRaycasts), speed);

            if (Math.abs(this.currentPitch - this.getSyncedPitch())> 0.1F) {
                this.syncServerPitch();
            }
        }

        float clientPitch = this.getClientPitch() + (this.currentPitch - this.getClientPitch()) * 0.25F;
        this.setClientPitch((float) Math.clamp(clientPitch, -Math.PI, Math.PI));
    }

    @Override
    public void tick() {
        super.tick();

        float speed = this.getSpeed();

        if (this.level().isClientSide() && ClientConfig.CONFIG.instance().pitchBasedOnBlocks()
        && this.shouldCalculatePitch()
        ) {
            tickPitch(speed);
        }


        if (this.getFirstPassenger() instanceof Player playerEntity) {
            if (!this.isSaddled()) {
                if (speed > 0.05F) {
                    // HUrt value that goes in 0.5 steps
                    float hurtAmount = Math.round(this.getSpeed() * 5F / 0.5F) * 0.5F;

                    if (hurtAmount > 0 && this.random.nextInt(100) < 10 * hurtAmount) {
                        playerEntity.hurt(new DamageSources(this.registryAccess()).sting(this), hurtAmount);
                    }
                }
            }

            if (!this.level().isClientSide()) {
                BlockPos currentPos = new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());

                if (speed > 0.05F && this.isSavingDistance() && (this.lastPos == null || !this.lastPos.equals(currentPos))) {
                    this.setBlocksTravelled(this.getBlocksTravelled() + speed / this.getWheelRadius());
                    this.lastPos = currentPos;
                }
            }

        }

        if (!this.level().isClientSide()) {
            if (this.getPassengers().isEmpty() && (Math.abs(speed) >= 0.05F || Math.abs(this.getSyncedPitch()) > 0)) {
                // Update manually the speed of the bike
                Vec2 newRots = this.updateRotations(new Vec2(this.getXRot(), this.getLastRotY()));

                // Apply the new rotations
                this.setXRot(newRots.x);
                this.setYRot(newRots.y);

                // Update movement
                this.updateMovement(0, 0);

                if (Math.abs(speed) <= 0.06F) {
                    this.setLastSpeed(0);
                    this.setInternalSpeed(0);
                }

                // System.out.printf("Speed: %f, Tilt: %f, Steering: %f, Rear Wheel: %f, Front Wheel: %f\n", this.getSpeed(), this.tilt, this.steeringYaw, this.backWheelRotation, this.frontWheelRotation);

                this.travel(new Vec3(0, 0, 1));
            }

            if (speed > 0F) {
                AABB aABB = this.getBoundingBox();

                this.level().getEntities(this, aABB).stream().filter((entity) -> entity instanceof Mob).forEach((entity) -> {
                    Vec3 vec3 = entity.position().subtract(this.position()).normalize();
                    Vec3 pushMov = new Vec3(vec3.x * 0.5F, 0.23F, vec3.z * 0.5F);

                    entity.addDeltaMovement(pushMov);

                    LivingEntity source = this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                    DamageSource damageSource = new DamageSources(this.registryAccess()).sting(source);

                    // Round the speed to the nearest 0.5F increment
                    final float originalDamage = Math.round(speed / 0.5F) * 0.5F;

                    // As the entity gets smaller more damage we do
                    // as the entity gets bigger less damage we do
                    float damage = originalDamage * 2F / entity.getBbWidth() * entity.getBbHeight();

                    // Apply the damage to the entity
                    entity.hurt(damageSource, damage);

                    // Scale damage for us based on how big the entity is
                    damage = entity.getBbWidth() * entity.getBbHeight() * originalDamage;

                    // Do a random change of the bike getting damaged
                    if (this.random.nextInt(100) < 25 * damage) {
                        this.hurt(new DamageSources(this.registryAccess()).sting(this), damage);
                    }
                });
            }
        }
    }

    public Vec2 updateRotations(Vec2 playerRot) {
        float speed = this.getSpeed();

        // Calculate how much to spin the model
        float turnRate = getTurnRate(speed);
        float calculatedYaw = this.getYRot() - turnRate;

        // Make sure that we are in range
        calculatedYaw = Mth.wrapDegrees(calculatedYaw);
        float diff = this.getYRot() - playerRot.y;
        diff = Mth.wrapDegrees(diff);

        if (!this.level().isClientSide()) {
            float steeringYaw = (float) Math.toRadians(diff);
            steeringYaw = Math.clamp(steeringYaw, -this.getMaxSteeringAngle(), this.getMaxSteeringAngle());
            this.setSteeringYaw(steeringYaw);
        }
        // System.out.printf("Rot; Is client side: %b, Is controlled by local instance: %b\n", this.level().isClientSide(), this.isControlledByLocalInstance());

        return new Vec2(playerRot.x, calculatedYaw);
    }

    @Override
    protected @NotNull Vec2 getRiddenRotation(LivingEntity controllingPassenger) {
        if (!this.level().isClientSide()) {
            this.setLastRotY(controllingPassenger.getYRot());
        }
        return this.updateRotations(new Vec2(controllingPassenger.getXRot() * 0.5F, controllingPassenger.getYRot()));
    }

    public void updateMovement(float sideways, float forward) {
        float f = sideways * 0.5F;
        float g = forward;
        if (g <= 0.0F) {
            g *= 0.25F;
        }
        g *= this.getPedalMultiplier();
        if (this.isBraking()) {
            g = 0F;
        }

        double steerInf = this.getSpeed() > 0.08F ? (
                this.getSteeringYaw() / this.getMaxSteeringAngle() * 0.5F
        ) * this.getSpeed() * 1.4F : 0;

        this.getCenterMass().setPlayerOffset(new Vector3d(f + steerInf, 0, 0));

        // Rotate the wheels based on our speed knowing that
        // the g is a magnitude in blocks
        float lastSpeed = this.getSpeed();
        this.setLastSpeed(lastSpeed);

        float rotation;
        float movSpeed = 0F;
        boolean isJSerialCommActive = false;

        if (this.getControllingPassenger() instanceof Player player) {
            PlayerAccessor playerAcc = (PlayerAccessor) player;
            if (playerAcc.bikesarepain$isJSCActive()) {
                if (!this.level().isClientSide()) {
                    playerAcc.bikesarepain$setJSCSinceUpdate(playerAcc.bikesarepain$getJSCSinceUpdate() + 1);
                }
                if (playerAcc.bikesarepain$getJSCSinceUpdate() < 80) {
                    isJSerialCommActive = true;
                    movSpeed = ((PlayerAccessor) player).bikesarepain$getJSCSpeed() / 3.6F;
                    // Minecraft runs at 20 ticks per second
                    movSpeed /= 20F;

                    if (g < 0F) {
                        movSpeed *= -1;
                    }
                } else {
                    if (this.level().isClientSide()) {
                        // Warn the player that the JSerialComm is not active
                        player.displayClientMessage(
                                Component.translatable("bikesarepain.jserialcomm.timeout"),
                                false
                        );
                    }

                    playerAcc.bikesarepain$setJSCActive(false);
                }
            }
        }
        if (!isJSerialCommActive) {
            rotation = (g * this.getMaxPedalAnglePerSecond()) / 20F;
            movSpeed = rotation * this.getWheelRadius();
        }

        if (this.isBraking() && this.onGround()) {
            lastSpeed = (float) (lastSpeed * Math.exp(-this.getBrakeMultiplier() * 0.25F));
            if (lastSpeed > 0F) {
                this.playBrakeSound();
                BlockPos floorPos = this.blockPosition().below();
                BlockState floorState = this.level().getBlockState(floorPos);

                // Scale the amount based on the speed
                int amount = (int) Math.ceil(lastSpeed * 10);
                amount = Math.min(amount, 10);

                Vec3 frontWheelPos = this.getFrontWheelPos();
                Vec3 backWheelPos = this.getBackWheelPos();

                double yRot = Math.toRadians(this.getYRot());

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
                    this.level().addParticle(particle, this.getX() + frontWheelParticlePos.x, this.getY() + frontWheelParticlePos.y, this.getZ() + frontWheelParticlePos.z, 0, 0, 0);
                    this.level().addParticle(particle, this.getX() + backWheelParticlePos.x, this.getY() + backWheelParticlePos.y, this.getZ() + backWheelParticlePos.z, 0, 0, 0);
                }
            }
        }

        boolean pressingForward = (Math.abs(g) > 0 && !isJSerialCommActive) || (isJSerialCommActive && Math.abs(movSpeed) > 0);

        // Run event listeners
        for (TriConsumer<AbstractBike, Float, Boolean> listener : onMoveListeners) {
            listener.accept(this, movSpeed, pressingForward);
        }

        if (pressingForward && !this.level().isClientSide()) {
            this.setTicksPedalled(this.getTicksPedalled() + 1);
        }

        float gravityAcceleration = 0F;
        // Correct for model's usage of pitch calculation
        float pitch = -this.getSyncedPitch();
        if (Math.abs(pitch) > 0.05F) {
            gravityAcceleration = (float) (Math.sin(pitch) * this.getGravity()) * 0.25F;
            float maxGravityAcc = 0.1F;
            gravityAcceleration = Math.max(-maxGravityAcc, Math.min(maxGravityAcc, gravityAcceleration));
        }

        if (!pressingForward) {
            movSpeed = lastSpeed * this.inertiaFactor() + gravityAcceleration;
            if (Math.abs(movSpeed) < 0.05F) {
                movSpeed *= 0.8F;
                if (Math.abs(movSpeed) < 0.003F) {
                    movSpeed = 0;
                }
            }
        } else {
            float acceleration = (movSpeed - lastSpeed) * (1.15F - this.inertiaFactor());
            movSpeed = lastSpeed + acceleration + gravityAcceleration;
        }
        final float maxSpeed = this.level().getGameRules().getRule(GameRuleManager.MAX_BIKE_SPEED).get() / 20F;
        movSpeed = Math.clamp(movSpeed, -maxSpeed, maxSpeed);

        rotation = movSpeed / this.getWheelRadius();

        this.setRearWheelSpeed(rotation / (2 * (float) Math.PI));

        float backWheelRotation = this.getBackWheelRotation() + rotation;
        this.setBackWheelRotation(Utils.wrapRotation(backWheelRotation));

        // Calculate the tilt of the bike
        float newTilt = (float) (Math.PI / 2 + this.getCenterMass().calculateRollAngle());
        newTilt = Math.clamp(newTilt, -this.getMaxTiltAngle(), this.getMaxTiltAngle());

        float tilt = this.getTilt();
        this.setTilt(tilt + (newTilt - tilt) * 0.25F);

        this.setInternalSpeed(movSpeed);
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(Player controllingPlayer, Vec3 movementInput) {
        this.updateMovement(controllingPlayer.xxa, controllingPlayer.zza);

        return new Vec3(0.0, 0.0, 1.0F);
    }

    @Override
    public float getSpeed() {
        if (this.isHealthAffectingSpeed()) {
            return this.getInternalSpeed() * this.getSpeedFactor(this.getHealth() / this.getMaxHealth());
        }
        return this.getInternalSpeed();
    }

    // Vanilla Abstracting methods
    @Override
    protected abstract @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor);

    // Getters and Setters
    public float getTilt() {
        return this.entityData.get(TILT);
    }

    public void setTilt(float tilt) {
        this.entityData.set(TILT, tilt);
    }

    public float getSteeringYaw() {
        return this.entityData.get(STEERING_YAW);
    }

    public void setSteeringYaw(float steeringYaw) {
        this.entityData.set(STEERING_YAW, steeringYaw);
    }

    public float getBackWheelRotation() {
        return this.entityData.get(BACKWHEEL_ROTATION);
    }

    public void setBackWheelRotation(float backWheelRotation) {
        this.entityData.set(BACKWHEEL_ROTATION, backWheelRotation);
    }

    public float getFrontWheelRotation() {
        return this.entityData.get(FRONWHEEL_ROTATION);
    }

    public void setFrontWheelRotation(float frontWheelRotation) {
        this.entityData.set(FRONWHEEL_ROTATION, frontWheelRotation);
    }

    public float getLastSpeed() {
        return this.entityData.get(LAST_SPEED);
    }

    public void setLastSpeed(float lastSpeed) {
        this.entityData.set(LAST_SPEED, lastSpeed);
    }

    public float getSpeedHealthMul(float healthRatio) {
        return 0.85F;
    }

    public float getSpeedFactor(float healthRatio) {
        return Math.clamp(1 - this.getSpeedHealthMul(healthRatio) * (1 - healthRatio) * (1 - healthRatio), 0F, 1F);
    }

    public float getInternalSpeed() {
        return this.entityData.get(INTERNAL_SPEED);
    }

    public void setInternalSpeed(float internalSpeed) {
        this.entityData.set(INTERNAL_SPEED, internalSpeed);
    }

    public boolean isBraking() {
        return this.entityData.get(BRAKING);
    }

    public void setBraking(boolean braking) {
        this.entityData.set(BRAKING, braking);
    }

    /**
     * Gets the speed of the rear wheel in Revolutions per tick
     *
     * @return The speed of the rear wheel in Revolutions per tick
     */
    public float getRearWheelSpeed() {
        // Gets the speed of the rear wheel in Revolutions per tick
        return this.rearWheelSpeed;
    }

    public void setRearWheelSpeed(float rearWheelSpeed) {
        this.rearWheelSpeed = rearWheelSpeed;
    }

    public boolean shouldCalculatePitch() {
        return !this.isInLiquid();
    }

    // These define the bike's physical properties
    public abstract CenterMass getCenterMass();

    public abstract float getMaxTiltAngle();

    public abstract float getMaxSteeringAngle();

    public abstract Vec3 getModelSize();

    public abstract float getMaxPedalAnglePerSecond();

    public abstract float getMaxTurnRate();

    public abstract float getTurnScalingFactor();

    public abstract float inertiaFactor();

    public abstract float getPedalMultiplier();

    public abstract float getBrakeMultiplier();

    public abstract void playBrakeSound();

    public abstract Vec3 getFrontWheelPos();

    public abstract Vec3 getFrontPivotPos();

    public abstract Vec3 getBackWheelPos();

    /**
     * Gets the radius of the wheel in meters (where 1 block = 1 meter)
     *
     * @return The radius of the wheel in meters
     */
    public abstract float getWheelRadius();

    /**
     * The real size of the wheel in the model, that is later scaled to `getWheelRadius()`
     *
     * @return The real size of the wheel in the model (in blocks)
     */
    public abstract float getModelWheelRadius();

    // Custom methods
    public float getModelScalingFactor() {
        return this.getWheelRadius() / this.getModelWheelRadius();
    }

    public float getTheoreticalMaxSpeed() {
        return (this.getMaxPedalAnglePerSecond() / 20F) * this.getWheelRadius();
    }

    public void actuallyHeal(float healAmount) {
        super.heal(healAmount);
    }

    public float getTurnRate(float speed) {
        float tiltInf = (this.getTilt() / this.getMaxTiltAngle());
        float steeringInf = (this.getSteeringYaw() / this.getMaxSteeringAngle());

        float turnInfluence = tiltInf * 0.3F + steeringInf * 0.7F;
        float turnRate = turnInfluence * speed * this.getTurnScalingFactor();
        return Math.clamp(turnRate, -this.getMaxTurnRate(), this.getMaxTurnRate());
    }

    public Vec3 calculateBoxSize(Vec3 size, float pitch, float yaw) {
        // Convert pitch and yaw to radians
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        // Create rotation matrix
        Matrix3f rotationMatrix = new Matrix3f()
                .rotateY(yawRad)
                .rotateX(pitchRad);

        // Create vectors for the corners of the box
        Vector3f[] corners = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            corners[i] = new Vector3f(
                    (float) (((i & 1) == 0) ? -size.x : size.x),
                    (float) (((i & 2) == 0) ? -size.y : size.y),
                    (float) (((i & 4) == 0) ? -size.z : size.z)
            );
            rotationMatrix.transform(corners[i]);
        }

        // Find the maximum extent in each dimension
        Vector3f min = new Vector3f(Float.MAX_VALUE);
        Vector3f max = new Vector3f(-Float.MAX_VALUE);
        for (Vector3f corner : corners) {
            min.min(corner);
            max.max(corner);
        }

        // Return the size of the bounding box
        Vector3f newSize = max.sub(min);
        return new Vec3(newSize.x, newSize.y, newSize.z);
    }

    public float getSpeedInMetersPerSecond() {
        return this.getSpeed() * 20F * this.getWheelRadius();
    }

    public Player getRider() {
        return this.getControllingPassenger() instanceof Player ? (Player) this.getControllingPassenger() : null;
    }

    public int getTicksPedalled() {
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                return mixPlayer.bikesarepain$getJSCTimePedalled();
            }
        }

        return this.entityData.get(TICKS_PEDALLED);
    }

    public void setTicksPedalled(int ticksPedalled) {
        if (this.getFirstPassenger() instanceof Player player) {
            PlayerAccessor mixPlayer = (PlayerAccessor) player;
            if (mixPlayer.bikesarepain$isJSCActive()) {
                mixPlayer.bikesarepain$setJSCTimePedalled(ticksPedalled);
                return;
            }
        }

        this.entityData.set(TICKS_PEDALLED, ticksPedalled);
    }

    public float getBlocksTravelled() {
        return this.entityData.get(BLOCKS_TRAVELLED);
    }

    public void setBlocksTravelled(float blocksTravelled) {
        this.entityData.set(BLOCKS_TRAVELLED, blocksTravelled);
    }

    public void setSaveTime(boolean saveTime) {
        this.saveTime = saveTime;
    }

    public void setSaveDistance(boolean saveDistance) {
        this.saveDistance = saveDistance;
    }

    public boolean isSavingTime() {
        return this.saveTime;
    }

    public boolean isSavingDistance() {
        return this.saveDistance;
    }

    public boolean isHealthAffectingSpeed() {
        return this.entityData.get(HEALTH_AFF_SPEED);
    }

    public void setHealthAffectsSpeed(boolean healthAffectsSpeed) {
        this.entityData.set(HEALTH_AFF_SPEED, healthAffectsSpeed);
    }

    public float getRadianYRot() {
        return (float) Math.toRadians(this.getYRot());
    }

    public Vec3 modelToWorldPos(Vec3 pos) {
        float angle = this.getRadianYRot();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        // Rotate the position by the entity's rotation
        Vec3 posRot = new Vec3(
                pos.x() * cos - pos.z() * sin,
                pos.y(),
                pos.x() * sin + pos.z() * cos
        );

        return this.position().add(posRot);
    }

    public boolean hasChest() {
        return this.entityData.get(HAS_CHEST);
    }

    public void setChested(boolean hasChest) {
        this.entityData.set(HAS_CHEST, hasChest);
    }

    public float getLastRotY() {
        return this.entityData.get(LAST_ROT_Y);
    }

    public void setLastRotY(float lastRotY) {
        this.entityData.set(LAST_ROT_Y, lastRotY);
    }

    @Environment(EnvType.CLIENT)
    public float getClientPitch() {
        return this.clientOnlyBikePitch;
    }

    @Environment(EnvType.CLIENT)
    public void setClientPitch(float pitch) {
        this.clientOnlyBikePitch = pitch;
    }

    public float getSyncedPitch() {
        return this.entityData.get(SYNCED_BIKE_PITCH);
    }

    // Event listeners

    public void setSyncedPitch(float pitch) {
        this.entityData.set(SYNCED_BIKE_PITCH, pitch);
    }

    @Environment(EnvType.CLIENT)
    public static class RotationData {
        public float rotation;
        public long lastUpdated;

        public RotationData() {
            this.rotation = 0;
            this.lastUpdated = 0;
        }

        public RotationData(float rotation, long lastUpdated) {
            this.rotation = rotation;
            this.lastUpdated = lastUpdated;
        }

        public float getRotation() {
            return this.rotation;
        }

        public void setRotation(float rotation) {
            this.rotation = rotation;
            this.lastUpdated = System.currentTimeMillis();
        }

        // Recalculates if needed, updates the last value and returns the new value
        public float reCalculateIfOld(Supplier<Float> valueSupplier, float originalForNoInterpolation) {
            if (ClientConfig.CONFIG.instance().useInterpolation()) {
                float maxTimePerFrameMs = Minecraft.getInstance().getFps() / 1000.0f;
                if (System.currentTimeMillis() - this.lastUpdated > maxTimePerFrameMs) {
                    this.setRotation(valueSupplier.get());
                }
                return this.rotation;
            }

            return originalForNoInterpolation;
        }
    }
}
