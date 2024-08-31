package com.kadmuffin.bikesarepain.client.helper;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import software.bernie.geckolib.cache.object.GeoBone;

public class DodecagonDisplayManager {
    private static final int MAX_DISPLAYS = 6;
    private static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
    private static final float[] ROTATION_ANGLES = {
            0f,    // Nothing
            (float) Math.toRadians(-30f),  // .
            (float) Math.toRadians(-60f),  // 0
            (float) Math.toRadians(30f),   // 9
            (float) Math.toRadians(60f),   // 8
            (float) Math.toRadians(90f),   // 7
            (float) Math.toRadians(120f),  // 6
            (float) Math.toRadians(150f),  // 5
            (float) Math.toRadians(180f),  // 4
            (float) Math.toRadians(210f),  // 3
            (float) Math.toRadians(240f),  // 2
            (float) Math.toRadians(270f)   // 1
    };
    private static final float[] TYPE_SCREEN_ROT = {
            0f, // Distance + meters
            (float) Math.toRadians(90f), // Time + min
            (float) Math.toRadians(180f), // Speed + Km/h
            (float) Math.toRadians(-90f) // Calories + kcal
    };

    private final float[] currentRotations = new float[MAX_DISPLAYS];
    private float typeScreenRotation = 0;

    public void preprocessTarget(int target, Bicycle bicycle) {
        if (target < 0 || target > 999999) {
            System.out.println("Invalid target number: " + target);
            return;
        }

        if (target != bicycle.getCachedTarget()) {
            bicycle.setCachedTarget(target);
            bicycle.setDigitCount(target == 0 ? 1 : (int)(Math.log10(target) + 1));
            updateCachedDigits(bicycle);
        }
    }

    public void updateDisplay(GeoBone bone, int type, Bicycle bicycle) {
        if (bicycle.getCachedTarget() == -1) {
            System.out.println("No target number has been preprocessed.");
            return;
        }

        String boneName = bone.getName();
        if (boneName.equals("TypeScreen")) {
            bone.setRotX(getTypeScreenRotation(type));
            return;
        }

        int displayIndex = getDisplayIndex(boneName);

        if (displayIndex == -1) {
            System.out.println("Invalid bone name: " + boneName);
            return;
        }

        int digit = (displayIndex < bicycle.getDigitCount()) ? bicycle.getCachedIntDisplay(displayIndex) : -1;
        float rotationAngle = getRotationAngle(digit);
        bone.setRotX(rotationAngle);
        currentRotations[displayIndex] = rotationAngle;
    }

    public void updateDisplayLerped(GeoBone bone, int type, float lerpFactor, Bicycle bicycle) {
        if (bicycle.getCachedTarget() == -1) {
            System.out.println("No target number has been preprocessed.");
            return;
        }

        String boneName = bone.getName();
        // Lerped rotation for the type screen
        if (boneName.equals("TypeScreen")) {
            float newRotation = getTypeScreenRotation(type);
            typeScreenRotation = typeScreenRotation + (newRotation - typeScreenRotation) * lerpFactor;
            bone.setRotX(typeScreenRotation);
            return;
        }


        int displayIndex = getDisplayIndex(boneName);

        if (displayIndex == -1) {
            System.out.println("Invalid bone name: " + boneName);
            return;
        }

        float newRotation = getNewRotation(lerpFactor, displayIndex, bicycle);
        bone.setRotX(newRotation);
        currentRotations[displayIndex] = newRotation;
    }

    private float getNewRotation(float lerpFactor, int displayIndex, Bicycle bicycle) {
        int digit = (displayIndex < bicycle.getDigitCount()) ? bicycle.getCachedIntDisplay(displayIndex) : -1;
        float targetRotation = getRotationAngle(digit);
        float currentRotation = currentRotations[displayIndex];

        // Determine the shortest rotation path
        float rotationDiff = targetRotation - currentRotation;
        if (rotationDiff > Math.PI) rotationDiff -= (float) (2 * Math.PI);
        else if (rotationDiff < -Math.PI) rotationDiff += (float) (2 * Math.PI);

        // If the change is too big, snap faster
        if (Math.abs(rotationDiff) > Math.PI / 2) {
            return currentRotation + rotationDiff * lerpFactor * 2;
        }

        return currentRotation + rotationDiff * lerpFactor;
    }

    private void updateCachedDigits(Bicycle bicycle) {
        int remainingTarget = bicycle.getCachedTarget();
        for (int i = 0; i < bicycle.getDigitCount(); i++) {
            bicycle.setCachedIntDisplay(i, remainingTarget % 10);
            remainingTarget /= 10;
        }
        for (int i = bicycle.getDigitCount(); i < MAX_DISPLAYS; i++) {
            bicycle.setCachedIntDisplay(i, -1);
        }
    }

    private int getDisplayIndex(String boneName) {
        /*if (boneName.startsWith("Display") && boneName.length() == 8) {
            char indexChar = boneName.charAt(7);
            if (indexChar >= '1' && indexChar <= '6') {
                return indexChar - '1';
            }
        }*/
        // Just use regex instead
        if (boneName.matches("Display[1-6]")) {
            return boneName.charAt(7) - '1';
        }
        return -1;
    }

    private float getRotationAngle(int digit) {
        if (digit < 0 || digit > 9) {
            return ROTATION_ANGLES[0]; // Nothing
        }

        return switch (digit) {
            case 0 -> ROTATION_ANGLES[2];
            case 1 -> ROTATION_ANGLES[11];
            case 2 -> ROTATION_ANGLES[10];
            case 3 -> ROTATION_ANGLES[9];
            case 4 -> ROTATION_ANGLES[8];
            case 5 -> ROTATION_ANGLES[7];
            case 6 -> ROTATION_ANGLES[6];
            case 7 -> ROTATION_ANGLES[5];
            case 8 -> ROTATION_ANGLES[4];
            case 9 -> ROTATION_ANGLES[3];
            default -> ROTATION_ANGLES[0];
        };
    }

    private float getTypeScreenRotation(int type) {
        if (type < 0 || type > 3) {
            return TYPE_SCREEN_ROT[0]; // Distance + meters
        }
        return TYPE_SCREEN_ROT[type];
    }
}