package com.kadmuffin.bikesarepain.packets;

import dev.architectury.platform.Platform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;
import static com.kadmuffin.bikesarepain.BikesArePain.MOD_NAME;

public class VersionCheckPacket {
    public record S2CVersionRequest() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<S2CVersionRequest> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "s2c_version_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, S2CVersionRequest> CODEC = StreamCodec.of((buf, obj) -> {},
                buf -> new S2CVersionRequest());

        public static C2SVersionShare prepareResponse() throws ArrayIndexOutOfBoundsException, NumberFormatException {
            String[] versionParts = getVersionSplit();

            int major;
            int minor;
            int patch;

            major = Integer.parseInt(versionParts[0]);
            minor = Integer.parseInt(versionParts[1]);
            patch = Integer.parseInt(versionParts[2]);

            return new C2SVersionShare(major, minor, patch);
        }

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

        public static boolean isVersionSupported(C2SVersionShare packet) throws ArrayIndexOutOfBoundsException, NumberFormatException  {
            String[] versionParts = getVersionSplit();

            // Assume the server's version is fine, and if not
            // the game will crash most likely.
            int major = Integer.parseInt(versionParts[0]);
            int minor = Integer.parseInt(versionParts[1]);

            return major == packet.major() && minor == packet.minor();
        }

        public static Component getDisconnectMessage(C2SVersionShare payload) {
            return Component.literal("Expected version ")
                    .append(Component.literal(String.format("v%s", VersionCheckPacket.getVersion()))
                            .withColor(CommonColors.SOFT_YELLOW).append(
                                    Component.literal(String.format(" of mod '%s' but found ", MOD_NAME)).withColor(CommonColors.WHITE).append(
                                            Component.literal(String.format("v%d.%d.%d",
                                                            payload.major(),
                                                            payload.minor(),
                                                            payload.patch()
                                                    )).withColor(CommonColors.SOFT_RED)
                                                    .append(
                                                            Component.literal("!").withColor(CommonColors.WHITE)
                                                    )
                                    )
                            )
                    );
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record VersionCheckTask() implements ConfigurationTask {
        public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath(MOD_ID, "s2c_version_request").toString());

        @Override
        public void start(Consumer<Packet<?>> task) {
            task.accept(new ClientboundCustomPayloadPacket(new S2CVersionRequest()));
        }

        @Override
        public @NotNull Type type() {
            return TYPE;
        }
    }

    public static String[] getVersionSplit() {
        return getVersion().split("\\.");
    }

    public static String getVersion() {
        return Platform.getMod(MOD_ID).getVersion();
    }

}
