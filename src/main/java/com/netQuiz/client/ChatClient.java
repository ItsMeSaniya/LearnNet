package com.netQuiz.client;

import java.io.*;
import java.net.*;

/**
 * ChatClient - Console-based chat client
 * Connects to ChatServer and enables real-time messaging
 * Uses two threads: one for reading, one for writing
 */
public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5002;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("Connecting to Chat Server...");
        System.out.println("=================================");

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to chat server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("=================================\n");

            // Start thread for reading messages from server
            Thread readerThread = new Thread(new MessageReader(socket));
            readerThread.setDaemon(true);
            readerThread.start();

            // Main thread handles writing messages to server
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String input;
            while ((input = consoleReader.readLine()) != null) {
                out.println(input);

                // Exit if user types "exit" or "quit"
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("Disconnecting from server...");
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Chat client closed.");
    }
}

