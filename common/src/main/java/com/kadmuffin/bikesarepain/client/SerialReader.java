package com.kadmuffin.bikesarepain.client;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.kadmuffin.bikesarepain.packets.PacketManager;
import dev.architectury.networking.NetworkManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SerialReader {
    private final StringBuilder buffer = new StringBuilder();
    private boolean markerFound = false;
    private SerialPort serialPort;
    private Queue<Float> speedQueue;
    private float sumSpeed = 0;

    private float lastSpeed = 0;
    private float lastDistance = 0;
    private float lastKcalories = 0;
    private float lastWheelRadius = 0;
    private float scaleBlockWheel = 1;
    private float scaleMeterWheel = 1;
    private float scaleBlockSpeed = 1;
    private float scaleMeterSpeed = 1;

    public SerialPort getSerial() {
        return this.serialPort;
    }

    public void setScaleFactor(float scaleBlock, float scaleMeter, int applyScaleTo) {
        switch (applyScaleTo) {
            case 0:
                this.scaleBlockSpeed = scaleBlock;
                this.scaleMeterSpeed = scaleMeter;
                this.scaleBlockWheel = scaleBlock;
                this.scaleMeterWheel = scaleMeter;
                float scale = scaleBlock / scaleMeter;
                ClientConfig.CONFIG.instance().setSpeedScaleRatio(scale);
                ClientConfig.CONFIG.instance().setWheelScaleRatio(scale);
                break;
            case 1:
                this.scaleBlockSpeed = scaleBlock;
                this.scaleMeterSpeed = scaleMeter;
                ClientConfig.CONFIG.instance().setSpeedScaleRatio(scaleBlock / scaleMeter);
                break;
            case 2:
                this.scaleBlockWheel = scaleBlock;
                this.scaleMeterWheel = scaleMeter;
                ClientConfig.CONFIG.instance().setWheelScaleRatio(scaleBlock / scaleMeter);
                break;
        }
        ClientConfig.CONFIG.save();

    }

    public String getScaleFactorString() {
        return String.format("Speed is set to %.2f block is %.2f meters; Wheel is set to %.2f block is %.2f meters.", this.scaleBlockSpeed, this.scaleMeterSpeed, this.scaleBlockWheel, this.scaleMeterWheel);
    }

    public void setSerial() {
        if (this.serialPort != null && this.serialPort.isOpen()) {
            this.serialPort.closePort();
        }

        this.serialPort = SerialPort.getCommPort(ClientConfig.CONFIG.instance().getPort());
        this.serialPort.setComPortParameters(ClientConfig.CONFIG.instance().getBaudRate(), 8, 1, 0);
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
                                    float wheelRadius = Float.parseFloat(data[3]);

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

                                    if (avgSpeed != lastSpeed || totalDistance != lastDistance || kcalories != lastKcalories || lastWheelRadius != wheelRadius) {
                                        lastSpeed = avgSpeed;
                                        lastDistance = totalDistance;
                                        lastKcalories = kcalories;
                                        lastWheelRadius = wheelRadius;

                                        updateServerData(true, false, false);
                                    } else {
                                        updateServerData(true, true, false);
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

        // Check for disconnect
        this.serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    updateServerData(false, true, true);
                }
            }
        });
    }

    public boolean start() {
        if (this.serialPort == null) {
            return false;
        }
        this.updateServerData(true, true, false);
        this.serialPort.openPort();
        this.speedQueue = new LinkedList<>();
        return true;
    }

    public void stop() {
        if (this.serialPort == null) {
            return;
        }
        updateServerData(false, true, false);
        this.serialPort.closePort();
    }

    public void updateServerData(boolean enabled, boolean empty, boolean disconnect) {
        if (this.serialPort == null) {
            return;
        }

        if (empty) {
            NetworkManager.sendToServer(new PacketManager.EmptyArduinoData(enabled, disconnect));
            return;
        }

        NetworkManager.sendToServer(new PacketManager.ArduinoData(
                enabled,
                lastSpeed,
                lastDistance,
                lastKcalories,
                lastWheelRadius,
                ClientConfig.CONFIG.instance().getWheelScaleRatio(),
                ClientConfig.CONFIG.instance().getSpeedScaleRatio()
        ));
    }

    public static List<String> getPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portNames = new ArrayList<>();
        for (SerialPort port : ports) {
            portNames.add(port.getSystemPortName());
        }

        return portNames;
    }

    // COMX: NAME
    public static List<String> getPortsNamed() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portNames = new ArrayList<>();
        for (SerialPort port : ports) {
            // COMX: NAME [Name is limited to 35 characters]
            String name = port.getPortDescription();
            if (name.length() > 35) {
                name = name.substring(0, 35);
                name += "...";
            }
            portNames.add(port.getSystemPortName() + ": " + (name.isEmpty() ? "Unknown" : name));
        }

        return portNames;
    }
}