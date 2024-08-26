package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.server.entity.ai.BikeBondWithPlayerGoal;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBike extends AbstractHorse implements PlayerRideableJumping, Saddleable {
    protected boolean jumping;
   // public float tilt = 0.0F;
    //public float steeringYaw = 0.0F;
    //public float frontWheelRotation = 0.0F;
    //public float backWheelRotation = 0.0F;
    //public float lastSpeed = 0.0F;
    public boolean hasChest = false;
    public boolean wasRingedAlready = false;
    public float bikePitch = 0.0F;
    //public float internalSpeed = 0.0F;
    private float rearWheelSpeed = 0.0F;

    // DataParameters for commented out variables
    private static final EntityDataAccessor<Float> TILT = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEERING_YAW = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BACKWHEEL_ROTATION = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FRONWHEEL_ROTATION = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAST_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> INTERNAL_SPEED = SynchedEntityData.defineId(AbstractBike.class, EntityDataSerializers.FLOAT);


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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TILT, 0.0F);
        builder.define(STEERING_YAW, 0.0F);
        builder.define(BACKWHEEL_ROTATION, 0.0F);
        builder.define(FRONWHEEL_ROTATION, 0.0F);
        builder.define(LAST_SPEED, 0.0F);
        builder.define(INTERNAL_SPEED, 0.0F);
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
    protected @NotNull AABB makeBoundingBox() {
        // I (probably) should not be modifying the bounding box like this
        //Vec3 position = this.position();
        //Vec3 boxSize = this.calculateBoxSize(this.getModelSize(), 0, this.getYRot());
        //return new AABB(position.x - boxSize.x / 2, position.y, position.z - boxSize.z / 2, position.x + boxSize.x / 2, position.y + boxSize.y, position.z + boxSize.z / 2);
        return super.makeBoundingBox();
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
        if (!this.isSaddled() && this.getFirstPassenger() instanceof Player playerEntity) {
            playerEntity.hurt(new DamageSources(this.registryAccess()).sting(this), 0.5F);
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

    public void updateMovement(float forward, float sideways) {
        float f = forward * 0.5F;
        float g = sideways;
        if (g <= 0.0F) {
            g *= 0.25F;
        }
        // System.out.printf("MOV; Is client side: %b, Is controlled by local instance: %b\n", this.level().isClientSide(), this.isControlledByLocalInstance());

        // Print all relevant information
        // System.out.printf("Forward: %f, Sideways: %f, Speed: %f, Tilt: %f, Steering: %f, Rear Wheel: %f, Front Wheel: %f\n", f, g, this.getSpeed(), this.tilt, this.steeringYaw, this.backWheelRotation, this.frontWheelRotation);

        this.getCenterMass().setPlayerOffset(new Vector3d(f,0,0));

        // Rotate the wheels based on our speed knowing that
        // the g is a magnitude in blocks
        float lastSpeed = this.getSpeed();
        this.setLastSpeed(lastSpeed);
        float rotation = (g * this.getMaxPedalAnglePerSecond())/20F;
        float movSpeed = rotation * this.getBackWheelRadius();

        if (g == 0) {
            movSpeed = lastSpeed * this.inertiaFactor();
            if (movSpeed < 0.05F) {
                movSpeed = 0;
            }
            rotation = movSpeed / this.getBackWheelRadius();
        } else {
            movSpeed = lastSpeed + (movSpeed - lastSpeed) * (1.15F-this.inertiaFactor());
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

    public float getInternalSpeed() { return this.entityData.get(INTERNAL_SPEED);}
    public void setInternalSpeed(float internalSpeed) { this.entityData.set(INTERNAL_SPEED, internalSpeed);}

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
    public abstract float getBackWheelRadius();
    public abstract float getMaxTiltAngle();
    public abstract float getMaxSteeringAngle();
    public abstract Vec3 getModelSize();
    public abstract float getMaxPedalAnglePerSecond();
    public abstract float getMaxTurnRate();
    public abstract float getTurnScalingFactor();
    public abstract float inertiaFactor();

    // Custom methods
    public float getTheoreticalMaxSpeed() {
        return (this.getMaxPedalAnglePerSecond()/20F) * this.getBackWheelRadius();
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

}
