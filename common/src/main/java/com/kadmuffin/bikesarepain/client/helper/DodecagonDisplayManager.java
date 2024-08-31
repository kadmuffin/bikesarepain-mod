package com.kadmuffin.bikesarepain.client.helper;

import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.Map;

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

    private static final float[][] TYPE_SCREEN_ROT = {
            // RotTypeScreen, RotUnit

            {0f, 0f}, // 0 -> Distance + meters
            {0f, (float) Math.PI/2}, // 1 -> Distance + km
            {0f, (float) Math.PI}, // 2 -> Distance + ft
            {0f, (float) -Math.PI/2}, // 3 -> Distance + mi

            {(float) Math.PI/2, 0f}, // 4 -> Time + sec
            {(float) Math.PI/2, (float) Math.PI/2}, // 5 -> Time + min
            {(float) Math.PI/2, (float) Math.PI}, // 6 -> Time + hr
            {(float) Math.PI/2, (float) -Math.PI/2}, // 7 -> Time + day

            {(float) Math.PI, (float) Math.PI/2}, // 8 -> Speed + km/h
            {(float) Math.PI, (float) Math.PI}, // 9 -> Speed + m/s
            {(float) Math.PI, (float) -Math.PI/2}, // 10 -> Speed + mph
            {(float) -Math.PI/2, 0f}, // 11 -> Calories + kcal
    };

    public enum DisplaySubType {
        DISTANCE(0),
        TIME(1),
        SPEED(2),
        CALORIES(3);

        private final int type;

        DisplaySubType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static DisplaySubType fromType(int type) {
            for (DisplaySubType displayType : values()) {
                if (displayType.getType() == type) {
                    return displayType;
                }
            }
            return DISTANCE;
        }


        public boolean shouldHide(int index) {
            return switch (this) {
                case DISTANCE -> index != 0;
                case TIME -> index != 1;
                case SPEED, CALORIES -> index != 2;
            };
        }
    }

    public enum DisplayType {
        DISTANCE_METERS(0),
        DISTANCE_KM(1),
        DISTANCE_FT(2),
        DISTANCE_MI(3),
        TIME_SEC(4),
        TIME_MIN(5),
        TIME_HR(6),
        TIME_DAY(7),
        SPEED_MS(8),
        SPEED_KMH(9),
        SPEED_MPH(10),
        CALORIES_KCAL(11);

        private final int type;

        DisplayType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public DisplaySubType getSubType() {
            return switch (this) {
                case DISTANCE_METERS, DISTANCE_KM, DISTANCE_FT, DISTANCE_MI -> DisplaySubType.DISTANCE;
                case TIME_SEC, TIME_MIN, TIME_HR, TIME_DAY -> DisplaySubType.TIME;
                case SPEED_MS, SPEED_KMH, SPEED_MPH -> DisplaySubType.SPEED;
                case CALORIES_KCAL -> DisplaySubType.CALORIES;
            };
        }

        public static DisplayType fromType(int type) {
            for (DisplayType displayType : values()) {
                if (displayType.getType() == type) {
                    return displayType;
                }
            }
            return DISTANCE_METERS;
        }

        public static DisplayType fromSubType(DisplaySubType subType) {
            return switch (subType) {
                case DISTANCE -> DISTANCE_METERS;
                case TIME -> TIME_SEC;
                case SPEED -> SPEED_MS;
                case CALORIES -> CALORIES_KCAL;
            };
        }

    };


    private final float[] currentRotations = new float[MAX_DISPLAYS];
    private final float[] currentUnitRotations = new float[3];
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

    public void updateDisplayLerped(GeoBone bone, DisplayType type, float lerpFactor, Bicycle bicycle) {
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

        if (boneName.startsWith("Unit")) {
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
        // Just use regex instead
        if (boneName.matches("Display[1-6]")) {
            return boneName.charAt(7) - '1';
        }
        return -1;
    }

    private int getUnitIndex(String boneName) {
        return switch (boneName) {
            case "UnitDistance" -> 0;
            case "UnitTime" -> 1;
            case "UnitSpeed" -> 2;
            default -> -1;
        };
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

    // Returns the rotation for the type and unit screen
    private float[] getScreenRotation(DisplayType type) {
        return TYPE_SCREEN_ROT[type.getType()];
    }

    private float getTypeScreenRotation(DisplayType type) {
        return TYPE_SCREEN_ROT[type.getType()][0];
    }

    // Reads the current display type
    public boolean shouldHideUnit(DisplayType type, int unitIndex) {
        return type.getSubType().shouldHide(unitIndex);
    }

    // Checks if the unit is rotating, and if so, wait until we are at 45 degrees
    // of the current unit rotation until hiding
    public boolean shouldHideUnitRot(DisplayType type, int unitIndex, float currentUnitRotation) {
        return shouldHideUnit(type, unitIndex) && Math.abs(currentUnitRotation - TYPE_SCREEN_ROT[type.getType()][1]) < Math.PI / 4;
    }

    // Automatically hides the unit display based on the type and current rotation
    // That is, it won't switch the unit being display unless we are halfway through the target rotation
    // of the new unit. Automatically lerps the unit rotation
    public void updateUnitDisplay(GeoBone bone, DisplayType type, float lerpFactor, Bicycle bicycle) {
        String boneName = bone.getName();
        int unitIndex = getUnitIndex(boneName);

        if (unitIndex == -1) {
            System.out.println("Invalid bone name: " + boneName);
            return;
        }

        boolean hide = shouldHideUnit(type, unitIndex);

        // Check if we should hide the unit
        bone.setHidden(hide);

        if (hide) {
            return;
        }

        float[] targetRotations = getScreenRotation(type);
        float targetRotation = targetRotations[1];
        float currentRotation = currentUnitRotations[unitIndex];

        // Determine the shortest rotation path
        float rotationDiff = targetRotation - currentRotation;
        // If the difference is zero, we don't need to do anything
        if (rotationDiff == 0) {
            return;
        }


        if (rotationDiff > Math.PI) rotationDiff -= (float) (2 * Math.PI);
        else if (rotationDiff < -Math.PI) rotationDiff += (float) (2 * Math.PI);

        // If the change is too big, snap faster
        if (Math.abs(rotationDiff) > Math.PI / 2) {
            currentUnitRotations[unitIndex] = currentRotation + rotationDiff * lerpFactor * 2;
        } else {
            currentUnitRotations[unitIndex] = currentRotation + rotationDiff * lerpFactor;
        }

        // Account for the type screen rotation
        bone.setRotX(currentUnitRotations[unitIndex] - typeScreenRotation);
    }

}