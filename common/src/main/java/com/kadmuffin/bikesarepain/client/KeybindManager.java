package com.kadmuffin.bikesarepain.client;

import com.kadmuffin.bikesarepain.packets.KeypressPacket;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class KeybindManager {
    private static final List<KeyHandler> MANAGED_KEY_MAPPINGS = new ArrayList<>();

    public static void init() {
        Predicate<Player> onBikeCondition = p -> p.getVehicle() instanceof AbstractBike;

        registerKey("ring_bell", InputConstants.KEY_B, KeypressPacket.KeyType.RING_BELL, onBikeCondition);
        registerKey("brake", InputConstants.KEY_V, KeypressPacket.KeyType.BRAKE, onBikeCondition);
        registerKey("switchd", InputConstants.KEY_N, KeypressPacket.KeyType.SWITCHD, onBikeCondition);

        ClientTickEvent.CLIENT_POST.register(KeybindManager::onClientTick);
    }

    private static void registerKey(String name, int keyCode, KeypressPacket.KeyType keyType, Predicate<Player> activationCondition) {
        KeyMapping keyMapping = new KeyMapping(
                "key.bikesarepain." + name,
                InputConstants.Type.KEYSYM,
                keyCode,
                "key.categories.bikesarepain"
        );
        KeyMappingRegistry.register(keyMapping);
        MANAGED_KEY_MAPPINGS.add(new KeyHandler(keyMapping, keyType, activationCondition));
    }

    private static void onClientTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            for (KeyHandler managedKey : MANAGED_KEY_MAPPINGS) {
                managedKey.tick(minecraft.player);
            }
        }
    }

    private static class KeyHandler {
        private final KeyMapping keyMapping;
        private final KeypressPacket.KeyType keyType;
        private boolean wasPressed;
        private final Predicate<Player> condition;

        public KeyHandler(KeyMapping keyMapping, KeypressPacket.KeyType keyType, Predicate<Player> activationCondition) {
            this.keyMapping = keyMapping;
            this.keyType = keyType;
            this.wasPressed = false;
            this.condition = activationCondition;
        }

        public void tick(Player player) {
            if (!this.condition.test(player)) {
                // Reset the key when the player isn't on the bike
                if (wasPressed) {
                    wasPressed = false;
                    NetworkManager.sendToServer(new KeypressPacket.Packet(false, keyType));
                }
                return;
            }

            boolean isPressed = keyMapping.isDown();
            if (isPressed != wasPressed) {
                wasPressed = isPressed;
                NetworkManager.sendToServer(new KeypressPacket.Packet(isPressed, keyType));
            }
        }
    }
}
