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
    }
}
