package com.kadmuffin.bikesarepain;

import com.kadmuffin.bikesarepain.client.ClientConfig;
import com.kadmuffin.bikesarepain.client.SerialReader;
import com.kadmuffin.bikesarepain.client.item.TooltipManager;
import com.kadmuffin.bikesarepain.client.serial.DataProcessor;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BikesArePainClient {
    private static final SerialReader reader = new SerialReader();
    private static final DataProcessor processor = new DataProcessor();

    public static void init() {
        ClientConfig.init();
        TooltipManager.init();

        reader.addListener((speed, triggerTimeHours, wheelRadius) -> {
            if (wheelRadius > 0F) {
                processor.setWheelRadius(wheelRadius);
            }
            processor.update(speed, triggerTimeHours);
        });

        processor.addChangeListener(dataPoint -> {
            float scaleAllRatio = getScaleAllRatio(processor.getWheelRadius());

            float inGameSpeed = applyAllScaleIfNeeded((float) dataPoint.speed, scaleAllRatio);
            float visibleSpeed = (float) (dataPoint.speed * scaleAllRatio);

            // Here, we could technically just use the value from "getTargetWheelSize"
            // if "useMappedForCalculations" is on, but for readability, use this.
            float inGameWheel = applyAllScaleIfNeeded(processor.getWheelRadius(), scaleAllRatio);
            float distanceMoved = (float) processor.getTotalDistance() * scaleAllRatio;
            float caloriesBurned = (float) processor.getTotalCalories() * scaleAllRatio;

            NetworkManager.sendToServer(
                    new PacketManager.ArduinoData(
                            true,
                            inGameSpeed,
                            visibleSpeed,
                            distanceMoved,
                            caloriesBurned,
                            inGameWheel,
                            ClientConfig.CONFIG.instance().getWheelScaleRatio(),
                            ClientConfig.CONFIG.instance().getSpeedScaleRatio()
                    )
            );
        });

        processor.addNothingChangedListener(dataPoint -> {
            NetworkManager.sendToServer(
                    new PacketManager.EmptyArduinoData(
                            true, false
                    )
            );
        });

        reader.addEventListener((event -> {
            switch (event) {
                case SUDDEN_DISCONNECT:
                    processor.reset();
                    NetworkManager.sendToServer(
                            new PacketManager.EmptyArduinoData(
                                    false, true
                            )
                    );
                    break;
                case START:
                    processor.reset();
                    NetworkManager.sendToServer(
                            new PacketManager.EmptyArduinoData(
                                    true, false
                            )
                    );
                    break;
                case STOP:
                    processor.reset();
                    NetworkManager.sendToServer(
                            new PacketManager.EmptyArduinoData(
                                    false, false
                            )
                    );
                    break;
            }
        }));

        PlayerEvent.PLAYER_JOIN.register((player) -> {
            // Check if the port in ClientConfig.CONFIG.instance().getPort()
            // is in the list SerialReader.getPorts()
            if (ClientConfig.CONFIG.instance().isAutoConnect() && !ClientConfig.CONFIG.instance().getPort().contains("No port")) {
                if (SerialReader.getPorts().contains(ClientConfig.CONFIG.instance().getPort())) {
                    reader.stop();
                    try {
                        reader.setSerial();
                        if (reader.start()) {
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

        PlayerEvent.PLAYER_QUIT.register((player) -> reader.stop());
    }

    public static boolean isConfigPortUnavailable() {
        return !SerialReader.getPorts().contains(ClientConfig.CONFIG.instance().getPort());
    }

    public static SerialReader getReader() {
        return reader;
    }

    public static DataProcessor getProcessor() {
        return processor;
    }

    private static float getScaleAllRatio(float wheelRadius) {
        float scaleRatio = 1F;

        if (ClientConfig.CONFIG.instance().wantsValuesScaled()) {
            // Let say the fitness bike wheel measures 0.27 meters in diameter
            // but a real bike wheel measures 0.7 meters in diameter
            float targetSize = ClientConfig.CONFIG.instance().getTargetWheelSize();
            scaleRatio = targetSize / wheelRadius;
        }

        return scaleRatio;
    }

    private static float applyAllScaleIfNeeded(float value, float scaleRatio) {
        if (ClientConfig.CONFIG.instance().useMappedForCalculations()) {
            return value * scaleRatio;
        }

        return value;
    }

}
