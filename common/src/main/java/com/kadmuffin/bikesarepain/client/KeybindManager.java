package com.kadmuffin.bikesarepain.client;

import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;

public class KeybindManager {
    public static KeyMapping RING_BELL = new KeyMapping(
            "key.bikesarepain.ring_bell",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_B,
            "key.categories.bikesarepain"
    );

    public static void init() {
        KeyMappingRegistry.register(RING_BELL);
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (RING_BELL.consumeClick()) {
                NetworkManager.sendToServer(new PacketManager.RingBellPacket());
            }
        });
    }
}
