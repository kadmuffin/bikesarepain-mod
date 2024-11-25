package com.kadmuffin.bikesarepain.client;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.kadmuffin.bikesarepain.client.serial.SerialParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.kadmuffin.bikesarepain.BikesArePain.LOGGER;

@Environment(EnvType.CLIENT)
public class SerialReader {
    private final SerialParser parser;
    private final List<TriConsumer<Float, Double, Float>> listeners;
    private final List<Consumer<Event>> eventListeners;
    private SerialPort port;

    public SerialReader() {
        listeners = new ArrayList<>();
        eventListeners = new ArrayList<>();

        parser = new SerialParser(
                '#', '*',
                () -> port
        );

        parser.addParser((rawString) -> {
            String[] pieces = rawString.split(";");

            if (pieces.length != 3) return;

            try {
                float speed = Float.parseFloat(pieces[0]);
                // pieces[1] is in miliseconds, convert to hours
                double triggerTimeHours = Double.parseDouble(pieces[1]) / 3600000;
                float wheelRadius = Float.parseFloat(pieces[2]);

                for (TriConsumer<Float, Double, Float> listener : listeners) {
                    listener.accept(speed, triggerTimeHours, wheelRadius);
                }

            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse serial data: {}", rawString);
            }
        });
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

    public void addListener(TriConsumer<Float, Double, Float> listener) {
        listeners.add(listener);
    }

    public void addEventListener(Consumer<Event> listener) {
        eventListeners.add(listener);
    }

    public void removeListener(TriConsumer<Float, Double, Float> listener) {
        listeners.remove(listener);
    }

    public void removeEventListener(Consumer<Event> listener) {
        eventListeners.remove(listener);
    }

    public void setSerial() {
        if (this.port != null && this.port.isOpen()) {
            this.port.closePort();
        }

        String portName = ClientConfig.CONFIG.instance().getPort();
        int baudRate = ClientConfig.CONFIG.instance().getBaudRate();
        this.port = SerialPort.getCommPort(portName);
        this.port.setComPortParameters(baudRate, 8, 1, 0);
        this.port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

        this.port.addDataListener(this.parser);

        // Check for disconnect
        this.port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    for (Consumer<Event> listener : eventListeners) {
                        listener.accept(Event.SUDDEN_DISCONNECT);
                    }
                }
            }
        });
    }

    public boolean start() {
        if (this.port == null) {
            return false;
        }
        this.port.openPort();
        for (Consumer<Event> listener : eventListeners) {
            listener.accept(Event.START);
        }

        return true;
    }

    public void stop() {
        if (this.port == null) {
            return;
        }

        for (Consumer<Event> listener : eventListeners) {
            listener.accept(Event.STOP);
        }

        this.port.closePort();
    }

    public enum Event {
        SUDDEN_DISCONNECT,
        START,
        STOP
    }
}