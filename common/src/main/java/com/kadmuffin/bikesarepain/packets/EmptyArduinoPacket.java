package com.kadmuffin.bikesarepain.packets;

import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class EmptyArduinoPacket {

    public record Packet(boolean enabled, boolean disconnect) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Packet> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "empty_arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Packet> CODEC = StreamCodec.of(
                (buf, obj) -> {
                    buf.writeBoolean(obj.enabled);
                    buf.writeBoolean(obj.disconnect);
                },
                buf -> new Packet(buf.readBoolean(), buf.readBoolean())
        );

        public static final NetworkManager.NetworkReceiver<Packet> RECEIVER = (packet, context) -> {
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
}
