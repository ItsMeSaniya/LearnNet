package com.netQuiz.client.service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import com.netQuiz.shared.Constants;

public class NotificationService {
    private DatagramSocket socket;
    private Thread listenerThread;
    private boolean running = false;
    private Consumer<String> notificationCallback;

    public synchronized void startListening() {
        if (running)
            return;

        try {
            // Bind to the same port server broadcasts to
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(Constants.UDP_NOTIFICATION_PORT));
            socket.setBroadcast(true);

            running = true;
            listenerThread = new Thread(this::listen, "NotificationListener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            System.out.println("[CLIENT NOTIFY] Listening for UDP Broadcasts...");

        } catch (Exception e) {
            System.err.println("[CLIENT NOTIFY] Failed to start: " + e.getMessage());
        }
    }

    private void listen() {
        byte[] buffer = new byte[1024];
        System.out.println("[CLIENT NOTIFY] Ready to receive broadcast messages...");

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("[CLIENT NOTIFY] Received: " + message);

                if (notificationCallback != null) {
                    notificationCallback.accept(message);
                }
            } catch (IOException e) {
                if (running)
                    System.err.println("[CLIENT NOTIFY] Error receiving: " + e.getMessage());
            }
        }

        System.out.println("[CLIENT NOTIFY] Listener stopped.");
    }

    public void setNotificationCallback(Consumer<String> callback) {
        this.notificationCallback = callback;
    }

    public synchronized void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("[CLIENT NOTIFY] Stopped listening.");
    }
}
