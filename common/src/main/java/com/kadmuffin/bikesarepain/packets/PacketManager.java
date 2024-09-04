package com.kadmuffin.bikesarepain.packets;

import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.server.GameRuleManager;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class PacketManager {
    public enum KeyPress {
        RING_BELL(0),
        BRAKE(1),
        SWITCHD(2);

        private final int type;

        KeyPress(int type) {
            this.type = type;
        }

        public static KeyPress fromType(int type) {
            for (KeyPress key : values()) {
                if (key.getType() == type) {
                    return key;
                }
            }
            return null;
        }

       // Get the type of the keypress
        public int getType() {
            return type;
        }

        public void ringBell(boolean isPressed, NetworkManager.PacketContext context) {
            Player player = context.getPlayer();
            if (player != null) {
                Bicycle bike = player.getVehicle() instanceof Bicycle ? (Bicycle) player.getVehicle() : null;
                if (bike != null) {
                    if (isPressed != bike.isRingAlreadyPressed()) {
                        boolean newRingState = !bike.isRingAlreadyPressed();
                        bike.setRingAlreadyPressed(newRingState);
                        if (newRingState) {
                            bike.ringBell();
                        }
                    }
                }
            }
        }

        public void switchDisplay(boolean isPressed, NetworkManager.PacketContext context) {
            Player player = context.getPlayer();
            if (player != null) {
                Bicycle bike = player.getVehicle() instanceof Bicycle ? (Bicycle) player.getVehicle() : null;
                if (bike != null) {
                    if (isPressed) {
                        bike.chooseNextDisplayStat();
                    }
                }
            }
        }

        public void brake(boolean isPressed, NetworkManager.PacketContext context) {
            Player player = context.getPlayer();
            if (player != null) {
                Bicycle bike = player.getVehicle() instanceof Bicycle ? (Bicycle) player.getVehicle() : null;
                if (bike != null) {
                    if (isPressed != bike.isBraking()) {
                        boolean newBrakeState = !bike.isBraking();
                        bike.setBraking(newBrakeState);
                    }
                }
            }
        }

        public void run(boolean isPressed, NetworkManager.PacketContext context) {
            switch (this) {
                case RING_BELL:
                    ringBell(isPressed, context);
                    break;
                case BRAKE:
                    brake(isPressed, context);
                    break;
                case SWITCHD:
                    switchDisplay(isPressed, context);
                    break;
            }
        }
    }

    public record EmptyArduinoData(boolean enabled) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EmptyArduinoData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "empty_arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EmptyArduinoData> CODEC = StreamCodec.of(
                (buf, obj) -> buf.writeBoolean(obj.enabled),
                buf -> new EmptyArduinoData(buf.readBoolean())
        );

        public static final NetworkManager.NetworkReceiver<EmptyArduinoData> RECEIVER = (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null){
                if (player.getVehicle() instanceof AbstractBike bike) {
                    PlayerAccessor playerAcc = ((PlayerAccessor) player);
                    playerAcc.bikesarepain$setJSCActive(packet.enabled);
                    playerAcc.bikesarepain$setJSCSinceUpdate(0);
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ArduinoData(boolean enabled, float speed, float distanceMoved, float kcalories, float wheelCircumference, float scaleFactor) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ArduinoData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ArduinoData> CODEC = StreamCodec.of(
                (buf, obj) -> {
                    buf.writeBoolean(obj.enabled);
                    buf.writeFloat(obj.speed);
                    buf.writeFloat(obj.distanceMoved);
                    buf.writeFloat(obj.kcalories);
                    buf.writeFloat(obj.wheelCircumference);
                    buf.writeFloat(obj.scaleFactor);
                },
                buf -> new ArduinoData(buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
        public static final NetworkManager.NetworkReceiver<ArduinoData> RECEIVER = (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null){
                if (player.getVehicle() instanceof AbstractBike bike) {
                    PlayerAccessor playerAcc = ((PlayerAccessor) player);

                    // Scale factor is controlled by the player by running
                    // /bikes scale set <factor1> block is <factor2> meter
                    // The physics code for the bike work on the basis of 1 block is 1 meter
                    // This command allows players to make their bike smaller or larger,
                    // typically a bike's wheel has a radius of 0.3 meters, and that i
                    // quite small in the game, so we will allow player to scale the bike
                    // without modifying the physics code

                    // The factor value results of taking the two parameters:
                    // - <factor1>
                    // - <factor2>
                    // and calculating a ratio.

                    // Read the gamerule limit
                    float scaleFactor = packet.scaleFactor;
                    if (bike.level().getGameRules().getRule(GameRuleManager.MAX_BIKE_SCALING).get() < packet.scaleFactor) {
                        scaleFactor = bike.level().getGameRules().getRule(GameRuleManager.MAX_BIKE_SCALING).get();
                    }
                    if (bike.level().getGameRules().getRule(GameRuleManager.MIN_BIKE_SCALING).get() > packet.scaleFactor) {
                        scaleFactor = bike.level().getGameRules().getRule(GameRuleManager.MIN_BIKE_SCALING).get()/10F;
                    }

                    playerAcc.bikesarepain$setJSCActive(packet.enabled);
                    playerAcc.bikesarepain$setJSCSpeed(packet.speed * scaleFactor);
                    playerAcc.bikesarepain$setJSCRealSpeed(packet.speed);
                    playerAcc.bikesarepain$setJSCDistance(packet.distanceMoved);
                    playerAcc.bikesarepain$setJSCCalories(packet.kcalories);
                    playerAcc.bikesarepain$setJSCWheelRadius(packet.wheelCircumference * scaleFactor);

                    playerAcc.bikesarepain$setJSCSinceUpdate(0);
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record KeypressPacket(boolean isPressed, KeyPress keyEnum) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<KeypressPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ringbell_click"));
        public static final StreamCodec<FriendlyByteBuf, KeypressPacket> CODEC = StreamCodec.of((buf, obj) -> {
            buf.writeBoolean(obj.isPressed);
            buf.writeEnum(obj.keyEnum);
        }, buf -> new KeypressPacket(buf.readBoolean(), buf.readEnum(KeyPress.class)));

        public static final NetworkManager.NetworkReceiver<KeypressPacket> RECEIVER = (packet, contextSupplier) -> packet.keyEnum.run(packet.isPressed, contextSupplier);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UnitSystemPacket(boolean useImperial) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UnitSystemPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "unit_system"));
        public static final StreamCodec<FriendlyByteBuf, UnitSystemPacket> CODEC = StreamCodec.of((buf, obj) -> buf.writeBoolean(obj.useImperial), buf -> new UnitSystemPacket(buf.readBoolean()));

        public static final NetworkManager.NetworkReceiver<UnitSystemPacket> RECEIVER = (packet, contextSupplier) -> {
            Player player = contextSupplier.getPlayer();
            if (player != null) {
                ((PlayerAccessor) player).bikesarepain$setAmericaUnitsPls(packet.useImperial);
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, KeypressPacket.TYPE, KeypressPacket.CODEC, KeypressPacket.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                PacketManager.ArduinoData.TYPE,
                PacketManager.ArduinoData.CODEC,
                PacketManager.ArduinoData.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                PacketManager.UnitSystemPacket.TYPE,
                PacketManager.UnitSystemPacket.CODEC,
                PacketManager.UnitSystemPacket.RECEIVER);
    }
}
