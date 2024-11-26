package com.kadmuffin.bikesarepain.neoforge;

import com.kadmuffin.bikesarepain.packets.VersionCheckPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.kadmuffin.bikesarepain.BikesArePain.*;
import static com.kadmuffin.bikesarepain.BikesArePain.MOD_NAME;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
final public class PacketEventHandler {
    private PacketEventHandler() {}

    @SubscribeEvent
    public static void registerConfigurations(final RegisterConfigurationTasksEvent event) {
        event.register(new VersionCheckPacket.VersionCheckTask());
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final String[] version = VersionCheckPacket.getVersionSplit();
        final PayloadRegistrar registrar = event.registrar(MOD_ID)
                .versioned(String.format("%s.%s.0", version[0], version[1]));

        registrar.configurationToClient(VersionCheckPacket.S2CVersionRequest.TYPE,
                StreamCodec.<FriendlyByteBuf, VersionCheckPacket.S2CVersionRequest>of(
                        (buf, obj) -> {}, buf -> new VersionCheckPacket.S2CVersionRequest()),
                (arg, context) -> {
                    VersionCheckPacket.C2SVersionShare response;

                    try {
                        response = VersionCheckPacket.S2CVersionRequest.prepareResponse();
                    } catch (Exception e) {
                        LOGGER.error(String.format("[%s -> Client]", MOD_NAME), e);
                        context.disconnect(Component.literal(String.format(
                                "[%s] Something went wrong while parsing the version number in the client. Check the console logs.",
                                MOD_NAME
                        )));
                        return;
                    }

                    context.reply(response);
                }
        );

        registrar.configurationToServer(VersionCheckPacket.C2SVersionShare.TYPE,
                StreamCodec.<FriendlyByteBuf, VersionCheckPacket.C2SVersionShare>of(
                        (buf, obj) -> {
                            buf.writeInt(obj.major());
                            buf.writeInt(obj.minor());
                            buf.writeInt(obj.patch());
                        },
                        buf -> new VersionCheckPacket.C2SVersionShare(
                                buf.readInt(),
                                buf.readInt(),
                                buf.readInt()
                        )
                ),
                (payload, context) -> {
                    boolean versionAccepted;
                    try {
                        versionAccepted = VersionCheckPacket.C2SVersionShare.isVersionSupported(payload);
                    } catch (Exception e) {
                        LOGGER.error(String.format("%s -> Server", MOD_NAME), e);
                        context.disconnect(Component.literal(String.format(
                                "[%s] Something went wrong while parsing the version number in the server side.",
                                MOD_NAME
                        )));
                        context.finishCurrentTask(VersionCheckPacket.VersionCheckTask.TYPE);
                        return;
                    }

                    if (versionAccepted) {
                        LOGGER.info(String.format("%s version is valid.", MOD_NAME));
                        context.finishCurrentTask(VersionCheckPacket.VersionCheckTask.TYPE);
                    } else {
                        LOGGER.info(String.format("%s version is not supported. Disconnecting.", MOD_NAME));
                        context.disconnect(
                                VersionCheckPacket.C2SVersionShare.getDisconnectMessage(payload)
                        );
                    }
                }
        );
    }

}
