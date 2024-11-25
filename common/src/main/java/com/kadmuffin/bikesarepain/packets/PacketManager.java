package com.kadmuffin.bikesarepain.packets;

import dev.architectury.networking.NetworkManager;

public class PacketManager {
    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, KeypressPacket.Packet.TYPE, KeypressPacket.Packet.CODEC, KeypressPacket.Packet.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                ArduinoPacket.Packet.TYPE,
                ArduinoPacket.Packet.CODEC,
                ArduinoPacket.Packet.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                EmptyArduinoPacket.Packet.TYPE,
                EmptyArduinoPacket.Packet.CODEC,
                EmptyArduinoPacket.Packet.RECEIVER);
    }
}
