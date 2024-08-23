package com.kadmuffin.bikesarepain.neoforge;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.SerialReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.level.entity.forge.EntityRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

@Mod(MOD_ID)
public final class BikesArePainNeoForge {
    private SerialReader reader;

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
                        );

                dispatcher.register(command);
            });
        }
    }


}
