package com.kadmuffin.bikesarepain.client;

import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;

public class KeybindManager {
    public static boolean alreadyRinging = false;
    public static boolean alreadyBraking = false;

    public static KeyMapping RING_BELL = new KeyMapping(
            "key.bikesarepain.ring_bell",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_B,
            "key.categories.bikesarepain"
    );

    public static KeyMapping BRAKE = new KeyMapping(
            "key.bikesarepain.brake",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.categories.bikesarepain"
    );

    public static void init() {
        KeyMappingRegistry.register(RING_BELL);
        KeyMappingRegistry.register(BRAKE);

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (minecraft.player == null) {
                return;
            }

            if (minecraft.player.getVehicle() == null || !(minecraft.player.getVehicle() instanceof AbstractBike vehicle)) {
                return;
            }

            /*if (RING_BELL.consumeClick()) {
                NetworkManager.sendToServer(new PacketManager.KeypressPacket(true, PacketManager.KeyPress.RING_BELL));
            }
             */

            if (RING_BELL.isDown() != alreadyRinging) {
                alreadyRinging = !alreadyRinging;
                NetworkManager.sendToServer(new PacketManager.KeypressPacket(alreadyRinging, PacketManager.KeyPress.RING_BELL));
            }

            if (BRAKE.isDown() != alreadyBraking) {
                alreadyBraking = !alreadyBraking;
                NetworkManager.sendToServer(new PacketManager.KeypressPacket(alreadyBraking, PacketManager.KeyPress.BRAKE));
            }
        });
    }
}
