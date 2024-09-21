package com.kadmuffin.bikesarepain.client;

import com.kadmuffin.bikesarepain.BikesArePain;
import dev.architectury.platform.Platform;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ClientConfig {
    public static final ConfigClassHandler<ClientConfig> CONFIG = ConfigClassHandler.createBuilder(ClientConfig.class)
            .id(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(Platform.getConfigFolder().resolve(String.format("%s.json5", BikesArePain.MOD_ID)))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Display Imperial units instead of metric.")
    private boolean imperial = false;

    @SerialEntry(comment = "Whether to automatically connect to the serial port on startup.")
    private boolean autoConnect = false;
    @SerialEntry(comment = "Display a message when the port chosen for auto connect is not available.")
    private boolean showPortNotAvailableMessage = true;
    @SerialEntry(comment = "The port to connect to.")
    private String port = "COM7";
    @SerialEntry(comment = "The baud rate to connect with.")
    private int baudRate = 31250;

    @SerialEntry(comment = "How much to scale the speed by. Calculated with 1 / x meters needed to do a full block. ")
    private float speedScaleRatio = 2F;
    @SerialEntry(comment = "How much to scale the wheel size read by JSerialComm. You can calculate it in the same way as the speedRatio.")
    private float wheelScaleRatio = 2F;

    public Screen getScreen(Screen parent) {
        return YetAnotherConfigLib.create(CONFIG, ((defaults, config, builder) -> builder.title(Component.literal("Bikes Are Pain Config Screen"))
                    .category(ConfigCategory.createBuilder()
                            .name(Component.translatable("config.bikesarepain.visuals.name"))
                            .tooltip(Component.translatable("config.bikesarepain.visuals.tooltip"))
                            .group(OptionGroup.createBuilder()
                                    .name(Component.translatable("config.bikesarepain.visuals.pedometer.name"))
                                    .description(OptionDescription.of(Component.translatable("config.bikesarepain.visuals.pedometer.tooltip")))
                                    .option(Option.<Boolean>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.visuals.pedometer.imperial.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.visuals.pedometer.imperial.tooltip")))
                                            .binding(false, () -> imperial, value -> imperial = value)
                                            .controller(TickBoxControllerBuilder::create)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .category(ConfigCategory.createBuilder()
                            .name(Component.translatable("config.bikesarepain.serial.name"))
                            .tooltip(Component.translatable("config.bikesarepain.serial.tooltip"))
                            .group(OptionGroup.createBuilder()
                                    .name(Component.translatable("config.bikesarepain.serial.link.name"))
                                    .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.link.tooltip")))
                                    .option(Option.<Boolean>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.serial.link.autoconnect.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.link.autoconnect.tooltip")))
                                            .binding(false, () -> autoConnect, value -> autoConnect = value)
                                            .controller(TickBoxControllerBuilder::create)
                                            .build()
                                    )
                                    .option(Option.<Boolean>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.serial.link.autoconnect_failed.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.link.autoconnect_failed.tooltip")))
                                            .binding(false, () -> showPortNotAvailableMessage, value -> showPortNotAvailableMessage = value)
                                            .controller(TickBoxControllerBuilder::create)
                                            .build()
                                    )
                                    .option(Option.<String>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.serial.link.port.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.link.port.tooltip")))
                                            .binding("No port selected", () -> port, value -> port = value)
                                            .controller(opt -> DropdownStringControllerBuilder.create(opt)
                                                    .values(getPorts())
                                                    .allowEmptyValue(false)
                                                    .allowAnyValue(false)
                                            )
                                            .build()
                                    )
                                    .option(Option.<Integer>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.serial.link.baudrate.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.link.baudrate.tooltip")))
                                            .binding(31250, () -> baudRate, value -> baudRate = value)
                                            .controller(opt -> CyclingListControllerBuilder.create(opt)
                                                    .values(List.of(9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000, 31250))
                                                    .formatValue(value -> Component.literal(value + " baud"))
                                            )
                                        .build()
                                    )
                                    .build()
                            )
                            .group(OptionGroup.createBuilder()
                                    .name(Component.translatable("config.bikesarepain.serial.scale.name"))
                                    .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.tooltip")))
                                    .option(Option.<Float>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.serial.scale.wheel.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.wheel.tooltip")))
                                            .binding(1F, () -> wheelScaleRatio, value -> wheelScaleRatio = value)
                                            .controller(opt -> FloatFieldControllerBuilder.create(opt)
                                                    .range(0.05F, 50F)
                                                    .formatValue(value -> Component.nullToEmpty(
                                                            String.format("1 meter is %.2f %s", value, (value == 1F ? "block" : "blocks"))
                                                    ))
                                            )
                                            .build()
                                    )
                                    .option(Option.<Float>createBuilder()
                                            .name(Component.translatable("config.bikesarepain.serial.scale.speed.name"))
                                            .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.speed.tooltip")))
                                            .binding(1F, () -> speedScaleRatio, value -> speedScaleRatio = value)
                                            .controller(opt -> FloatFieldControllerBuilder.create(opt)
                                                    .range(0.05F, 50F)
                                                    .formatValue(value -> Component.nullToEmpty(
                                                            String.format("1 meter is %.2f %s", value, (value == 1F ? "block" : "blocks"))
                                                    ))
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    ))).generateScreen(parent);
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public String getPortRaw() {
        return port;
    }

    public String getPort() {
        String port = this.getPortRaw();

        if (port.contains(":")) {
            // Assuming that the port is in the format "COM7: Arduino Uno (COM7)"
            return port.split(":")[0];
        }

        return port;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public float getWheelScaleRatio() {
        return wheelScaleRatio;
    }

    public float getSpeedScaleRatio() {
        return speedScaleRatio;
    }

    public void setAutoConnect(boolean value) {
        this.autoConnect = value;
    }

    public void setPort(String value) {
        this.port = value;
    }

    public void setBaudRate(int value) {
        this.baudRate = value;
    }

    public void setWheelScaleRatio(float value) {
        this.wheelScaleRatio = value;
    }

    public void setSpeedScaleRatio(float value) {
        this.speedScaleRatio = value;
    }

    public void setImperial(boolean value) {
        this.imperial = value;
    }

    public boolean isImperial() {
        return imperial;
    }

    public boolean isShowPortNotAvailableMessage() {
        return showPortNotAvailableMessage;
    }

    public void setShowPortNotAvailableMessage(boolean value) {
        this.showPortNotAvailableMessage = value;
    }

    public boolean doesConfigExist() {
        return Platform.getConfigFolder().resolve(String.format("%s.json5", BikesArePain.MOD_ID)).toFile().exists();
    }

    public static List<String> getPorts() {
        List<String> ports = SerialReader.getPortsNamed();
        if (ports.isEmpty()) {
            ports.add("No port available");
        }
        return ports;
    }

    public static void init() {
        CONFIG.load();
    }
}
