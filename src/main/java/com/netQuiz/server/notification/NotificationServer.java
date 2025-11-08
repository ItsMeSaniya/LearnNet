package com.netQuiz.server.notification;

import com.netQuiz.shared.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Member 5 – Notification System / UDP Broadcaster
 * Sends announcements via UDP broadcast packets.
 * Clients listen on a specific port to receive updates (e.g., "New quiz added").
 * Demonstrates connectionless, lightweight communication.
 */
public class NotificationServer implements Runnable {
    private DatagramSocket socket;
    private boolean running;
    private BlockingQueue<String> messageQueue;
    private InetAddress broadcastAddress;

    public NotificationServer() {
        this.running = false;
        this.messageQueue = new LinkedBlockingQueue<>();
        try {
            this.broadcastAddress = InetAddress.getByName("255.255.255.255");
        } catch (IOException e) {
            System.err.println("Error getting broadcast address: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            running = true;
            System.out.println("Notification Server started (UDP Broadcasting)");

            while (running) {
                try {
                    // Wait for messages to broadcast
                    String message = messageQueue.take();
                    broadcastMessage(message);
                } catch (InterruptedException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            System.err.println("Notification Server error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void broadcastMessage(String message) {
        try {
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
                                                       broadcastAddress, 
                                                       Constants.UDP_NOTIFICATION_PORT);
            socket.send(packet);
            System.out.println("Broadcast notification: " + message);
        } catch (IOException e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
        }
    }

    public void sendNotification(String message) {
        messageQueue.offer(message);
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void cleanup() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    // Convenience methods for common notifications
    public void notifyNewQuiz(String quizTitle) {
        sendNotification("NEW_QUIZ:" + quizTitle);
    }

    public void notifyNewFile(String fileName, String uploader) {
        sendNotification("NEW_FILE:" + fileName + " uploaded by " + uploader);
    }

    public void notifySystemMessage(String message) {
        sendNotification("SYSTEM:" + message);
    }
}
