package com.kadmuffin.bikesarepain.client;

import com.kadmuffin.bikesarepain.packets.KeypressPacket;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public class KeybindManager {
    public static final KeyMapping RING_BELL = new KeyMapping(
            "key.bikesarepain.ring_bell",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_B,
            "key.categories.bikesarepain"
    );
    public static final KeyMapping BRAKE = new KeyMapping(
            "key.bikesarepain.brake",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.categories.bikesarepain"
    );
    public static final KeyMapping SWITCHD = new KeyMapping(
            "key.bikesarepain.switchd",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_N,
            "key.categories.bikesarepain"
    );
    public static boolean alreadyRinging = false;
    public static boolean alreadyBraking = false;
    public static boolean alreadySwitchedType = false;

    public static void init() {
        KeyMappingRegistry.register(RING_BELL);
        KeyMappingRegistry.register(BRAKE);
        KeyMappingRegistry.register(SWITCHD);

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (minecraft.player == null) {
                return;
            }

            if (minecraft.player.getVehicle() == null || !(minecraft.player.getVehicle() instanceof AbstractBike)) {
                return;
            }

            if (RING_BELL.isDown() != alreadyRinging) {
                alreadyRinging = !alreadyRinging;
                NetworkManager.sendToServer(new KeypressPacket.Packet(alreadyRinging, KeypressPacket.KeyType.RING_BELL));
            }

            if (BRAKE.isDown() != alreadyBraking) {
                alreadyBraking = !alreadyBraking;
                NetworkManager.sendToServer(new KeypressPacket.Packet(alreadyBraking, KeypressPacket.KeyType.BRAKE));
            }

            if (SWITCHD.isDown() != alreadySwitchedType) {
                alreadySwitchedType = !alreadySwitchedType;
                NetworkManager.sendToServer(new KeypressPacket.Packet(alreadySwitchedType, KeypressPacket.KeyType.SWITCHD));
            }
        });
    }
}
