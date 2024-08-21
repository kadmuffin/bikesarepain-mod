package com.kadmuffin.bikesarepain.server.entity;

import com.kadmuffin.bikesarepain.server.entity.ai.BikeBondWithPlayerGoal;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
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
import org.joml.Vector3f;

public abstract class AbstractBike extends AbstractHorse {
    protected boolean jumping;
    public float tilt = 0.0F;
    public float steeringYaw = 0.0F;
    public boolean showGears = true;
    public float frontWheelRotation = 0.0F;
    public float backWheelRotation = 0.0F;
    public float maxTiltAngle = 10.0F;
    public float maxSteeringAngle = 50.0F;
    public boolean hasChest = false;
    final float delta = 1.0F / 20.0F;
    public float bikePitch = 0.0F;

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

    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2 && !hasChest;
    }

    // This method calculates the turn rate
    public float getTurnRate(float speed) {
        float tiltInf = (this.tilt / this.maxTiltAngle);
        float steeringInf = (this.steeringYaw / this.maxSteeringAngle);

        float turnInfluence = tiltInf * 0.3F + steeringInf * 0.7F;
        float turnRate = turnInfluence * speed * 2.0F;
        System.out.printf("Tilt: %f, Steering: %f, Speed: %f, TurnRate: %f, ActualSteering: %f, ActualTilt: %f\n", tiltInf, steeringInf, speed, turnRate, this.steeringYaw, this.tilt);
        return Math.clamp(turnRate, -90F, 90F);
    }


    @Override
    protected @NotNull Vec2 getRiddenRotation(LivingEntity controllingPassenger) {
        float speed = this.getSpeed();

        // Calculate how much to spin the model and the wheels
        float turnRate = getTurnRate(speed);

        float playerYaw = controllingPassenger.getYRot();
        float calculatedYaw = this.getYRot() - turnRate;
        calculatedYaw = Mth.wrapDegrees(calculatedYaw);
        float diff = this.getYRot() - playerYaw;
        diff = Mth.wrapDegrees(diff);

        // Calculate SteeringYaw using the difference between the player yaw and the entity yaw
        // The steeringYaw is used for the handle and is in range -1F to 1F
        this.steeringYaw = diff;

        return new Vec2(controllingPassenger.getXRot() * 0.5F, calculatedYaw);

    }

    @Override
    protected @NotNull Vec3 getRiddenInput(Player controllingPlayer, Vec3 movementInput) {
        float f = controllingPlayer.xxa * 0.5F;
        float g = controllingPlayer.zza;
        if (g <= 0.0F) {
            g *= 0.25F;
        }

        // Add to the tilt angle based on sideways movement
        final float targetTilt = f * this.maxTiltAngle;
        this.tilt = this.tilt + (targetTilt - this.tilt) * delta;

        //System.out.printf("Tilt: %f, TargetMax: %f, MaxRad: %f", this.tilt, this.maxTiltAngle * f, targetTilt);

        // Rotate the wheels based on our speed knowing that
        // the g is a magnitude in blocks
        // and that one wheel measures about 1 diameter
        // So if we move 1 block, then we must have made the
        // wheel rotate 1 diameter
        this.frontWheelRotation = (this.frontWheelRotation + (g)) % 360;
        this.backWheelRotation = this.frontWheelRotation;

        return new Vec3(0.0, 0.0, g);
    }


    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
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
    protected @NotNull AABB makeBoundingBox() {
        Vec3 position = this.position();
        Vec3 boxSize = this.calculateBoxSize(this.getModelSize(), 0, this.getYRot());
        return new AABB(position.x - boxSize.x / 2, position.y, position.z - boxSize.z / 2, position.x + boxSize.x / 2, position.y + boxSize.y, position.z + boxSize.z / 2);
    }

    public abstract Vec3 getModelSize();

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

    @Override
    public void tick() {
        super.tick();
        if (!this.isSaddled() && this.getFirstPassenger() instanceof Player playerEntity) {
            playerEntity.hurt(new DamageSources(this.registryAccess()).sting(this), (this.tilt / this.maxTiltAngle));
        }
    }


    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.getPassengers().isEmpty()) {
            return super.mobInteract(player, hand);
        } else if (player.isSecondaryUseActive()) {
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
}
