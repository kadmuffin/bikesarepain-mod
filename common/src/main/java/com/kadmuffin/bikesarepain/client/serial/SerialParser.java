package com.kadmuffin.bikesarepain.client.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SerialParser implements SerialPortDataListener {
    private final char startMarker;
    private final char endMarker;
    private final List<Consumer<String>> parsers;
    private final Supplier<SerialPort> serialPortSupplier;
    private final StringBuilder buffer;
    private boolean markerFound = false;

    public SerialParser(char startMarker, char endMarker, Supplier<SerialPort> serialPortSupplier) {
        this.startMarker = startMarker;
        this.endMarker = endMarker;

        parsers = new ArrayList<>();
        buffer = new StringBuilder();

        this.serialPortSupplier = serialPortSupplier;
    }

    public void reset() {
        this.buffer.setLength(0);
        this.markerFound = false;
    }

    public void addParser(Consumer<String> listener) {
        parsers.add(listener);
    }

    public void removeParser(Consumer<String> listener) {
        parsers.remove(listener);
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

        SerialPort port = this.serialPortSupplier.get();

        if (!port.isOpen()) return;
        if (port.bytesAvailable() <= 0) return;

        byte[] readBuffer = new byte[port.bytesAvailable()];
        int numRead = port.readBytes(readBuffer, readBuffer.length);

        for (int i = 0; i < numRead; i++) {
            char ch = (char) readBuffer[i];

            if (ch == startMarker) {
                buffer.setLength(0); // Clear previous data
                markerFound = true;
            } else if (ch == endMarker) {
                markerFound = false;
                String data = buffer.toString();
                for (Consumer<String> parser : parsers) {
                    parser.accept(data);
                }
            } else if (markerFound) {
                buffer.append(ch);
            }
        }
    }
}
