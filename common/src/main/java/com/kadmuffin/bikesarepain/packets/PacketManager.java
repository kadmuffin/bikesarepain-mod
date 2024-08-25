package com.kadmuffin.bikesarepain.packets;

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
    public record ArduinoData(float speed, float distanceMoved, float kcalories, float wheelRadius) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ArduinoData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ArduinoData> CODEC = StreamCodec.of(
                (buf, obj) -> {
                    buf.writeFloat(obj.speed);
                    buf.writeFloat(obj.distanceMoved);
                    buf.writeFloat(obj.kcalories);
                    buf.writeFloat(obj.wheelRadius);
                },
                buf -> new ArduinoData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
        public static final NetworkManager.NetworkReceiver<ArduinoData> RECEIVER = (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null){
                if (player.getVehicle() instanceof AbstractBike bike) {
                    bike.setjCommaSpeed(packet.speed);
                    bike.setDistanceTravelled(packet.distanceMoved);
                    bike.setKcaloriesBurned(packet.kcalories);
                    bike.setjCommaWheelRadius(packet.wheelRadius);
                    bike.setjCommaEnabled(true);
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RingBellPacket() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RingBellPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ringbell_click"));
        public static final StreamCodec<FriendlyByteBuf, RingBellPacket> CODEC = StreamCodec.of((buf, obj) -> {}, buf -> new RingBellPacket());

        public static final NetworkManager.NetworkReceiver<RingBellPacket> RECEIVER = (packet, contextSupplier) -> {
            Player player = contextSupplier.getPlayer();
            if (player != null) {
                Bicycle bike = player.getVehicle() instanceof Bicycle ? (Bicycle) player.getVehicle() : null;
                if (bike != null) {
                    bike.ringBell();
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, RingBellPacket.TYPE, RingBellPacket.CODEC, RingBellPacket.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                PacketManager.ArduinoData.TYPE,
                PacketManager.ArduinoData.CODEC,
                PacketManager.ArduinoData.RECEIVER);
    }
}
