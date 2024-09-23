package com.kadmuffin.bikesarepain.neoforge;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.BikesArePainClient;
import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.kadmuffin.bikesarepain.client.SerialReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

@Mod(MOD_ID)
public final class BikesArePainNeoForge {
    private SerialReader reader;

    private RequiredArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack, Float> setScale(ClientConfig.ApplyScaleTo applyTo) {
        return ClientCommandRegistrationEvent.argument("scale1", FloatArgumentType.floatArg(
                        0.01F,
                        50F
                ))
                .then(ClientCommandRegistrationEvent.literal("block")
                        .then(ClientCommandRegistrationEvent.literal("is")
                                .then(ClientCommandRegistrationEvent.argument("scale2", FloatArgumentType.floatArg(
                                                        0.01F,
                                                        50F
                                                ))
                                                .then(ClientCommandRegistrationEvent.literal("meter")
                                                        .executes(context -> {
                                                            float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                                            float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                                            ClientConfig.CONFIG.instance().setScale(scale1, scale2, applyTo);
                                                            context.getSource().arch$sendSuccess(() -> Component.literal("Set scale factor"), false);
                                                            return 1;
                                                        })
                                                )
                                )
                        )
                )

                .then(ClientCommandRegistrationEvent.literal("meter")
                        .then(ClientCommandRegistrationEvent.argument("scale2", FloatArgumentType.floatArg(
                                            0.01F,
                                            50F
                                        ))
                                        .executes(context -> {
                                            float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                            float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                            ClientConfig.CONFIG.instance().setScale(scale1, scale2, applyTo);
                                            context.getSource().arch$sendSuccess(() -> Component.literal("Set scale factor"), false);
                                            return 1;
                                        })
                        )
                );
    }

    public BikesArePainNeoForge() {
        // Run our common setup.
        BikesArePain.init();

        // Make sure we are running in the client side
        if (Minecraft.getInstance().level == null) {
            BikesArePainClient.init();

            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (client, parent) -> ClientConfig.CONFIG.instance().getScreen(parent));

            ClientCommandRegistrationEvent.EVENT.register((dispatcher, dedicated) -> {
                LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> command = ClientCommandRegistrationEvent.literal("bikes")
                        .then(ClientCommandRegistrationEvent.literal("open")
                                .executes(context -> {
                                    try {
                                        if (ClientConfig.CONFIG.instance().getPort().contains("No port")) {
                                            context.getSource().arch$sendFailure(Component.literal("No port set yet."));
                                            return 0;
                                        }

                                        // Check if the port is available
                                        if (BikesArePainClient.isConfigPortUnavailable()) {
                                            context.getSource().arch$sendFailure(Component.literal("The chosen port is not available. Please choose another port or try again."));
                                            return 0;
                                        }

                                        BikesArePainClient.getReader().setSerial();
                                        BikesArePainClient.getReader().start();
                                    } catch (Exception e) {
                                        System.out.println("Failed to open port: " + e);
                                        context.getSource().arch$sendFailure(Component.literal("Failed to open port: " + e));
                                        return 0;
                                    }

                                    context.getSource().arch$sendSuccess(() -> Component.literal("Opened port"), false);
                                    return 1;
                                })
                                .then(ClientCommandRegistrationEvent.argument("port", StringArgumentType.string())
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
                                                    context.getSource().arch$sendFailure(Component.literal("The chosen port is not available. Please choose another port or try again."));
                                                    return 0;
                                                }

                                                BikesArePainClient.getReader().setSerial();
                                                BikesArePainClient.getReader().start();
                                                ClientConfig.CONFIG.save();
                                            } catch (Exception e) {
                                                System.out.println("Failed to open port: " + e);
                                                context.getSource().arch$sendFailure(Component.literal("Failed to open port: " + e));
                                                return 0;
                                            }

                                            context.getSource().arch$sendSuccess(() -> Component.literal("Opened port"), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandRegistrationEvent.literal("clear")
                                .executes(context -> {
                                    BikesArePainClient.getProcessor().reset();
                                    context.getSource().arch$sendSuccess(() -> Component.literal("Cleared data"), false);
                                    return 1;
                                })
                        )
                        .then(ClientCommandRegistrationEvent.literal("close")
                                .executes(context -> {
                                    try {
                                        BikesArePainClient.getReader().stop();
                                    } catch (Exception e) {
                                        System.out.println("Failed to close port: " + e);
                                        context.getSource().arch$sendFailure(Component.literal("Failed to close port: " + e));
                                        return 0;
                                    }

                                    context.getSource().arch$sendSuccess(() -> Component.literal("Closed port"), false);
                                    return 1;
                                })
                        )
                                .then(ClientCommandRegistrationEvent.literal("scale")
                                        .then(ClientCommandRegistrationEvent.literal("set")
                                                .then(
                                                        ClientCommandRegistrationEvent.literal("all").then(
                                                                setScale(ClientConfig.ApplyScaleTo.BOTH)
                                                        )
                                                )
                                                .then(
                                                        ClientCommandRegistrationEvent.literal("wheel").then(
                                                                setScale(ClientConfig.ApplyScaleTo.WHEEL)
                                                        )
                                                )
                                                .then(
                                                        ClientCommandRegistrationEvent.literal("speed").then(
                                                                setScale(ClientConfig.ApplyScaleTo.SPEED)
                                                        )
                                                )
                                        ).then(
                                                ClientCommandRegistrationEvent.literal("get")
                                                        .then(ClientCommandRegistrationEvent.literal("scale")
                                                                .executes(context -> {
                                                                    context.getSource().arch$sendSuccess(() -> Component.literal(ClientConfig.CONFIG.instance().getScaleRatiosString()), false);
                                                                    return 1;
                                                                })
                                                        )
                                        )
                                )
                                        .then(ClientCommandRegistrationEvent.literal("unit")
                                                .then(ClientCommandRegistrationEvent.literal("imperial")
                                                        .executes(context -> {
                                                            ClientConfig.CONFIG.instance().setImperial(true);
                                                            ClientConfig.CONFIG.save();
                                                            context.getSource().arch$sendSuccess(() -> Component.literal("Set unit system to imperial"), false);
                                                            return 1;
                                                        })
                                                )
                                                .then(ClientCommandRegistrationEvent.literal("metric")
                                                        .executes(context -> {
                                                            ClientConfig.CONFIG.instance().setImperial(false);
                                                            ClientConfig.CONFIG.save();
                                                            context.getSource().arch$sendSuccess(() -> Component.literal("Set unit system to metric"), false);
                                                            return 1;
                                                        })
                                                )
                                        );

                dispatcher.register(command);
            });
        }
    }


}
