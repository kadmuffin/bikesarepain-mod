package com.kadmuffin.bikesarepain;

import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.kadmuffin.bikesarepain.client.SerialReader;
import com.kadmuffin.bikesarepain.client.item.TooltipManager;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.network.chat.Component;

public class BikesArePainClient {
    private static SerialReader reader;

    public static void init() {
        reader = new SerialReader();
        ClientConfig.init();
        TooltipManager.init();

        PlayerEvent.PLAYER_JOIN.register((player) -> {
            // Check if the port in ClientConfig.CONFIG.instance().getPort()
            // is in the list SerialReader.getPorts()
            if (ClientConfig.CONFIG.instance().isAutoConnect() && !ClientConfig.CONFIG.instance().getPort().contains("No port")) {
                if (SerialReader.getPorts().contains(ClientConfig.CONFIG.instance().getPort())) {
                    reader.stop();
                    try {
                        reader.setSerial();
                        if (reader.start())  {
                            player.displayClientMessage(
                                    Component.translatable("bikesarepain.jserialcomm.connected_to_port"),
                                    false
                            );
                        }
                    } catch (Exception e) {
                        player.displayClientMessage(
                                Component.literal("Bikes Are Pain: Something went wrong while trying to connect to the serial port. Error: " + e),
                                false
                        );
                    }
                } else if (ClientConfig.CONFIG.instance().isShowPortNotAvailableMessage()) {
                    player.displayClientMessage(
                            Component.translatable("bikesarepain.jserialcomm.port_not_available"),
                            false
                    );
                }
            }
        });

        PlayerEvent.PLAYER_QUIT.register((player) -> {
            reader.stop();
        });
    }

    public static boolean isConfigPortUnavailable() {
        return !SerialReader.getPorts().contains(ClientConfig.CONFIG.instance().getPort());
    }

    public static SerialReader getReader() {
        return reader;
    }
}
