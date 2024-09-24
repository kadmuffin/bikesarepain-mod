package com.kadmuffin.bikesarepain.server;

import net.minecraft.world.level.GameRules;

public class GameRuleManager {
    public static final int MAX_BIKE_WHEEL_VAL = 2;
    public static final int MIN_BIKE_WHEEL_VAL = 1;
    public static final int MAX_BIKE_SPEED_BLOCKS_PER_SECOND = 10;

    // For wheel size
    public static final GameRules.Key<GameRules.IntegerValue> MAX_BIKE_WHEEL_SIZE = GameRules.register("maxWheelBlockSize", GameRules.Category.PLAYER, GameRules.IntegerValue.create(MAX_BIKE_WHEEL_VAL));
    public static final GameRules.Key<GameRules.IntegerValue> MIN_BIKE_WHEEL_SIZE = GameRules.register("minWheelBlockSizeDecimals100", GameRules.Category.PLAYER, GameRules.IntegerValue.create(MIN_BIKE_WHEEL_VAL));

    // For bike speed
    public static final GameRules.Key<GameRules.IntegerValue> MAX_BIKE_SPEED = GameRules.register("maxBikeSpeedBlocksPerSecond", GameRules.Category.PLAYER, GameRules.IntegerValue.create(MAX_BIKE_SPEED_BLOCKS_PER_SECOND));

    public static void init() {
    }
}
