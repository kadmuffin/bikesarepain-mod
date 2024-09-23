package com.kadmuffin.bikesarepain.fabric.client;

import com.kadmuffin.bikesarepain.BikesArePainClient;
import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.kadmuffin.bikesarepain.client.SerialReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class BikesArePainFabricClient implements ClientModInitializer {
    private RequiredArgumentBuilder<FabricClientCommandSource, ?> scaleSet(int applyTo) {
        return ClientCommandManager.argument("scale1", FloatArgumentType.floatArg(
                0.01F,
                50F
        )).then(
                ClientCommandManager.literal("block").then(
                        ClientCommandManager.literal("is").then(
                                ClientCommandManager.argument("scale2", FloatArgumentType.floatArg(
                                        0.01F,
                                        50F
                                )).then(
                                        ClientCommandManager.literal("meter").executes(context -> {
                                            float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                            float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                            BikesArePainClient.getReader().setScaleFactor(scale1, scale2, applyTo);
                                            context.getSource().sendFeedback(Component.literal("Set scale factor"));
                                            return 1;
                                        })
                                )
                        )
                )).then(ClientCommandManager.literal("meter")
                .then(ClientCommandManager.literal("is")
                        .then(ClientCommandManager.argument("scale2", FloatArgumentType.floatArg(
                                0.01F,
                                50F
                        )).then(
                                ClientCommandManager.literal("block").executes(context -> {
                                    float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                    float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                    BikesArePainClient.getReader().setScaleFactor(scale2, scale1, applyTo);
                                    context.getSource().sendFeedback(Component.literal("Set scale factor"));
                                    return 1;
                                })
                        ))
                )
        );
    }

    @Override
    public void onInitializeClient() {
        BikesArePainClient.init();

        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("bikes").then(
                ClientCommandManager.literal("open")
                        .executes(context -> {
                            try {
                                if (ClientConfig.CONFIG.instance().getPort().contains("No port")) {
                                    context.getSource().sendFeedback(Component.literal("No port set yet."));
                                    return 0;
                                }

                                // Check if the port is available
                                if (BikesArePainClient.isConfigPortUnavailable()) {
                                    context.getSource().sendFeedback(Component.literal("The chosen port is not available. Please choose another port or try again."));
                                    return 0;
                                }

                                BikesArePainClient.getReader().setSerial();
                                BikesArePainClient.getReader().start();
                            } catch (Exception e) {
                                System.out.println("Failed to open port: " + e);
                                context.getSource().sendFeedback(Component.literal("Failed to open port: " + e));
                                return 0;
                            }

                            context.getSource().sendFeedback(Component.literal("Opened port"));
                            return 1;
                        })
                        .then(
                        ClientCommandManager.argument("port", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (String port : SerialReader.getPorts()) {
                                        builder.suggest(port);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String port = StringArgumentType.getString(context, "port");
                                    try {
                                        ClientConfig.CONFIG.instance().setPort(port);

                                        // Check if the port is available
                                        if (BikesArePainClient.isConfigPortUnavailable()) {
                                            context.getSource().sendFeedback(Component.literal("The chosen port is not available. Please choose another port or try again."));
                                            return 0;
                                        }

                                        BikesArePainClient.getReader().setSerial();
                                        BikesArePainClient.getReader().start();
                                        ClientConfig.CONFIG.save();
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
                        BikesArePainClient.getReader().stop();
                    } catch (Exception e) {
                        System.out.println("Failed to close port: " + e);
                        context.getSource().sendFeedback(Component.literal("Failed to close port: " + e));
                        return 0;
                    }

                    context.getSource().sendFeedback(Component.literal("Closed port"));
                    return 1;
                })
        ).then(ClientCommandManager.literal("clear").executes(context -> {
                    BikesArePainClient.getReader().resetDistance();
                    BikesArePainClient.getReader().resetCalories();
                    context.getSource().sendFeedback(Component.literal("Cleared data"));
                    return 1;
                }))
                .then(
                ClientCommandManager.literal("scale").then(
                        ClientCommandManager.literal("set").then(
                                ClientCommandManager.literal("all").then(scaleSet(0))
                        )
                                .then(
                                        ClientCommandManager.literal("wheel").then(
                                                scaleSet(2)
                                        )
                                )
                                .then(
                                        ClientCommandManager.literal("speed").then(
                                                scaleSet(1)
                                        )
                                )
                ).then(ClientCommandManager.literal("get")
                        .executes(context -> {
                            context.getSource().sendFeedback(Component.literal("Scale factor: " + BikesArePainClient.getReader().getScaleFactorString()));
                            return 1;
                        })
                )
        ).then(
                ClientCommandManager.literal("unit").then(
                        ClientCommandManager.literal("imperial").executes(context -> {
                            ClientConfig.CONFIG.instance().setImperial(true);
                            ClientConfig.CONFIG.save();
                            context.getSource().sendFeedback(Component.literal("Set to imperial"));
                            return 1;
                        })
                )
                        .then(
                                ClientCommandManager.literal("metric").executes(context -> {
                                    ClientConfig.CONFIG.instance().setImperial(false);
                                    ClientConfig.CONFIG.save();
                                    context.getSource().sendFeedback(Component.literal("Set to metric"));
                                    return 1;
                                })
                        )
        )));

    }
}
