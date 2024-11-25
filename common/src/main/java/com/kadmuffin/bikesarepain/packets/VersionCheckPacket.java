package com.kadmuffin.bikesarepain.packets;

import com.kadmuffin.bikesarepain.accessor.PlayerAccessor;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class VersionCheckPacket {
    private static final Logger LOGGER = LogManager.getLogger();

    public record S2CVersionRequest() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<S2CVersionRequest> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "s2c_version_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, S2CVersionRequest> CODEC = StreamCodec.of((buf, obj) -> {},
                buf -> new S2CVersionRequest());

        // This happens in the client
        public static final NetworkManager.NetworkReceiver<S2CVersionRequest> RECEIVER = (packet, context) -> {
            String[] versionParts = getVersion();

            int major;
            int minor;
            int patch;

            try {
                major = Integer.parseInt(versionParts[0]);
                minor = Integer.parseInt(versionParts[1]);
                patch = Integer.parseInt(versionParts[2]);
            } catch (Exception e) {
                if (Minecraft.getInstance().getConnection() instanceof ClientPacketListener listener) {
                    listener.getConnection().disconnect(
                            Component.literal(
                                    String.format("[%s] Something went wrong while parsing the mod version. Check your console logs.", MOD_ID)
                            )
                    );
                }

                LOGGER.error(MOD_ID, e);
                return;
            }

            NetworkManager.sendToServer(new C2SVersionShare(major, minor, patch));
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record C2SVersionShare(int major, int minor, int patch) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<C2SVersionShare> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "c2s_version_share"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2SVersionShare> CODEC = StreamCodec.of((buf, obj) -> {
            buf.writeInt(obj.major);
            buf.writeInt(obj.minor);
            buf.writeInt(obj.patch);
        }, buf -> new C2SVersionShare(buf.readInt(), buf.readInt(), buf.readInt()));

        // This happens in the server
        public static final NetworkManager.NetworkReceiver<C2SVersionShare> RECEIVER = (packet, context) -> {
            String[] versionParts = getVersion();

            // Assume the server's version is fine, and if not
            // the game will crash most likely.
            int major = Integer.parseInt(versionParts[0]);
            int minor = Integer.parseInt(versionParts[1]);

            boolean versionMatches = major == packet.major() && minor == packet.minor();

            if (!versionMatches && context.getPlayer() instanceof ServerPlayer player) {
                player.connection.disconnect(Component.literal(String.format("[%s] Expected version ", MOD_ID)).append(
                        Component.literal(String.format("%d.%d.X ", major, minor)).withColor(CommonColors.SOFT_YELLOW).append(
                                Component.literal("but found version ").append(
                                        Component.literal(String.format("%d.%d.X", packet.major, packet.minor)).withColor(CommonColors.SOFT_RED)
                                                .append(Component.literal("."))
                                )
                        )
                ));
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static String[] getVersion() {
        String versionString = Platform.getMod(MOD_ID).getVersion();
        return versionString.split("\\.");
    }


}
