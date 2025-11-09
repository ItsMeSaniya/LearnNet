package com.netQuiz.server.notification;

import com.netQuiz.shared.Constants;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NotificationServer implements Runnable {
    private DatagramSocket socket;
    private boolean running;
    private BlockingQueue<String> messageQueue;
    private InetAddress broadcastAddress;

    public NotificationServer() {
        this.running = false;
        this.messageQueue = new LinkedBlockingQueue<>();
        try {
            this.broadcastAddress = getBroadcastAddress();
            System.out.println("[NOTIFY SERVER] Using broadcast address: " + broadcastAddress);
        } catch (IOException e) {
            System.err.println("[NOTIFY SERVER] Error getting broadcast address: " + e.getMessage());
            try {
                this.broadcastAddress = InetAddress.getByName("255.255.255.255");
                System.out.println("[NOTIFY SERVER] Fallback broadcast: 255.255.255.255");
            } catch (Exception ignored) {
            }
        }
    }

    private InetAddress getBroadcastAddress() throws IOException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isUp() && !ni.isLoopback()) {
                for (InterfaceAddress address : ni.getInterfaceAddresses()) {
                    InetAddress broadcast = address.getBroadcast();
                    if (broadcast != null)
                        return broadcast;
                }
            }
        }
        return InetAddress.getByName("255.255.255.255");
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(Constants.UDP_NOTIFICATION_PORT));
            socket.setBroadcast(true);

            running = true;
            System.out.println("[NOTIFY SERVER] Started UDP broadcaster on port " + Constants.UDP_NOTIFICATION_PORT);

            sendNotification("SERVER:Notification server started");

            while (running) {
                try {
                    String message = messageQueue.take();
                    broadcastMessage(message);
                } catch (InterruptedException ignored) {
                }
            }

        } catch (IOException e) {
            System.err.println("[NOTIFY SERVER] Socket error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void broadcastMessage(String message) {
        try {
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);

            // Force broadcast to all clients on local network
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length,
                    InetAddress.getByName("255.255.255.255"),
                    Constants.UDP_NOTIFICATION_PORT);

            System.out.println("[NOTIFY SERVER] Broadcasting → " + message);
            socket.send(packet);

        } catch (IOException e) {
            System.err.println("[NOTIFY SERVER] Failed to broadcast: " + e.getMessage());
        }
    }

    public void sendNotification(String message) {
        messageQueue.offer(message);
    }

    public void notifyNewQuiz(String quizTitle, String creator) {
        sendNotification("NEW_QUIZ:" + quizTitle + " by " + creator);
    }

    public void notifyNewFile(String fileName, String uploader) {
        sendNotification("NEW_FILE:" + fileName + " uploaded by " + uploader);
    }

    public void notifySystemMessage(String message) {
        sendNotification("SYSTEM:" + message);
    }

    public void notifyUserLogin(String username) {
        sendNotification("USER_LOGIN:" + username);
    }

    public void notifyUserLogout(String username) {
        sendNotification("USER_LOGOUT:" + username);
    }

    public void testBroadcast() {
        sendNotification("TEST:Hello from server at " + System.currentTimeMillis());
    }

    private void cleanup() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("[NOTIFY SERVER] Shutdown complete.");
    }
}
