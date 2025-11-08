package com.netQuiz.client.service;

import com.netQuiz.shared.Constants;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserService {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread listenerThread;
    private Consumer<List<String>> userListHandler;
    private Consumer<String> messageHandler;  // For receiving chat messages
    private volatile boolean running = false;
    
    public boolean login(String username, String password, Consumer<List<String>> userListHandler) 
            throws IOException {
        return login(username, password, userListHandler, null);
    }

    public boolean login(String username, String password, Consumer<List<String>> userListHandler,
                        Consumer<String> messageHandler) throws IOException {
        this.userListHandler = userListHandler;
        this.messageHandler = messageHandler;
        socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        
        // Send request type
        out.writeUTF(Constants.USER_REQUEST);
        out.writeUTF("LOGIN");
        out.writeUTF(username);
        out.writeUTF(password);
        out.flush();
        
        boolean success = in.readBoolean();
        String welcomeMessage = in.readUTF();

        if (success && messageHandler != null) {
            messageHandler.accept(welcomeMessage);
        }

        if (success) {
            running = true;
            listenerThread = new Thread(this::listenForUpdates);
            listenerThread.setDaemon(true);
            listenerThread.start();
        }
        
        return success;
    }
    
    /**
     * Send a chat message or command to the server
     * Supports: regular messages, /msg username message, /users, /help
     */
    public void sendMessage(String message) throws IOException {
        if (out != null && running) {
            out.writeUTF(message);
            out.flush();
        }
    }

    public void logout(String username) {
        running = false;
        
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        
        try {
            if (out != null) {
                out.writeUTF("LOGOUT");
                out.writeUTF(username);
                out.flush();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        closeConnection();
    }
    
    public List<String> getOnlineUsers() throws IOException {
        try (Socket tempSocket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream tempOut = new DataOutputStream(tempSocket.getOutputStream());
             DataInputStream tempIn = new DataInputStream(tempSocket.getInputStream())) {
            
            // Send request type
            tempOut.writeUTF(Constants.USER_REQUEST);
            tempOut.writeUTF("GET_USERS");
            tempOut.flush();
            
            int count = tempIn.readInt();
            List<String> users = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                users.add(tempIn.readUTF());
            }
            
            return users;
        }
    }
    
    private void listenForUpdates() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                String messageType = in.readUTF();

                switch (messageType) {
                    case "USER_LIST":
                        // Handle user list update
                        int count = in.readInt();
                        List<String> users = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            users.add(in.readUTF());
                        }
                        if (userListHandler != null) {
                            userListHandler.accept(users);
                        }
                        break;

                    case "MESSAGE":
                    case "CHAT_MSG":
                    case "SYSTEM_MSG":
                    case "PRIVATE_MSG":
                    case "ERROR":
                        // Handle chat messages
                        String message = in.readUTF();
                        if (messageHandler != null) {
                            messageHandler.accept(message);
                        }
                        break;

                    case "HELP":
                        // Handle help messages (multiple lines)
                        StringBuilder helpText = new StringBuilder();
                        // Read the help messages until we get another message type or error
                        try {
                            while (true) {
                                String line = in.readUTF();
                                // Check if this might be a new message type
                                if (line.equals("USER_LIST") || line.equals("MESSAGE") ||
                                    line.equals("CHAT_MSG") || line.equals("SYSTEM_MSG")) {
                                    // This is a new message, handle it recursively
                                    handleMessage(line);
                                    break;
                                }
                                helpText.append(line).append("\n");
                            }
                        } catch (EOFException e) {
                            // End of help text
                        }
                        if (messageHandler != null && helpText.length() > 0) {
                            messageHandler.accept(helpText.toString());
                        }
                        break;

                    default:
                        System.err.println("[USER] Unknown message type: " + messageType);
                        break;
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("User update listener error: " + e.getMessage());
            }
        }
    }
    
    private void handleMessage(String messageType) throws IOException {
        switch (messageType) {
            case "USER_LIST":
                int count = in.readInt();
                List<String> users = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    users.add(in.readUTF());
                }
                if (userListHandler != null) {
                    userListHandler.accept(users);
                }
                break;

            case "CHAT_MSG":
            case "SYSTEM_MSG":
            case "MESSAGE":
                String message = in.readUTF();
                if (messageHandler != null) {
                    messageHandler.accept(message);
                }
                break;
        }
    }

    private void closeConnection() {
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            // Ignore
        }
        
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            // Ignore
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }
}
