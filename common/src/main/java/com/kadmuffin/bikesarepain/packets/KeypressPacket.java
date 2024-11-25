package com.kadmuffin.bikesarepain.packets;

import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class KeypressPacket {
    public enum KeyType {
        RING_BELL(0),
        BRAKE(1),
        SWITCHD(2);

        private final int type;

        KeyType(int type) {
            this.type = type;
        }

        public static KeyType fromType(int type) {
            for (KeyType key : values()) {
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

    public record Packet(boolean isPressed, KeyType keyEnum) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Packet> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "keypress_packet"));
        public static final StreamCodec<FriendlyByteBuf, Packet> CODEC = StreamCodec.of((buf, obj) -> {
            buf.writeBoolean(obj.isPressed);
            buf.writeEnum(obj.keyEnum);
        }, buf -> new KeypressPacket.Packet(buf.readBoolean(), buf.readEnum(KeyType.class)));

        public static final NetworkManager.NetworkReceiver<KeypressPacket.Packet> RECEIVER = (packet, contextSupplier) -> packet.keyEnum.run(packet.isPressed, contextSupplier);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
