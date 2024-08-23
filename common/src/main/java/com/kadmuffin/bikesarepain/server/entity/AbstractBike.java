package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.server.entity.ai.BikeBondWithPlayerGoal;
import com.kadmuffin.bikesarepain.server.helper.CenterMass;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public abstract class AbstractBike extends AbstractHorse implements PlayerRideableJumping, Saddleable {
    protected boolean jumping;
    public float tilt = 0.0F;
    public float steeringYaw = 0.0F;
    public boolean showGears = false;
    public float frontWheelRotation = 0.0F;
    public float backWheelRotation = 0.0F;
    public boolean hasChest = false;
    public float bikePitch = 0.0F;
    public float internalSpeed = 0.0F;

    // For arduino link
    private float jCommaSpeed = 0.0F;
    private float distanceTravelled = 0.0F;
    private float kcaloriesBurned = 0.0F;
    private float jCommaWheelRadius = 0.0F;
    private boolean jCommaEnabled = false;

    protected AbstractBike(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createBaseHorseAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22499999403953552);
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
        return this.internalSpeed;
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        this.tilt = 0.0F;
        this.bikePitch = 0.0F;
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        // I (probably) should not be modifying the bounding box like this
        Vec3 position = this.position();
        Vec3 boxSize = this.calculateBoxSize(this.getModelSize(), 0, this.getYRot());
        return new AABB(position.x - boxSize.x / 2, position.y, position.z - boxSize.z / 2, position.x + boxSize.x / 2, position.y + boxSize.y, position.z + boxSize.z / 2);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.getPassengers().isEmpty()) {
            return super.mobInteract(player, hand);

            // Check if the player is doing a right click
        } else if (player.isShiftKeyDown()) {
            if (player.getItemInHand(hand).getItem() == Items.DARK_OAK_SLAB && this.showGears) {
                this.showGears = false;
                this.playSound(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, 1.0F, Mth.nextFloat(this.random, 1F, 1.5F));
                // Remove one item from the player's hand
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (!this.showGears){
                this.showGears = true;
                this.playSound(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, 1.0F, Mth.nextFloat(this.random, 1F, 1.5F));
                // Give one slab
                player.addItem(new ItemStack(Items.DARK_OAK_SLAB, 1));
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
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
            if (!this.isSaddled() && !this.level().isClientSide()) {
                playerEntity.hurt(new DamageSources(this.registryAccess()).sting(this), (this.tilt /this.getMaxTiltAngle()));
            }
            if (this.isjCommaEnabled() && this.level().isClientSide()) {
                // Display colored message
                // "Speed": red, "Distance": green, "Kcalories": blue
                playerEntity.displayClientMessage(Component.literal("Speed: ").withColor(CommonColors.GREEN)
                                .append(Component.literal(String.format("%f ", this.getjCommaSpeed())).withColor(CommonColors.RED)
                                .append(Component.literal("Distance: ").withColor(CommonColors.GREEN)
                                        .append(Component.literal(String.format("%f meters ", this.getDistanceTravelled())).withColor(CommonColors.BLUE)
                                                        .append(Component.literal("KCal: ").withColor(CommonColors.GREEN)
                                                                .append(Component.literal(String.format("%f", this.getKcaloriesBurned())).withColor(CommonColors.BLUE)))))), true);
            }
        }


    }

    @Override
    protected @NotNull Vec2 getRiddenRotation(LivingEntity controllingPassenger) {
        float speed = this.getSpeed();

        // Calculate how much to spin the model
        float turnRate = getTurnRate(speed);

        // Calculate the yaw of the player and the bike
        float playerYaw = controllingPassenger.getYRot();
        float calculatedYaw = this.getYRot() - turnRate;

        // Make sure that we are in range
        calculatedYaw = Mth.wrapDegrees(calculatedYaw);
        float diff = this.getYRot() - playerYaw;
        diff = Mth.wrapDegrees(diff);

        this.steeringYaw = (float) Math.toRadians(diff);
        this.steeringYaw = Math.clamp(this.steeringYaw, -this.getMaxSteeringAngle(), this.getMaxSteeringAngle());

        return new Vec2(controllingPassenger.getXRot() * 0.5F, calculatedYaw);

    }

    @Override
    protected @NotNull Vec3 getRiddenInput(Player controllingPlayer, Vec3 movementInput) {
        float f = controllingPlayer.xxa * 0.5F;
        float g = controllingPlayer.zza;
        if (g <= 0.0F) {
            g *= 0.25F;
        }

        this.getCenterMass().setPlayerOffset(new Vector3d(f,0,0));

        this.frontWheelRotation = this.backWheelRotation;

        // Rotate the wheels based on our speed knowing that
        // the g is a magnitude in blocks
        float rotation = (float) (g * (Math.PI))/20F;
        float movSpeed = rotation * this.getBackWheelRadius();

        if (jCommaEnabled) {
            // The speed we get from the arduino is in km/h
            // we need to convert it to meters per second
            float speed = this.getjCommaSpeed() / 3.6F;

            // Minecraft runs at 20 ticks per second
            speed = speed / 20F;

            float expectedRotation = (speed / this.getjCommaWheelRadius());

            // Now we need to map the expected rotation to the actual rotation
            // as our wheel diameter is different
            rotation = expectedRotation * (this.getBackWheelRadius() / this.getjCommaWheelRadius());
            movSpeed = Math.signum(g) * speed;

            // We'll set jCommaEnabled to false, so we only update
            // the speed when we get a new packet
            setjCommaEnabled(false);
        }

        this.backWheelRotation += (float) (rotation % 2*Math.PI);

        // Calculate the tilt of the bike
        float newTilt = (float) (Math.toRadians(90) + this.getCenterMass().calculateRollAngle());
        newTilt = Math.clamp(newTilt, -this.getMaxTiltAngle(), this.getMaxTiltAngle());

        this.tilt = this.tilt + (newTilt - this.tilt) * 0.25F;

        this.internalSpeed = movSpeed;

        return new Vec3(0.0, 0.0, 1.0F);
    }

    // Vanilla Abstracting methods
    @Override
    protected abstract @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor);

    // These define the bike's physical properties
    public abstract CenterMass getCenterMass();
    public abstract float getBackWheelRadius();
    public abstract float getMaxTiltAngle();
    public abstract float getMaxSteeringAngle();
    public abstract Vec3 getModelSize();

    // Custom methods
    public float getTurnRate(float speed) {
        float tiltInf = (this.tilt / this.getMaxTiltAngle());
        float steeringInf = (this.steeringYaw / this.getMaxSteeringAngle());

        float turnInfluence = tiltInf * 0.3F + steeringInf * 0.7F;
        float turnRate = turnInfluence * speed * 8.0F;
        //System.out.printf("Tilt: %f, Steering: %f, Speed: %f, TurnRate: %f, ActualSteering: %f, ActualTilt: %f\n", tiltInf, steeringInf, speed, turnRate, this.steeringYaw, this.tilt);
        return (float) Math.clamp(turnRate, -Math.PI / 2, Math.PI / 2);
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

    // Get/Set for privates
    public float getjCommaSpeed() {
        return jCommaSpeed;
    }

    public void setjCommaSpeed(float jCommaSpeed) {
        this.jCommaSpeed = jCommaSpeed;
    }

    public float getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(float distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    public float getKcaloriesBurned() {
        return kcaloriesBurned;
    }

    public void setKcaloriesBurned(float kcaloriesBurned) {
        this.kcaloriesBurned = kcaloriesBurned;
    }

    public float getjCommaWheelRadius() {
        return jCommaWheelRadius;
    }

    public void setjCommaWheelRadius(float jCommaWheelRadius) {
        this.jCommaWheelRadius = jCommaWheelRadius;
    }

    public boolean isjCommaEnabled() {
        return jCommaEnabled;
    }

    public void setjCommaEnabled(boolean jCommaEnabled) {
        this.jCommaEnabled = jCommaEnabled;
    }
}
