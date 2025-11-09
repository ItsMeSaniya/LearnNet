package com.netQuiz.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ChatServer - Multithreaded chat server that handles multiple clients
 * Port: 5004
 * Uses ExecutorService for thread management
 */
public class ChatServer {
    private static final int PORT = 5004; // Chat port
    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("Chat Server started on port " + PORT);
        System.out.println("=================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Broadcast a message to all connected clients except the sender
     * @param message The message to broadcast
     * @param sender The client who sent the message (will not receive it back)
     */
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Remove a client from the active clients list
     * @param client The client to remove
     */
    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("Client disconnected. Total clients: " + clientHandlers.size());
    }
}

