package com.netQuiz.server.handlers;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member 4 – User Management Handler
 * Handles user login/logout, presence tracking, and chat messaging
 * Phase 2: Enhanced with private messaging and chat features
 */
public class UserHandler implements Runnable {
    private Map<String, UserConnection> onlineUsers;
    private Map<Socket, String> socketToUsername;
    private boolean running;

    public UserHandler() {
        this.onlineUsers = new ConcurrentHashMap<>();
        this.socketToUsername = new ConcurrentHashMap<>();
        this.running = false;
    }

    @Override
    public void run() {
        running = true;
        System.out.println("[USER] Service started with Phase 2 chat features");
        System.out.println("[USER] Features: Private messaging, user list, join/leave notifications");

        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Main request handler called by NetQuizServer
     */
    public void handleRequest(Socket socket, DataInputStream in, DataOutputStream out) {
        try {
            String command = in.readUTF();
            System.out.println("[USER] Command received: " + command);

            switch (command) {
                case "LOGIN":
                    handleLogin(socket, in, out);
                    break;
                case "GET_USERS":
                    sendOnlineUsersList(out);
                    break;
                case "LOGOUT":
                    handleLogout(socket);
                    break;
                default:
                    System.out.println("[USER] Unknown command: " + command);
                    break;
            }

        } catch (IOException e) {
            System.err.println("[USER] Request handler error: " + e.getMessage());
            handleDisconnect(socket);
        }
    }

    /**
     * Handle user login with duplicate username check
     */
    private void handleLogin(Socket socket, DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();

        System.out.println("[USER] Login attempt - Username: " + username);

        // Check for duplicate username
        if (onlineUsers.containsKey(username)) {
            System.out.println("[USER] Login rejected - Username already taken: " + username);
            out.writeBoolean(false);
            out.writeUTF("Username '" + username + "' is already taken. Please choose another.");
            out.flush();
            return;
        }

        // Accept login
        System.out.println("[USER] Login successful: " + username);
        out.writeBoolean(true);
        out.writeUTF("Welcome to NetQuiz Chat, " + username + "!");
        out.flush();

        // Create user connection
        UserConnection userConn = new UserConnection(username, socket, in, out);
        onlineUsers.put(username, userConn);
        socketToUsername.put(socket, username);

        System.out.println("[USER] Total online users: " + onlineUsers.size());

        // Broadcast join notification to all other users
        broadcastSystemMessage(username + " has joined the chat", username);

        // Send current user list to new user
        sendUserListToUser(out);

        // Broadcast updated user list to everyone
        broadcastUserList();

        // Start message listener thread for this user
        startMessageListener(socket, username);
    }

    /**
     * Start a dedicated thread to listen for messages from this user
     */
    private void startMessageListener(Socket socket, String username) {
        Thread listenerThread = new Thread(() -> {
            System.out.println("[USER] Message listener started for: " + username);
            listenForMessages(socket, username);
        }, "Listener-" + username);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Listen for incoming messages and commands from a user
     */
    private void listenForMessages(Socket socket, String username) {
        UserConnection conn = onlineUsers.get(username);
        if (conn == null) {
            System.err.println("[USER] Connection not found for: " + username);
            return;
        }

        try {
            while (running && !socket.isClosed()) {
                String message = conn.in.readUTF();

                System.out.println("[USER] Received from " + username + ": " + message);

                if (message.equals("LOGOUT")) {
                    System.out.println("[USER] Logout request from: " + username);
                    handleLogout(socket);
                    break;

                } else if (message.startsWith("/msg ")) {
                    // Private message command
                    handlePrivateMessage(message, username, conn.out);

                } else if (message.equals("/users")) {
                    // Show user list command
                    sendUserListToUser(conn.out);

                } else if (message.equals("/help")) {
                    // Help command
                    sendHelpMessage(conn.out);

                } else {
                    // Regular chat message - broadcast to all
                    System.out.println("[CHAT] Broadcasting from " + username + ": " + message);
                    broadcastChatMessage(username + ": " + message, username);
                }
            }
        } catch (IOException e) {
            System.err.println("[USER] Listener error for " + username + ": " + e.getMessage());
            handleDisconnect(socket);
        }
    }

    /**
     * Handle private message command: /msg username message
     */
    private void handlePrivateMessage(String command, String sender, DataOutputStream senderOut) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            sendDirectMessage(senderOut, "ERROR", "Usage: /msg <username> <message>");
            return;
        }

        String targetUsername = parts[1];
        String privateMsg = parts[2];

        UserConnection target = onlineUsers.get(targetUsername);

        if (target != null) {
            // Send to recipient
            String formattedMsg = "[Private from " + sender + "]: " + privateMsg;
            sendDirectMessage(target.out, "PRIVATE_MSG", formattedMsg);

            // Confirm to sender
            String confirmation = "[Private to " + targetUsername + "]: " + privateMsg;
            sendDirectMessage(senderOut, "PRIVATE_MSG", confirmation);

            System.out.println("[PRIVATE] " + sender + " -> " + targetUsername + ": " + privateMsg);
        } else {
            sendDirectMessage(senderOut, "ERROR", "User '" + targetUsername + "' not found");
        }
    }

    /**
     * Send help message with available commands
     */
    private void sendHelpMessage(DataOutputStream out) {
        try {
            synchronized (out) {
                out.writeUTF("HELP");
                out.writeUTF("=== Available Commands ===");
                out.writeUTF("/msg <username> <message> - Send a private message");
                out.writeUTF("/users - Show list of online users");
                out.writeUTF("/help - Show this help message");
                out.writeUTF("Type any other text to send a public message");
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("[USER] Error sending help: " + e.getMessage());
        }
    }

    /**
     * Send user list to a specific user
     */
    private void sendUserListToUser(DataOutputStream out) {
        try {
            List<String> users = new ArrayList<>(onlineUsers.keySet());
            synchronized (out) {
                out.writeUTF("USER_LIST");
                out.writeInt(users.size());
                for (String user : users) {
                    out.writeUTF(user);
                }
                out.flush();
            }
            System.out.println("[USER] Sent user list (" + users.size() + " users)");
        } catch (IOException e) {
            System.err.println("[USER] Error sending user list: " + e.getMessage());
        }
    }

    /**
     * Send online users list (for GET_USERS command)
     */
    private void sendOnlineUsersList(DataOutputStream out) throws IOException {
        List<String> users = new ArrayList<>(onlineUsers.keySet());
        out.writeInt(users.size());
        for (String user : users) {
            out.writeUTF(user);
        }
        out.flush();
    }

    /**
     * Broadcast chat message to all users except sender
     */
    private void broadcastChatMessage(String message, String sender) {
        int sentCount = 0;
        for (Map.Entry<String, UserConnection> entry : onlineUsers.entrySet()) {
            if (!entry.getKey().equals(sender)) {
                sendDirectMessage(entry.getValue().out, "CHAT_MSG", message);
                sentCount++;
            }
        }
        System.out.println("[CHAT] Broadcast to " + sentCount + " users");
    }

    /**
     * Broadcast system message (join/leave notifications)
     */
    private void broadcastSystemMessage(String message, String excludeUser) {
        System.out.println("[SYSTEM] Broadcasting: " + message);
        for (Map.Entry<String, UserConnection> entry : onlineUsers.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                sendDirectMessage(entry.getValue().out, "SYSTEM_MSG", message);
            }
        }
    }

    /**
     * Broadcast updated user list to all connected users
     */
    private void broadcastUserList() {
        List<String> users = new ArrayList<>(onlineUsers.keySet());
        System.out.println("[USER] Broadcasting user list to all (" + users.size() + " users)");

        for (UserConnection conn : onlineUsers.values()) {
            try {
                synchronized (conn.out) {
                    conn.out.writeUTF("USER_LIST");
                    conn.out.writeInt(users.size());
                    for (String user : users) {
                        conn.out.writeUTF(user);
                    }
                    conn.out.flush();
                }
            } catch (IOException e) {
                // Connection lost, will be handled by listener
            }
        }
    }

    /**
     * Send a direct message to a specific user
     */
    private void sendDirectMessage(DataOutputStream out, String messageType, String message) {
        try {
            synchronized (out) {
                out.writeUTF(messageType);
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            // Connection lost, will be handled by listener
        }
    }

    /**
     * Handle user logout
     */
    private void handleLogout(Socket socket) {
        handleDisconnect(socket);
    }

    /**
     * Handle user disconnect (logout or connection lost)
     */
    private void handleDisconnect(Socket socket) {
        String username = socketToUsername.remove(socket);

        if (username != null) {
            UserConnection conn = onlineUsers.remove(username);

            if (conn != null) {
                System.out.println("[USER] " + username + " has left the chat");
                System.out.println("[USER] Total online users: " + onlineUsers.size());

                // Broadcast leave notification
                broadcastSystemMessage(username + " has left the chat", username);

                // Broadcast updated user list
                broadcastUserList();
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Stop the handler and disconnect all users
     */
    public void stop() {
        System.out.println("[USER] Stopping user handler...");
        running = false;

        for (UserConnection conn : onlineUsers.values()) {
            try {
                conn.socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        onlineUsers.clear();
        socketToUsername.clear();
        System.out.println("[USER] All users disconnected");
    }

    /**
     * Get set of currently online usernames
     */
    public Set<String> getOnlineUsers() {
        return new HashSet<>(onlineUsers.keySet());
    }

    /**
     * Get number of online users
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * Inner class to store user connection details
     */
    private static class UserConnection {
        final String username;
        final Socket socket;
        final DataInputStream in;
        final DataOutputStream out;

        UserConnection(String username, Socket socket, DataInputStream in, DataOutputStream out) {
            this.username = username;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
    }
}

