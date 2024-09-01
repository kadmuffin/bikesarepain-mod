package com.kadmuffin.bikesarepain.server;

import net.minecraft.world.level.GameRules;

public class GameRuleManager {
    public static final int MAX_BIKE_SCALING_VAL = 15;
    public static final int MIN_BIKE_SCALING_VAL = 1;

    public static final GameRules.Key<GameRules.IntegerValue> MAX_BIKE_SCALING = GameRules.register("maxBikeScaling", GameRules.Category.PLAYER, GameRules.IntegerValue.create(MAX_BIKE_SCALING_VAL));
    public static final GameRules.Key<GameRules.IntegerValue> MIN_BIKE_SCALING = GameRules.register("minBikeDecimalsAtZero", GameRules.Category.PLAYER, GameRules.IntegerValue.create(MIN_BIKE_SCALING_VAL));
    public static void init() {}
}
