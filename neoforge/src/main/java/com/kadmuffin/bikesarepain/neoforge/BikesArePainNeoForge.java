package com.kadmuffin.bikesarepain.neoforge;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.SerialReader;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.kadmuffin.bikesarepain.server.GameRuleManager;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

@Mod(MOD_ID)
public final class BikesArePainNeoForge {
    private SerialReader reader;

    private RequiredArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack, Float> setScale(int applyTo) {
        return ClientCommandRegistrationEvent.argument("scale1", FloatArgumentType.floatArg(
                        GameRuleManager.MIN_BIKE_SCALING_VAL / 10F,
                        GameRuleManager.MAX_BIKE_SCALING_VAL
                ))
                .then(ClientCommandRegistrationEvent.literal("block")
                        .then(ClientCommandRegistrationEvent.literal("is")
                                .then(ClientCommandRegistrationEvent.argument("scale2", FloatArgumentType.floatArg(
                                                        GameRuleManager.MIN_BIKE_SCALING_VAL / 10F,
                                                        GameRuleManager.MAX_BIKE_SCALING_VAL
                                                ))
                                                .then(ClientCommandRegistrationEvent.literal("meter")
                                                        .executes(context -> {
                                                            float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                                            float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                                            reader.setScaleFactor(scale1, scale2, applyTo);
                                                            context.getSource().arch$sendSuccess(() -> Component.literal("Set scale factor"), false);
                                                            return 1;
                                                        })
                                                )
                                )
                        )
                )

                .then(ClientCommandRegistrationEvent.literal("meter")
                        .then(ClientCommandRegistrationEvent.argument("scale2", FloatArgumentType.floatArg(
                                                GameRuleManager.MIN_BIKE_SCALING_VAL / 10F,
                                                GameRuleManager.MAX_BIKE_SCALING_VAL
                                        ))
                                        .executes(context -> {
                                            float scale1 = FloatArgumentType.getFloat(context, "scale1");
                                            float scale2 = FloatArgumentType.getFloat(context, "scale2");
                                            reader.setScaleFactor(scale1, scale2, applyTo);
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
            reader = new SerialReader();

            ClientCommandRegistrationEvent.EVENT.register((dispatcher, dedicated) -> {
                LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack> command = ClientCommandRegistrationEvent.literal("bikes")
                        .then(ClientCommandRegistrationEvent.literal("open")
                                .then(ClientCommandRegistrationEvent.argument("port", StringArgumentType.string())
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
                                                context.getSource().arch$sendFailure(Component.literal("Failed to open port: " + e));
                                                return 0;
                                            }

                                            context.getSource().arch$sendSuccess(() -> Component.literal("Opened port"), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandRegistrationEvent.literal("close")
                                .executes(context -> {
                                    try {
                                        reader.stop();
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
                                                                setScale(0)
                                                        )
                                                )
                                                .then(
                                                        ClientCommandRegistrationEvent.literal("wheel").then(
                                                                setScale(2)
                                                        )
                                                )
                                                .then(
                                                        ClientCommandRegistrationEvent.literal("speed").then(
                                                                setScale(1)
                                                        )
                                                )
                                        ).then(
                                                ClientCommandRegistrationEvent.literal("get")
                                                        .then(ClientCommandRegistrationEvent.literal("scale")
                                                                .executes(context -> {
                                                                    context.getSource().arch$sendSuccess(() -> Component.literal(reader.getScaleFactorString()), false);
                                                                    return 1;
                                                                })
                                                        )
                                        )
                                )
                                        .then(ClientCommandRegistrationEvent.literal("unit")
                                                .then(ClientCommandRegistrationEvent.literal("imperial")
                                                        .executes(context -> {
                                                            NetworkManager.sendToServer(new PacketManager.UnitSystemPacket(true));
                                                            context.getSource().arch$sendSuccess(() -> Component.literal("Set unit system to imperial"), false);
                                                            return 1;
                                                        })
                                                )
                                                .then(ClientCommandRegistrationEvent.literal("metric")
                                                        .executes(context -> {
                                                            NetworkManager.sendToServer(new PacketManager.UnitSystemPacket(false));
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
