package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.server.entity.ai.BikeBondWithPlayerGoal;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.CommonColors;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public abstract class AbstractBike extends AbstractHorse implements PlayerRideableJumping, Saddleable {
    protected boolean jumping;

    private boolean saveTime = false;
    private boolean saveDistance = false;

    public boolean hasChest = false;
    public float bikePitch = 0.0F;
    private float rearWheelSpeed = 0.0F;

    private float blocksTravelled = 0.0F;
    private int ticksTravelled = 0;

    // DataParameters for commented out variables
    private static final EntityDataAccessor<Float> TILT = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEERING_YAW = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BACKWHEEL_ROTATION = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FRONWHEEL_ROTATION = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAST_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> INTERNAL_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BRAKING = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEALTH_AFF_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.BOOLEAN);


    protected AbstractBike(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createBaseHorseAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22499999403953552)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.4D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("SaveTime", this.saveTime);
        compound.putBoolean("SaveDistance", this.saveDistance);
        compound.putBoolean("HasChest", this.hasChest);
        compound.putFloat("BlocksTravelled", this.blocksTravelled);
        compound.putInt("TicksTravelled", this.ticksTravelled);
        compound.putBoolean("HealthAffectsSpeed", this.isHealthAffectingSpeed());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.saveTime = compound.getBoolean("SaveTime");
        this.saveDistance = compound.getBoolean("SaveDistance");
        this.hasChest = compound.getBoolean("HasChest");
        this.blocksTravelled = compound.getFloat("BlocksTravelled");
        this.ticksTravelled = compound.getInt("TicksTravelled");
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
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BikeBondWithPlayerGoal(this, 1.3));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2 && !hasChest;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void heal(float healAmount) {}

    @Override
    protected void followMommy() {}

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    protected void executeRidersJump(float strength, Vec3 movementInput) {
        super.executeRidersJump(strength, movementInput);
        bikePitch = 10F;
    }

    @Override
    public boolean canJump() {
        return true;
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
        this.bikePitch = 0.0F;
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

    @Override
    public void tick() {
        super.tick();
        if (this.getFirstPassenger() instanceof Player playerEntity) {
            if (!this.level().isClientSide()) {

                if (((PlayerAccessor) playerEntity).bikesarepain$isJSCActive()) {
                    // Display colored message
                    // "Speed": red, "Distance": green, "Kcalories": blue
                    playerEntity.displayClientMessage(Component.literal("Speed: ").withColor(CommonColors.GREEN)
                            .append(Component.literal(
                                            ((float) Math.round(
                                                    ((PlayerAccessor) playerEntity).bikesarepain$getJSCSpeed()
                                                    *100))/100 + " km/h "
                                            ).withColor(CommonColors.RED)
                                    .append(Component.literal("Distance: ").withColor(CommonColors.GREEN)
                                            .append(Component.literal(((float) Math.round(
                                                            ((PlayerAccessor) playerEntity).bikesarepain$getJSCDistance()
                                                    *100))/100 + " meters ").withColor(CommonColors.BLUE)
                                                    .append(Component.literal("Calories Spent: ").withColor(CommonColors.GREEN)
                                                            .append(Component.literal(
                                                                    ((float) Math.round(
                                                                            ((PlayerAccessor) playerEntity).bikesarepain$getJSCCalories()
                                                                            *100))/100 + " kcal").withColor(CommonColors.BLUE)))))), true);
                }
            }
            if (!this.isSaddled()) {
                playerEntity.hurt(new DamageSources(this.registryAccess()).sting(this), 0.5F);
            }

        }

        if (!this.level().isClientSide()) {
            if (this.getPassengers().isEmpty() && this.getSpeed() >= 0.05F && !this.level().isClientSide()) {
                // Update manually the speed of the bike
                Vec2 newRots = this.updateRotations(new Vec2(this.getXRot(), this.getYRot()));

                // Apply the new rotations
                this.setXRot(newRots.x);
                this.setYRot(newRots.y);

                // Update movement
                this.updateMovement(0, 0);

                if (this.getSpeed() <= 0.06F) {
                    this.setLastSpeed(0);
                    this.setInternalSpeed(0);
                }

                // System.out.printf("Speed: %f, Tilt: %f, Steering: %f, Rear Wheel: %f, Front Wheel: %f\n", this.getSpeed(), this.tilt, this.steeringYaw, this.backWheelRotation, this.frontWheelRotation);

                this.travel(new Vec3(0, 0, 1));
            }

            if (this.getSpeed() > 0F) {
                AABB aABB = this.getBoundingBox();

                this.level().getEntities(this, aABB).stream().filter((entity) -> entity instanceof Mob).forEach((entity) -> {
                    Vec3 vec3 = entity.position().subtract(this.position()).normalize();
                    Vec3 pushMov = new Vec3(vec3.x * 0.5F, 0.23F, vec3.z * 0.5F);

                    entity.addDeltaMovement(pushMov);

                    LivingEntity source = this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                    DamageSource damageSource = new DamageSources(this.registryAccess()).sting(source);

                    // Round the speed to the nearest 0.5F increment
                    final float originalDamage = Math.round(this.getSpeed() / 0.5F) * 0.5F;

                    // As the entity gets smaller more damage we do
                    // as the entity gets bigger less damage we do
                    float damage = originalDamage * 2F / entity.getBbWidth() * entity.getBbHeight();

                    // Apply the damage to the entity
                    entity.hurt(damageSource, damage);

                    // Scale damage for us based on how big the entity is
                    damage = entity.getBbWidth() * entity.getBbHeight() * originalDamage;

                    // Do a random change of the bike getting damaged
                    if (this.random.nextInt(100) < 25* damage) {
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

        float steeringYaw = (float) Math.toRadians(diff);
        steeringYaw = Math.clamp(steeringYaw, -this.getMaxSteeringAngle(), this.getMaxSteeringAngle());
        this.setSteeringYaw(steeringYaw);

        // System.out.printf("Rot; Is client side: %b, Is controlled by local instance: %b\n", this.level().isClientSide(), this.isControlledByLocalInstance());

        return new Vec2(playerRot.x, calculatedYaw);
    }

    @Override
    protected @NotNull Vec2 getRiddenRotation(LivingEntity controllingPassenger) {
        if (this.isControlledByLocalInstance()) {
            return this.updateRotations(new Vec2(controllingPassenger.getXRot()*0.5F, controllingPassenger.getYRot()));
        }

        return new Vec2(this.getXRot(), this.getYRot());
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

        // System.out.printf("MOV; Is client side: %b, Is controlled by local instance: %b\n", this.level().isClientSide(), this.isControlledByLocalInstance());

        // Print all relevant information
        // System.out.printf("Forward: %f, Sideways: %f, Speed: %f, Tilt: %f, Steering: %f, Rear Wheel: %f, Front Wheel: %f\n", f, g, this.getSpeed(), this.tilt, this.steeringYaw, this.backWheelRotation, this.frontWheelRotation);

        this.getCenterMass().setPlayerOffset(new Vector3d(f,0,0));

        // Rotate the wheels based on our speed knowing that
        // the g is a magnitude in blocks
        float lastSpeed = this.getSpeed();
        this.setLastSpeed(lastSpeed);

        float rotation;
        float movSpeed = 0F;
        boolean isJSerialCommActive = false;

        if (this.getControllingPassenger() instanceof Player player) {
            if (((PlayerAccessor) player).bikesarepain$isJSCActive()) {
                isJSerialCommActive = true;
                movSpeed = ((PlayerAccessor) player).bikesarepain$getJSCSpeed() / 3.6F;
                // Minecraft runs at 20 ticks per second
                movSpeed /= 20F;

                if (g < 0F) {
                    movSpeed *= -1;
                }
            }
        }
        if (!isJSerialCommActive) {
            rotation = (g * this.getMaxPedalAnglePerSecond())/20F;
            movSpeed = rotation * this.getWheelRadius();
        }

        if (this.isBraking() && this.onGround()) {
            lastSpeed = (float) (lastSpeed * Math.exp(-this.getBrakeMultiplier()*0.25F));
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
                for (int i = 0; i < amount*2; i++) {
                    this.level().addParticle(particle, this.getX() + frontWheelParticlePos.x, this.getY() + frontWheelParticlePos.y, this.getZ() + frontWheelParticlePos.z, 0, 0, 0);
                    this.level().addParticle(particle, this.getX() + backWheelParticlePos.x, this.getY() + backWheelParticlePos.y, this.getZ() + backWheelParticlePos.z, 0, 0, 0);
                }
            }
        }

        if ((g == 0 && !isJSerialCommActive) || (isJSerialCommActive && movSpeed == 0F)) {
            movSpeed = lastSpeed * this.inertiaFactor();
            if (movSpeed < 0.05F) {
                movSpeed = 0;
            }
            rotation = movSpeed / this.getWheelRadius();
        } else {
            movSpeed = lastSpeed + (movSpeed - lastSpeed) * (1.15F-this.inertiaFactor());
            rotation = movSpeed / this.getWheelRadius();
        }

        this.setRearWheelSpeed(rotation / (2 * (float) Math.PI));

        float backWheelRotation = this.getBackWheelRotation() + rotation;
        if (Math.abs(backWheelRotation) > 2*Math.PI) {
            backWheelRotation = rotation;
        }
        this.setBackWheelRotation(backWheelRotation);
        // Make the front wheel lag behind the back wheel
        float frontWheelRotation = this.getFrontWheelRotation();
        //this.frontWheelRotation = this.frontWheelRotation + (this.backWheelRotation - this.frontWheelRotation) * 0.25F;
        this.setFrontWheelRotation(frontWheelRotation + (backWheelRotation - frontWheelRotation) * 0.25F);

        // Calculate the tilt of the bike
        float newTilt = (float) (Math.PI/2 + this.getCenterMass().calculateRollAngle());
        newTilt = Math.clamp(newTilt, -this.getMaxTiltAngle(), this.getMaxTiltAngle());

        float tilt = this.getTilt();
        //this.tilt = this.tilt + (newTilt - this.tilt) * 0.25F;
        this.setTilt(tilt + (newTilt - tilt) * 0.25F);

        this.setInternalSpeed(movSpeed);
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(Player controllingPlayer, Vec3 movementInput) {
        this.updateMovement(controllingPlayer.xxa, controllingPlayer.zza);

        if (this.getSpeed() > 0.05F) {
            this.blocksTravelled += this.getSpeed();
            this.ticksTravelled++;
        }

        return new Vec3(0.0, 0.0, 1.0F);
    }

    @Override
    public float getSpeed() {
        return this.getInternalSpeed();
    }

    // Vanilla Abstracting methods
    @Override
    protected abstract @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor);

    // Getters and Setters
    public float getTilt() { return this.entityData.get(TILT);}
    public void setTilt(float tilt) { this.entityData.set(TILT, tilt);}

    public float getSteeringYaw() { return this.entityData.get(STEERING_YAW);}
    public void setSteeringYaw(float steeringYaw) { this.entityData.set(STEERING_YAW, steeringYaw);}

    public float getBackWheelRotation() { return this.entityData.get(BACKWHEEL_ROTATION);}
    public void setBackWheelRotation(float backWheelRotation) { this.entityData.set(BACKWHEEL_ROTATION, backWheelRotation);}

    public float getFrontWheelRotation() { return this.entityData.get(FRONWHEEL_ROTATION);}
    public void setFrontWheelRotation(float frontWheelRotation) { this.entityData.set(FRONWHEEL_ROTATION, frontWheelRotation);}

    public float getLastSpeed() { return this.entityData.get(LAST_SPEED);}
    public void setLastSpeed(float lastSpeed) { this.entityData.set(LAST_SPEED, lastSpeed);}

    public float getInternalSpeed() {
        if (this.isHealthAffectingSpeed()) {
            return this.entityData.get(INTERNAL_SPEED) * this.getHealth() / this.getMaxHealth();
        }

        return this.entityData.get(INTERNAL_SPEED);
    }
    public void setInternalSpeed(float internalSpeed) { this.entityData.set(INTERNAL_SPEED, internalSpeed);}

    public boolean isBraking() { return this.entityData.get(BRAKING);}
    public void setBraking(boolean braking) { this.entityData.set(BRAKING, braking);}

    /**
     * Gets the speed of the rear wheel in Revolutions per tick
     * @return The speed of the rear wheel in Revolutions per tick
     */
    public float getRearWheelSpeed() {
        // Gets the speed of the rear wheel in Revolutions per tick
        return this.rearWheelSpeed;
    }

    public void setRearWheelSpeed(float rearWheelSpeed) {
        this.rearWheelSpeed = rearWheelSpeed;
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
    public abstract Vec3 getBackWheelPos();

    /**
     * Gets the radius of the wheel in meters (where 1 block = 1 meter)
     * @return The radius of the wheel in meters
     */
    public abstract float getWheelRadius();

    /**
     * The real size of the wheel in the model, that is later scaled to `getWheelRadius()`
     * @return The real size of the wheel in the model (in blocks)
     */
    public abstract float getModelWheelRadius();

    // Custom methods
    public float getModelScalingFactor() {
        return this.getWheelRadius() / this.getModelWheelRadius();
    }

    public float getTheoreticalMaxSpeed() {
        return (this.getMaxPedalAnglePerSecond()/20F) * this.getWheelRadius();
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
        return this.getSpeed() * this.getWheelRadius();
    }

    public Player getRider() {
        return this.getControllingPassenger() instanceof Player ? (Player) this.getControllingPassenger() : null;
    }

    public float getBlocksTravelled() {
        return this.blocksTravelled;
    }

    public int getTicksTravelled() {
        return this.ticksTravelled;
    }

    public void setBlocksTravelled(float blocksTravelled) {
        this.blocksTravelled = blocksTravelled;
    }

    public void setTicksTravelled(int ticksTravelled) {
        this.ticksTravelled = ticksTravelled;
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

}
