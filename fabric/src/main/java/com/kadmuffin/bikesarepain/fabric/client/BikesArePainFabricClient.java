package com.kadmuffin.bikesarepain.fabric.client;

import com.kadmuffin.bikesarepain.client.SerialReader;
import com.kadmuffin.bikesarepain.server.GameRuleManager;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public final class BikesArePainFabricClient implements ClientModInitializer {
    private final SerialReader reader = new SerialReader();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("bikes").then(
                    ClientCommandManager.literal("open").then(
                            ClientCommandManager.argument("port", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        for (String port : reader.getPorts()) {
                                            builder.suggest(port);
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        String port = StringArgumentType.getString(context, "port");
                                        try {
                                            reader.setSerial(port);
                                            reader.start();
                                        } catch (Exception e) {
                                            System.out.println("Failed to open port: " + e);
                                            context.getSource().sendFeedback(Component.literal("Failed to open port: " + e));
                                            return 0;
                                        }

                                        context.getSource().sendFeedback(Component.literal("Opened port"));
                                        return 1;
                                    })
                    )).then(
                    ClientCommandManager.literal("close").executes(context -> {
                        try {
                            reader.stop();
                        } catch (Exception e) {
                            System.out.println("Failed to close port: " + e);
                            context.getSource().sendFeedback(Component.literal("Failed to close port: " + e));
                            return 0;
                        }

                        context.getSource().sendFeedback(Component.literal("Closed port"));
                        return 1;
                    })
            ).then(
                    ClientCommandManager.literal("scale").then(
                            ClientCommandManager.literal("set").then(
                                    ClientCommandManager.argument("scale1", FloatArgumentType.floatArg(
                                            GameRuleManager.MIN_BIKE_SCALING_VAL / 10F,
                                            GameRuleManager.MAX_BIKE_SCALING_VAL
                                    )).then(
                                            ClientCommandManager.literal("block").then(
                                                    ClientCommandManager.literal("is").then(
                                                            ClientCommandManager.argument("scale2", FloatArgumentType.floatArg(
                                                                    GameRuleManager.MIN_BIKE_SCALING_VAL / 10F,
                                                                    GameRuleManager.MAX_BIKE_SCALING_VAL
                                                            )).then(
                                                                    ClientCommandManager.literal("meter").executes(context -> {
                                                                        float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                                                        float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                                                        reader.setScaleFactor(scale1, scale2);
                                                                        context.getSource().sendFeedback(Component.literal("Set scale factor"));
                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                            )).then(ClientCommandManager.literal("meter")
                                                    .then(ClientCommandManager.literal("is")
                                                            .then(ClientCommandManager.argument("scale2", FloatArgumentType.floatArg(
                                                                    GameRuleManager.MIN_BIKE_SCALING_VAL / 10F,
                                                                    GameRuleManager.MAX_BIKE_SCALING_VAL
                                                            )).then(
                                                                    ClientCommandManager.literal("block").executes(context -> {
                                                                        float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                                                        float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                                                        reader.setScaleFactor(scale2, scale1);
                                                                        context.getSource().sendFeedback(Component.literal("Set scale factor"));
                                                                        return 1;
                                                                    })
                                                            ))
                                                    )
                                            )

                            )
                    ).then(ClientCommandManager.literal("get")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("Scale factor: " + reader.getScaleFactorString()));
                                return 1;
                            })
                    )
            ));
        });
    }
}
