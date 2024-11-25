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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class PacketManager {
    public static void processArduinoData(ArduinoData packet, Player player, Level level) {
        PlayerAccessor playerAcc = ((PlayerAccessor) player);

        final float scaleFactorWheel = packet.scaleFactorWheel;
        final float scaleFactorSpeed = packet.scaleFactorSpeed;

        final float maxWheelSize = level.getGameRules().getRule(GameRuleManager.MAX_BIKE_WHEEL_SIZE).get();
        final float minWheelSize = level.getGameRules().getRule(GameRuleManager.MIN_BIKE_WHEEL_SIZE).get() / 100F;

        float wheelSize = packet.wheelRadius * scaleFactorWheel;
        wheelSize = Math.min(maxWheelSize, Math.max(minWheelSize, wheelSize));

        playerAcc.bikesarepain$setJSCActive(packet.enabled);
        playerAcc.bikesarepain$setJSCSpeed(packet.speed * scaleFactorSpeed);
        playerAcc.bikesarepain$setJSCWheelRadius(wheelSize);

        playerAcc.bikesarepain$setJSCRealSpeed(packet.scaledSpeed);
        playerAcc.bikesarepain$setJSCDistance(packet.distanceMoved);
        playerAcc.bikesarepain$setJSCCalories(packet.kcalories);

        playerAcc.bikesarepain$setJSCSinceUpdate(0);
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, KeypressPacket.Packet.TYPE, KeypressPacket.Packet.CODEC, KeypressPacket.Packet.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                PacketManager.ArduinoData.TYPE,
                PacketManager.ArduinoData.CODEC,
                PacketManager.ArduinoData.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                PacketManager.EmptyArduinoData.TYPE,
                PacketManager.EmptyArduinoData.CODEC,
                PacketManager.EmptyArduinoData.RECEIVER);
    }


    public record EmptyArduinoData(boolean enabled, boolean disconnect) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EmptyArduinoData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "empty_arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EmptyArduinoData> CODEC = StreamCodec.of(
                (buf, obj) -> {
                    buf.writeBoolean(obj.enabled);
                    buf.writeBoolean(obj.disconnect);
                },
                buf -> new EmptyArduinoData(buf.readBoolean(), buf.readBoolean())
        );

        public static final NetworkManager.NetworkReceiver<EmptyArduinoData> RECEIVER = (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null) {
                if (player.getVehicle() instanceof AbstractBike) {
                    PlayerAccessor playerAcc = ((PlayerAccessor) player);
                    playerAcc.bikesarepain$setJSCActive(packet.enabled);
                    if (packet.disconnect) {
                        playerAcc.bikesarepain$setJSCSinceUpdate(1000);
                    } else {
                        playerAcc.bikesarepain$setJSCSinceUpdate(0);
                    }
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ArduinoData(boolean enabled, float speed, float scaledSpeed, float distanceMoved, float kcalories,
                              float wheelRadius, float scaleFactorWheel,
                              float scaleFactorSpeed) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ArduinoData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ArduinoData> CODEC = StreamCodec.of(
                (buf, obj) -> {
                    buf.writeBoolean(obj.enabled);
                    buf.writeFloat(obj.speed);
                    buf.writeFloat(obj.scaledSpeed);
                    buf.writeFloat(obj.distanceMoved);
                    buf.writeFloat(obj.kcalories);
                    buf.writeFloat(obj.wheelRadius);
                    buf.writeFloat(obj.scaleFactorWheel);
                    buf.writeFloat(obj.scaleFactorSpeed);
                },
                buf -> new ArduinoData(buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
        public static final NetworkManager.NetworkReceiver<ArduinoData> RECEIVER = (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null) {
                if (player.getVehicle() instanceof AbstractBike bike) {
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
                    processArduinoData(packet, player, bike.level());
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

}
