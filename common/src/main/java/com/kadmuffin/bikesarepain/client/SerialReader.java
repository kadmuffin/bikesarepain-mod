package com.kadmuffin.bikesarepain.client;

import com.fazecast.jSerialComm.SerialPort;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialReader implements Runnable {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final StringBuilder buffer = new StringBuilder();
    private boolean markerFound = false;
    private SerialPort serialPort;
    private Queue<Float> speedQueue;
    private float sumSpeed = 0;

    public SerialPort getSerial() {
        return this.serialPort;
    }

    public void setSerial(String port) {
        if (this.serialPort != null && this.serialPort.isOpen()) {
            this.serialPort.closePort();
        }

        this.serialPort = SerialPort.getCommPort(port);
        this.serialPort.setComPortParameters(31250, 8, 1, 0);
        this.serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    }

    public void start() {
        this.serialPort.openPort();
        this.speedQueue = new LinkedList<>();
        Thread worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
        this.serialPort.closePort();
    }

    public String[] getPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                if (this.serialPort.isOpen()) {
                    if (this.serialPort.bytesAvailable() > 0) {
                        byte[] readBuffer = new byte[this.serialPort.bytesAvailable()];
                        int numRead = this.serialPort.readBytes(readBuffer, readBuffer.length);

                        //String data = new String(readBuffer, 0, numRead, StandardCharsets.US_ASCII);
                        //System.out.println("Data: " + data);

                        // We are going to loop through the bytes
                        // When we see the start marker, we will start adding bytes to the buffer
                        // Once, we see the end marker, we will parse the buffer
                        // with String(byte[], offset, length, "ASCII") constructor
                        for (int i = 0; i < numRead; i++) {
                            char ch = (char) readBuffer[i];

                            if (ch == '#') {
                                buffer.setLength(0); // Clear previous data
                                markerFound = true;
                            } else if (ch == '*') {
                                markerFound = false;

                                // Update speed and distance moved
                                String[] data = buffer.toString().split(";");
                                if (data.length == 4) {
                                    float speed = Float.parseFloat(data[0]);
                                    float totalDistance = Float.parseFloat(data[1]);
                                    float kcalories = Float.parseFloat(data[2]);
                                    float wheelRadius = Float.parseFloat(data[3]);

                                    // Now calculate average speed
                                    this.speedQueue.add(speed);
                                    this.sumSpeed += speed;
                                    if (this.speedQueue.size() > 10) {
                                        this.sumSpeed -= this.speedQueue.poll();
                                    }

                                    float avgSpeed = 0;
                                    if (!this.speedQueue.isEmpty()) {
                                        avgSpeed = this.sumSpeed / this.speedQueue.size();
                                        avgSpeed = avgSpeed > 0 ? avgSpeed : 0;
                                    }

                                    // Round to last two decimal places
                                    avgSpeed = (float) (Math.round(avgSpeed * 100.0) / 100.0);

                                    NetworkManager.sendToServer(new PacketManager.ArduinoData(
                                            avgSpeed,
                                            totalDistance,
                                            kcalories,
                                            wheelRadius
                                    ));
                                }

                            } else if (markerFound) {
                                buffer.append(ch);
                            }
                        }

                    }
                }

            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}