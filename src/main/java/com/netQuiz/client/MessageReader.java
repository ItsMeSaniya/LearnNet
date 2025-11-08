package com.netQuiz.client;

import java.io.*;
import java.net.*;

/**
 * MessageReader - Reads messages from the server in a separate thread
 * Implements Runnable to allow concurrent reading and writing
 */
public class MessageReader implements Runnable {
    private Socket socket;

    public MessageReader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println("Connection to server lost: " + e.getMessage());
        }
    }
}

