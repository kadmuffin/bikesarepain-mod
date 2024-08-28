package com.kadmuffin.bikesarepain.client;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
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

public class SerialReader {
    // private final AtomicBoolean running = new AtomicBoolean(false);
    private final StringBuilder buffer = new StringBuilder();
    private boolean markerFound = false;
    private SerialPort serialPort;
    private Queue<Float> speedQueue;
    private float sumSpeed = 0;

    private float lastSpeed = 0;
    private float lastDistance = 0;
    private float lastKcalories = 0;
    private float lastWheelCircumference = 0;
    private float scaleBlock = 1;
    private float scaleMeter = 1;
    private float scaleFactor = 1;

    public SerialPort getSerial() {
        return this.serialPort;
    }

    public void setScaleFactor(float scaleBlock, float scaleMeter) {
        this.scaleBlock = scaleBlock;
        this.scaleMeter = scaleMeter;
        this.scaleFactor = scaleBlock / scaleMeter;
    }

    public float getScaleFactor() {
        return this.scaleFactor;
    }

    public String getScaleFactorString() {
        return this.scaleBlock + " block is " + this.scaleMeter + " meter";
    }

    public void setSerial(String port) {
        if (this.serialPort != null && this.serialPort.isOpen()) {
            this.serialPort.closePort();
        }


        this.serialPort = SerialPort.getCommPort(port);
        this.serialPort.setComPortParameters(31250, 8, 1, 0);
        this.serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        this.serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;

                if (serialPort.isOpen()) {
                    if (serialPort.bytesAvailable() > 0) {
                        byte[] readBuffer = new byte[serialPort.bytesAvailable()];
                        int numRead = serialPort.readBytes(readBuffer, readBuffer.length);

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
                                    float wheelCircumference = Float.parseFloat(data[3]);

                                    // Now calculate average speed
                                    speedQueue.add(speed);
                                    sumSpeed += speed;
                                    if (speedQueue.size() > 3) {
                                        sumSpeed -= speedQueue.poll();
                                    }

                                    float avgSpeed = 0;
                                    if (!speedQueue.isEmpty()) {
                                        avgSpeed = sumSpeed / speedQueue.size();
                                        avgSpeed = avgSpeed > 0 ? avgSpeed : 0;
                                    }

                                    // Round to last two decimal places
                                    avgSpeed = (float) (Math.round(avgSpeed * 100.0) / 100.0);

                                    if (avgSpeed != lastSpeed || totalDistance != lastDistance || kcalories != lastKcalories || wheelCircumference != lastWheelCircumference) {
                                        lastSpeed = avgSpeed;
                                        lastDistance = totalDistance;
                                        lastKcalories = kcalories;
                                        lastWheelCircumference = wheelCircumference;

                                        updateServerData(true);
                                    }
                                }

                            } else if (markerFound) {
                                buffer.append(ch);
                            }
                        }

                    }
                }
            }
        });
    }

    public boolean start() {
        if (this.serialPort == null) {
            return false;
        }
        this.updateServerData(true);
        this.serialPort.openPort();
        this.speedQueue = new LinkedList<>();
        //Thread worker = new Thread(this);
        //worker.start();
        return true;
    }

    public boolean stop() {
        //running.set(false);
        if (this.serialPort == null) {
            return false;
        }
        updateServerData(false);
        this.serialPort.closePort();
        return true;
    }

    public void updateServerData(boolean enabled) {
        if (this.serialPort == null) {
            return;
        }

        NetworkManager.sendToServer(new PacketManager.ArduinoData(
                enabled,
                lastSpeed,
                lastDistance,
                lastKcalories,
                lastWheelCircumference,
                scaleFactor
        ));
    }

    public String[] getPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }
}