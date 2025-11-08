package com.netQuiz.server.handlers;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member 4 – User Management Handler
 * Handles user login/logout and presence tracking
 */
public class UserHandler implements Runnable {
    private Map<Socket, UserConnection> onlineUsers;
    private boolean running;

    public UserHandler() {
        this.onlineUsers = new ConcurrentHashMap<>();
        this.running = false;
    }

    @Override
    public void run() {
        running = true;
        System.out.println("[USER] Service started");
        
        // Keep service running
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void handleRequest(Socket socket, DataInputStream in, DataOutputStream out) {
        try {
            String command = in.readUTF();
            System.out.println("[USER] Command: " + command);

            if (command.equals("LOGIN")) {
                String username = in.readUTF();
                String password = in.readUTF();
                
                System.out.println("[USER] Login request - Username: " + username);
                
                // Simple authentication (accept all for now)
                out.writeBoolean(true);
                out.flush();
                
                // Store user connection
                onlineUsers.put(socket, new UserConnection(username, in, out));
                
                // Start listener for this user
                Thread listenerThread = new Thread(() -> listenForUserRequests(socket));
                listenerThread.setDaemon(true);
                listenerThread.start();
                
                // Broadcast updated user list
                broadcastUserList();
                
            } else if (command.equals("GET_USERS")) {
                sendUserListCount(out);
            }

        } catch (IOException e) {
            System.err.println("[USER] Handler error: " + e.getMessage());
            handleDisconnect(socket);
        }
    }

    private void listenForUserRequests(Socket socket) {
        UserConnection conn = onlineUsers.get(socket);
        if (conn == null) return;

        try {
            while (running && !socket.isClosed()) {
                String message = conn.in.readUTF();
                
                if (message.equals("LOGOUT")) {
                    String username = conn.in.readUTF();
                    System.out.println("[USER] Logout request: " + username);
                    handleDisconnect(socket);
                    break;
                } else if (message.equals("GET_USERS")) {
                    sendUserListCount(conn.out);
                }
            }
        } catch (IOException e) {
            handleDisconnect(socket);
        }
    }
    
    private void sendUserListCount(DataOutputStream out) {
        try {
            List<String> users = new ArrayList<>(getOnlineUsers());
            out.writeInt(users.size());
            for (String user : users) {
                out.writeUTF(user);
            }
            out.flush();
        } catch (IOException e) {
            System.err.println("[USER] Error sending user list: " + e.getMessage());
        }
    }

    private void handleDisconnect(Socket socket) {
        UserConnection conn = onlineUsers.remove(socket);
        if (conn != null) {
            System.out.println("[USER] Logout: " + conn.username);
            broadcastUserList();
        }
        
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    private void broadcastUserList() {
        List<String> users = new ArrayList<>(getOnlineUsers());
        
        for (UserConnection conn : onlineUsers.values()) {
            try {
                synchronized (conn.out) {
                    conn.out.writeInt(users.size());
                    for (String user : users) {
                        conn.out.writeUTF(user);
                    }
                    conn.out.flush();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public void stop() {
        running = false;
        for (Socket socket : onlineUsers.keySet()) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public Set<String> getOnlineUsers() {
        Set<String> users = new HashSet<>();
        for (UserConnection conn : onlineUsers.values()) {
            users.add(conn.username);
        }
        return users;
    }

    private static class UserConnection {
        String username;
        DataInputStream in;
        DataOutputStream out;

        UserConnection(String username, DataInputStream in, DataOutputStream out) {
            this.username = username;
            this.in = in;
            this.out = out;
        }
    }
}
