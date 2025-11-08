package com.netQuiz.server;

import java.io.*;
import java.net.*;

/**
 * ClientHandler - Handles communication with a single client
 * Implements Runnable to run in a separate thread
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Request username from client
            out.println("Enter your username:");
            username = in.readLine();

            if (username == null || username.trim().isEmpty()) {
                username = "Anonymous";
            }

            System.out.println(username + " has joined the chat");
            ChatServer.broadcast(username + " has joined the chat", this);

            // Read and broadcast messages
            String message;
            while ((message = in.readLine()) != null) {
                if (message.trim().isEmpty()) {
                    continue;
                }
                System.out.println(username + ": " + message);
                ChatServer.broadcast(username + ": " + message, this);
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Send a message to this client
     * @param message The message to send
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Clean up resources when client disconnects
     */
    private void cleanup() {
        try {
            ChatServer.removeClient(this);
            if (username != null) {
                ChatServer.broadcast(username + " has left the chat", this);
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}

