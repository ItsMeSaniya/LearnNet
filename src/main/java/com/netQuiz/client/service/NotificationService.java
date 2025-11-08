package com.netQuiz.client.service;

import com.netQuiz.shared.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NotificationService {
    private DatagramSocket socket;
    private Thread listenerThread;
    private boolean running;
    private Consumer<String> notificationCallback;
    
    public void startListening() {
        try {
            socket = new DatagramSocket(Constants.UDP_NOTIFICATION_PORT);
            running = true;
            
            listenerThread = new Thread(this::listen);
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            System.out.println("Notification listener started on port " + Constants.UDP_NOTIFICATION_PORT);
        } catch (IOException e) {
            System.err.println("Error starting notification listener: " + e.getMessage());
        }
    }
    
    private void listen() {
        byte[] buffer = new byte[1024];
        
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                
                if (notificationCallback != null) {
                    notificationCallback.accept(message);
                }
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error receiving notification: " + e.getMessage());
                }
            }
        }
    }
    
    public void setNotificationCallback(Consumer<String> callback) {
        this.notificationCallback = callback;
    }
    
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
