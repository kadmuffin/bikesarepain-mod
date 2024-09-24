package com.kadmuffin.bikesarepain.server.helper;

import org.joml.Vector3d;

public class CenterMass {
    // The current tracked values (in local space, relative to initial center of mass)
    private final Vector3d trackedMassOffset;
    private final Vector3d trackedPlayerOffset;

    // The initial center of mass for the model and player
    private final Vector3d modelCenterOfMass;
    private final Vector3d playerCenterOfMass;

    // Masses
    private double modelMass;
    private double playerMass;

    public CenterMass(Vector3d modelCenterOfMass, Vector3d playerCenterOfMass, double modelMass, double playerMass) {
        this.modelCenterOfMass = new Vector3d(modelCenterOfMass);
        this.playerCenterOfMass = new Vector3d(playerCenterOfMass);
        this.modelMass = modelMass;
        this.playerMass = playerMass;
        this.trackedMassOffset = new Vector3d(0, 0, 0);
        this.trackedPlayerOffset = new Vector3d(0, 0, 0);
    }

    public Vector3d getTrackedMassOffset() {
        return new Vector3d(trackedMassOffset);
    }

    public Vector3d getTrackedPlayerOffset() {
        return new Vector3d(trackedPlayerOffset);
    }

    public void moveModelOffsetBy(Vector3d offset) {
        trackedMassOffset.add(offset);
    }

    public void movePlayerOffsetBy(Vector3d offset) {
        trackedPlayerOffset.add(offset);
    }

    public void setModelOffset(Vector3d offset) {
        trackedMassOffset.set(offset);
    }

    public void setPlayerOffset(Vector3d offset) {
        trackedPlayerOffset.set(offset);
    }

    public void setModelMass(double mass) {
        this.modelMass = mass;
    }

    public void setPlayerMass(double mass) {
        this.playerMass = mass;
    }

    public void reset() {
        trackedMassOffset.set(0, 0, 0);
        trackedPlayerOffset.set(0, 0, 0);
    }

    public Vector3d getCombinedCenterOfMassLocal() {
        Vector3d modelPosition = new Vector3d(modelCenterOfMass);
        modelPosition.add(trackedMassOffset);

        Vector3d playerPosition = new Vector3d(playerCenterOfMass);
        playerPosition.add(trackedPlayerOffset);

        Vector3d combinedCenterOfMass = new Vector3d();

        // Scale model position by model mass
        modelPosition.mul(modelMass);

        // Scale player position by player mass
        playerPosition.mul(playerMass);

        // Add scaled positions
        combinedCenterOfMass.add(modelPosition);
        combinedCenterOfMass.add(playerPosition);

        // Divide by total mass
        double totalMass = modelMass + playerMass;
        combinedCenterOfMass.mul(1.0 / totalMass);

        return combinedCenterOfMass;
    }

    public Vector3d getCombinedCenterOfMassWorld(Vector3d worldPosition) {
        Vector3d localCenterOfMass = getCombinedCenterOfMassLocal();
        Vector3d worldCenterOfMass = new Vector3d(localCenterOfMass);
        worldCenterOfMass.add(worldPosition);

        return worldCenterOfMass;
    }

    public double calculateAngleFromVertical() {
        Vector3d combinedCenterOfMass = getCombinedCenterOfMassLocal();
        Vector3d directionToCOM = new Vector3d(combinedCenterOfMass);
        directionToCOM.sub(modelCenterOfMass);

        Vector3d upVector = new Vector3d(0, 1, 0);
        return Math.acos(directionToCOM.dot(upVector) / (directionToCOM.length() * upVector.length()));
    }

    public double calculateRollAngle() {
        Vector3d combinedCenterOfMass = getCombinedCenterOfMassLocal();
        Vector3d directionToCOM = new Vector3d(combinedCenterOfMass);
        directionToCOM.sub(modelCenterOfMass);

        Vector3d projectedDirection = new Vector3d(directionToCOM.x, 0, directionToCOM.z);

        double angle = Math.atan2(projectedDirection.z, projectedDirection.x);

        if (directionToCOM.y < 0) {
            angle = -angle;
        }

        return angle;
    }

    public double calculatePitchAngle() {
        Vector3d combinedCenterOfMass = getCombinedCenterOfMassLocal();
        Vector3d directionToCOM = new Vector3d(combinedCenterOfMass);
        directionToCOM.sub(modelCenterOfMass);

        Vector3d projectedDirection = new Vector3d(0, directionToCOM.y, directionToCOM.z);

        double angle = Math.atan2(projectedDirection.z, projectedDirection.y);

        if (directionToCOM.x < 0) {
            angle = -angle;
        }

        return angle;
    }

    public double calculateYawAngle() {
        Vector3d combinedCenterOfMass = getCombinedCenterOfMassLocal();
        Vector3d directionToCOM = new Vector3d(combinedCenterOfMass);
        directionToCOM.sub(modelCenterOfMass);

        Vector3d projectedDirection = new Vector3d(directionToCOM.x, directionToCOM.y, 0);

        double angle = Math.atan2(projectedDirection.y, projectedDirection.x);

        if (directionToCOM.z < 0) {
            angle = -angle;
        }

        return angle;
    }
}