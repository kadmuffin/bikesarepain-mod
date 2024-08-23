package com.kadmuffin.bikesarepain.fabric.client;

import com.kadmuffin.bikesarepain.client.SerialReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public final class BikesArePainFabricClient implements ClientModInitializer {
    private SerialReader reader = new SerialReader();

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientCommandRegistrationCallback.EVENT.register( (dispatcher, registryAccess) -> {
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
                    )
            );
        });
    }
}
