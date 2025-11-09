package com.netQuiz.server.handlers;

import com.netQuiz.shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member 3 – Real-Time Chat Handler
 * Handles chat connections through unified server port
 * Runs as background service for persistent connections
 */
public class ChatHandler implements Runnable {
    private Set<ChatClientHandler> clients;
    private boolean running;

    public ChatHandler() {
        this.clients = ConcurrentHashMap.newKeySet();
        this.running = false;
    }

    @Override
    public void run() {
        running = true;
        System.out.println("[CHAT] Service started");
        
        // Keep service running
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void addClient(Socket socket, DataInputStream in, DataOutputStream out) {
        ChatClientHandler handler = new ChatClientHandler(socket, in, out);
        clients.add(handler);
        System.out.println("[CHAT] Added new client. Total clients now: " + clients.size());
        Thread thread = new Thread(handler);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        for (ChatClientHandler client : clients) {
            client.close();
        }
    }

    private void broadcast(Message message, ChatClientHandler sender) {
        System.out.println("[CHAT] Broadcasting from " + message.getSender() + ": " + message.getContent());
        System.out.println("[CHAT] Broadcasting to " + clients.size() + " clients");
        for (ChatClientHandler client : clients) {
            // Send to all clients including sender (client can filter if needed)
            System.out.println("[CHAT] Sending to client: " + client.username);
            client.sendMessage(message);
        }
    }

    private class ChatClientHandler implements Runnable {
        private Socket socket;
        private DataOutputStream out;
        private DataInputStream in;
        private String username;

        public ChatClientHandler(Socket socket, DataInputStream dataIn, DataOutputStream dataOut) {
            this.socket = socket;
            this.in = dataIn;
            this.out = dataOut;
        }

        @Override
        public void run() {
            try {
                // Read initial CONNECT command and username
                String command = in.readUTF();
                if ("CONNECT".equals(command)) {
                    username = in.readUTF();
                    System.out.println("[CHAT] User joined: " + username);
                    
                    // Broadcast join message
                    Message joinMessage = new Message(username + " has joined the chat", "Server", System.currentTimeMillis());
                    broadcast(joinMessage, this);
                }

                // Listen for messages
                while (running && !socket.isClosed()) {
                    try {
                        String msgCommand = in.readUTF();
                        System.out.println("[CHAT] Received command from " + username + ": " + msgCommand);
                        
                        if ("MESSAGE".equals(msgCommand)) {
                            String sender = in.readUTF();
                            String content = in.readUTF();
                            System.out.println("[CHAT] Message from " + sender + ": " + content);
                            Message message = new Message(sender, content, System.currentTimeMillis());
                            broadcast(message, this);
                        } else if ("DISCONNECT".equals(msgCommand)) {
                            break;
                        }
                    } catch (EOFException e) {
                        break;
                    }
                }

            } catch (IOException e) {
                if (running) {
                    System.err.println("[CHAT] Client error: " + e.getMessage());
                }
            } finally {
                cleanup();
            }
        }

        public void sendMessage(Message message) {
            try {
                if (out == null) {
                    System.err.println("[CHAT] ERROR: Output stream is null for " + username);
                    return;
                }
                if (socket == null || socket.isClosed()) {
                    System.err.println("[CHAT] ERROR: Socket is closed for " + username);
                    return;
                }
                
                synchronized (out) {
                    out.writeUTF(message.getSender());
                    out.writeUTF(message.getContent());
                    out.writeLong(message.getTimestamp());
                    out.flush();
                    System.out.println("[CHAT] Successfully sent message to " + username);
                }
            } catch (IOException e) {
                System.err.println("[CHAT] Error sending to " + username + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }

        private void cleanup() {
            clients.remove(this);
            if (username != null) {
                System.out.println("[CHAT] User left: " + username);
                Message leaveMessage = new Message("Server", username + " has left the chat", System.currentTimeMillis());
                broadcast(leaveMessage, this);
            }
            close();
        }
    }
}
