package com.kadmuffin.bikesarepain.packets;

import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class KeypressPacket {

    @FunctionalInterface
    public interface KeyAction {
        void execute(boolean isPressed, Bicycle bike);
    }

    public enum KeyType {
        RING_BELL(0, (isPressed, bike) -> {
            if (isPressed != bike.isRingAlreadyPressed()) {
                bike.setRingAlreadyPressed(isPressed);
                if (isPressed) {
                    bike.ringBell();
                }
            }
        }),
        BRAKE(1, (isPressed, bike) -> {
            if (isPressed != bike.isBraking()) {
                bike.setBraking(isPressed);
            }
        }),
        // Switching the current display category
        SWITCHD(2, (isPressed, bike) -> {
            if (isPressed) {
                bike.chooseNextDisplayStat();
            }
        });

        private final int type;
        private final KeyAction action;
        private static final Map<Integer, KeyType> BY_TYPE =
                Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(KeyType::getType, key -> key));

        KeyType(int type, KeyAction action) {
            this.type = type;
            this.action = action;
        }

        public static KeyType fromType(int type) {
            return BY_TYPE.get(type);
        }

        public int getType() {
            return this.type;
        }

        public void run(boolean isPressed, NetworkManager.PacketContext context) {
            Player player = context.getPlayer();
            if (player != null && player.getVehicle() instanceof Bicycle bike) {
                this.action.execute(isPressed, bike);
            }
        }
    }

    public record Packet(boolean isPressed, KeyType keyEnum) implements CustomPacketPayload {
        public static final Type<Packet> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "keypress_packet"));
        public static final StreamCodec<FriendlyByteBuf, Packet> CODEC = StreamCodec.of(
                (buf, packet) -> {
                    buf.writeBoolean(packet.isPressed);
                    buf.writeEnum(packet.keyEnum);
                },
                buf -> new Packet(buf.readBoolean(), buf.readEnum(KeyType.class))
        );

        public static final NetworkManager.NetworkReceiver<Packet> RECEIVER =
                (packet, context) -> packet.keyEnum.run(packet.isPressed, context);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}