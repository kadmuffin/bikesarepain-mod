package com.kadmuffin.bikesarepain.client;

import com.kadmuffin.bikesarepain.BikesArePain;
import dev.architectury.platform.Platform;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.text.DecimalFormat;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientConfig {
    public static final DecimalFormat format = new DecimalFormat("#.##");
    public static final ConfigClassHandler<ClientConfig> CONFIG = ConfigClassHandler.createBuilder(ClientConfig.class)
            .id(ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "config.client"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(Platform.getConfigFolder().resolve(String.format("%s.client.json5", BikesArePain.MOD_ID)))
                    .setJson5(true)
                    .build())
            .build();
    @SerialEntry(comment = "Whether to sync the pitch with the server. If this is enabled, the server will use the client's visual pitch for applying gravity acceleration.")
    private boolean syncPitchWithServer = true;
    @SerialEntry(comment = "Display Imperial units instead of metric.")
    private boolean imperial = false;
    @SerialEntry(comment = "Amount of rays to cast per wheel for pitching the bike depending on terrain.")
    private int amountOfRaysPerWheel = 4;
    @SerialEntry(comment = "Use (probably not well done) interpolation for movement.")
    private boolean interpolation = true;
    @SerialEntry(comment = "Whether to automatically connect to the serial port on startup.")
    private boolean autoConnect = false;
    @SerialEntry(comment = "Display a message when the port chosen for auto connect is not available.")
    private boolean showPortNotAvailableMessage = true;
    @SerialEntry(comment = "The port to connect to.")
    private String port = "COM7";
    @SerialEntry(comment = "The baud rate to connect with.")
    private int baudRate = 31250;
    @SerialEntry(comment = "How much to scale the speed by. Calculated with 1 / x meters needed to do a full block. ")
    private float speedScaleRatio = 1f;
    @SerialEntry(comment = "How much to scale the wheel size read by JSerialComm. You can calculate it in the same way as the speedRatio.")
    private float wheelScaleRatio = 1f;
    @SerialEntry(comment = "When true, the values showed at the pedometer will be calculated with a reference value in mind (maps the small fitness bike to the real bike).")
    private boolean scaleCalcByRef = true;
    @SerialEntry(comment = "Map the original wheel size to the reference for wheel/speed calculations.")
    private boolean useForCalculations = true;
    @SerialEntry(comment = "The real size of a bicycle wheel in meters.")
    private float targetWheelSize = 0.62f;
    @SerialEntry(comment = "The mass of the player in kilograms (or if \"imperial=true\", in pounds).")
    private float bodyMass = 70f;
    @SerialEntry(comment = "Show debug rays for the wheel.")
    private boolean debugShowWheelRays = false;
    @SerialEntry(comment = "Minimum wait for raycasting to happen (in ticks).")
    private int minimumRaycastWait = 1;
    @SerialEntry(comment = "Maximum wait for raycasting to happen (in ticks).")
    private int maximumRaycastWait = 24;
    @SerialEntry(comment = "Threshold for considering height changes significant (in blocks).")
    private float verticalThreshold = 0.6f;
    @SerialEntry(comment = "Factor that determines how much is the wait for raycast reduced by height changes.")
    private float verticalSensitivity = 0.8f;
    @SerialEntry(comment = "Controls how quickly delay decreases as speed increases.")
    private float speedSensitivity = 4.0f;
    @SerialEntry(comment = "How many speed data points to use for smoothing changes (Only used if a fitness bike is linked).")
    private int speedDataPoints = 3;
    @SerialEntry(comment = "Max amount of fitness datapoints to keep in memory (Only used if a fitness bike is linked).")
    private int maxMemoryDatapoints = 1000;

    public static String roundUpToTwo(float value) {
        return format.format(Math.round(value * 100) / 100f);
    }

    public static String roundUpToOne(float value) {
        return format.format(Math.round(value * 10) / 10f);
    }


    public static String getUnitString(float value, boolean imperial) {
        if (imperial) {
            // Convert blocks per meter to blocks per foot
            float blocksPerFoot = value * 3.28084f; // value is blocks per meter
            return String.format("1 foot measures %s %s",
                    roundUpToTwo(blocksPerFoot),
                    (blocksPerFoot == 1f ? "block" : "blocks"));
        } else {
            return String.format("1 meter measures %s %s",
                    // Limit to
                    roundUpToTwo(value),
                    (value == 1f ? "block" : "blocks"));
        }
    }

    public static String getAutoKgToLbString(float value, boolean imperial) {
        if (imperial) {
            // Convert kilograms to pounds
            float pounds = value * 2.20462f; // value is kilograms
            return String.format("%s pounds", roundUpToTwo(pounds));
        } else {
            // from pounds to kg
            return String.format("%s kg", roundUpToTwo(value / 2.20462f));
        }
    }

    public static String getAutoCMtoInchString(float value, boolean imperial) {
        if (imperial) {
            // Convert centimeters to inches
            float inches = value * 39.3701f; // value is meters
            return String.format("%s inches", roundUpToTwo(inches));
        } else {
            // from meters to cm
            return String.format("%s cm", roundUpToTwo(value * 100));
        }
    }

    public static double getMET(float speedKPH) {
        if (speedKPH == 0) return 1;
        if (speedKPH <= 15) return 5.8;
        else if (speedKPH <= 19) return 6.8;
        else if (speedKPH <= 22) return 8.0;
        else if (speedKPH <= 25) return 10.0;
        else if (speedKPH <= 30) return 12.0;
        else return 16.8;
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
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.visuals.pitch.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.visuals.pitch.tooltip")))
                                        .binding(4, () -> amountOfRaysPerWheel, value -> amountOfRaysPerWheel = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                // If the value is 0, we want to display "Disabled" instead of "0"
                                                .formatValue(value -> (value == 0 ? Component.translatable("config.bikesarepain.visuals.pitch.disabled") : Component.literal(String.format("%d rays", value))))
                                        )
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.visuals.pitch.sync_pitch.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.visuals.pitch.sync_pitch.tooltip")))
                                        .binding(true, () -> syncPitchWithServer, value -> syncPitchWithServer = value)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(value -> (value ? Component.translatable("config.bikesarepain.visuals.pitch.sync_pitch.enabled") : Component.translatable("config.bikesarepain.visuals.pitch.sync_pitch.disabled")))
                                                .coloured(true)
                                        )
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.visuals.bad_interpolation.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.visuals.bad_interpolation.tooltip")))
                                        .binding(true, () -> interpolation, value -> interpolation = value)
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
                                        .binding(true, () -> showPortNotAvailableMessage, value -> showPortNotAvailableMessage = value)
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
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.scale.reference.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.reference.tooltip")))
                                        .binding(true, () -> scaleCalcByRef, value -> scaleCalcByRef = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.scale.use_for_calculations.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.use_for_calculations.tooltip")))
                                        .binding(true, () -> useForCalculations, value -> useForCalculations = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.scale.wheel.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.wheel.tooltip")))
                                        .binding(1.5F, () -> this.wheelScaleRatio, value -> this.wheelScaleRatio = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(ClientConfig.getUnitString(value, this.imperial)))
                                                .range(0.05F, 10F)
                                                .step(0.05F)
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.scale.speed.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.speed.tooltip")))
                                        .binding(1F, () -> this.speedScaleRatio, value -> this.speedScaleRatio = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(ClientConfig.getUnitString(value, this.imperial)))
                                                .range(0.05F, 10F)
                                                .step(0.05F)
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.scale.reference.wheel.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.scale.reference.wheel.tooltip")))
                                        .binding(0.622F, () -> targetWheelSize, value -> targetWheelSize = Math.max(0.01F, value))
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(ClientConfig.getAutoCMtoInchString(value, this.imperial)))
                                                .range(0.05F, 2F)
                                                .step((imperial ? 0.01F : 0.001F))
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.calories.mass.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.calories.mass.tooltip")))
                                        .binding(70F, () -> bodyMass, value -> bodyMass = value)
                                        .controller(opt -> FloatFieldControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(imperial ?
                                                        String.format("%s pounds", roundUpToTwo(value)) :
                                                        String.format("%s kg", roundUpToTwo(value)))
                                                )
                                                .min(1F)
                                        )
                                        .build()
                                )
                                .build()
                        )

                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.bikesarepain.serial.smoothing.name"))
                                .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.smoothing.tooltip")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.serial.smoothing.speed"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.serial.smoothing.speed.tooltip")))
                                        .binding(3, () -> speedDataPoints, value -> speedDataPoints = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .step(1)
                                                .range(1, 16)
                                                .formatValue(value -> Component.literal(String.format("%d point%s",
                                                        value,
                                                        value == 1 ? "" : "s"
                                                        )))
                                        )
                                        .build()
                                )
                                .build()
                        )

                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.bikesarepain.advanced.name"))
                        .tooltip(Component.translatable("config.bikesarepain.advanced.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.bikesarepain.advanced.raycast_timing.name"))
                                .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.raycast_timing.tooltip")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.advanced.raycast_timing.min_ray_wait"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.raycast_timing.min_ray_wait.tooltip")))
                                        .binding(1, () -> minimumRaycastWait, (value) -> minimumRaycastWait = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(value == 1 ? String.format("%d tick", value) : String.format("%d ticks", value)))
                                                .range(0, 5)
                                                .step(1)
                                        )
                                        .build()
                                )
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.advanced.raycast_timing.max_ray_wait"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.raycast_timing.max_ray_wait.tooltip")))
                                        .binding(12, () -> maximumRaycastWait, (value) -> maximumRaycastWait = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(value == 0 ? "Run every tick" : String.format("%d ticks", value)))
                                                .range(0, 42)
                                                .step(6)
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.advanced.raycast_timing.vertical_threshold"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.raycast_timing.vertical_threshold.tooltip")))
                                        .binding(0.6f, () -> verticalThreshold, (value) -> verticalThreshold = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .formatValue(value -> Component.literal(value == 1 ? "1 block" : String.format("%s blocks", roundUpToOne(value))))
                                                .range(0.1f, 1f)
                                                .step(0.1f)
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.advanced.raycast_timing.vertical_sensitivity"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.raycast_timing.vertical_sensitivity.tooltip")))
                                        .binding(0.8f, () -> verticalSensitivity, (value) -> verticalSensitivity = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.3f, 1f)
                                                .step(0.1f)
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.advanced.raycast_timing.speed_sensitivity"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.raycast_timing.speed_sensitivity.tooltip")))
                                        .binding(4f, () -> speedSensitivity, (value) -> speedSensitivity = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(1.5f, 6f)
                                                .step(0.1f)
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.bikesarepain.advanced.debug.name"))
                                .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.debug.tooltip")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.bikesarepain.advanced.debug.wheel_rays.name"))
                                        .description(OptionDescription.of(Component.translatable("config.bikesarepain.advanced.debug.wheel_rays.tooltip")))
                                        .binding(false, () -> debugShowWheelRays, value -> debugShowWheelRays = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
        )).generateScreen(parent);
    }

    public float kilogramsToPounds(float value) {
        return value * 2.20462f;
    }

    public float poundsToKilograms(float value) {
        return value / 2.20462f;
    }

    public float getBodyMassKg() {
        return imperial ? this.poundsToKilograms(this.bodyMass) : bodyMass;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean value) {
        this.autoConnect = value;
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

    public void setPort(String value) {
        this.port = value;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int value) {
        this.baudRate = value;
    }

    public float getWheelScaleRatio() {
        return wheelScaleRatio;
    }

    public void setWheelScaleRatio(float value) {
        this.wheelScaleRatio = value;
    }

    public float getSpeedScaleRatio() {
        return speedScaleRatio;
    }

    public void setSpeedScaleRatio(float value) {
        this.speedScaleRatio = value;
    }

    public boolean isImperial() {
        return imperial;
    }

    public void setImperial(boolean value) {
        this.imperial = value;
    }

    public boolean useInterpolation() {
        return interpolation;
    }

    public void setInterpolation(boolean value) {
        this.interpolation = value;
    }

    public boolean pitchBasedOnBlocks() {
        return amountOfRaysPerWheel > 0;
    }

    public int getAmountOfRaysPerWheel() {
        return amountOfRaysPerWheel;
    }

    public void setAmountOfRaysPerWheel(int value) {
        this.amountOfRaysPerWheel = value;
    }

    public boolean syncPitchWithServer() {
        return syncPitchWithServer;
    }

    public void setSyncPitchWithServer(boolean value) {
        this.syncPitchWithServer = value;
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

    public boolean wantsValuesScaled() {
        return this.scaleCalcByRef && this.targetWheelSize > 0;
    }

    public float getTargetWheelSize() {
        return this.targetWheelSize / 2f;
    }

    public boolean useMappedForCalculations() {
        return this.useForCalculations;
    }

    public void setUseMappedForCalculations(boolean value) {
        this.useForCalculations = value;
    }

    public boolean showDebugWheelRays() {
        return this.debugShowWheelRays;
    }

    public int getMinimumRaycastWait() { return this.minimumRaycastWait; }
    public int getMaximumRaycastWait() { return this.maximumRaycastWait; }
    public float getVerticalThreshold() { return this.verticalThreshold; }
    public float getVerticalSensitivity() { return this.verticalSensitivity; }
    public float getSpeedSensitivity() { return this.speedSensitivity; }


    public float calculateCalories(double speedKPH, double timeHours) {
        return (float) (getMET((float) speedKPH) * this.getBodyMassKg() * timeHours);
    }

    public void setScale(float scaleBlock, float scaleMeter, ApplyScaleTo target) {
        float scale = scaleBlock / scaleMeter;

        switch (target) {
            case SPEED:
                this.speedScaleRatio = scale;
                break;
            case WHEEL:
                this.wheelScaleRatio = scale;
                break;
            case BOTH:
                this.speedScaleRatio = scale;
                this.wheelScaleRatio = scale;
                break;
        }
    }

    public String getScaleRatiosString() {
        return String.format(
                "Speed is set to %s, Wheel is set to %s",
                getUnitString(this.speedScaleRatio, this.imperial),
                getUnitString(this.wheelScaleRatio, this.imperial)
        );
    }

    public int getSpeedDataPoints() {
        return speedDataPoints >= 1 ? speedDataPoints : 1;
    }

    public int getMaxMemoryDatapoints() {
        return maxMemoryDatapoints;
    }

    public enum ApplyScaleTo {
        SPEED,
        WHEEL,
        BOTH
    }
}
