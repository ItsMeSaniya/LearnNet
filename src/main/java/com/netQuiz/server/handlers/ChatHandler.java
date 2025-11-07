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
        for (ChatClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private class ChatClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ChatClientHandler(Socket socket, DataInputStream dataIn, DataOutputStream dataOut) {
            this.socket = socket;
            try {
                this.out = new ObjectOutputStream(dataOut);
                this.in = new ObjectInputStream(dataIn);
            } catch (IOException e) {
                System.err.println("[CHAT] Error creating streams: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // Read username
                Message loginMessage = (Message) in.readObject();
                if (loginMessage.getType() == Message.MessageType.LOGIN) {
                    username = loginMessage.getSender();
                    System.out.println("[CHAT] User joined: " + username);
                    
                    // Broadcast join message
                    Message joinMessage = new Message(Message.MessageType.CHAT, "Server", 
                                                     username + " has joined the chat");
                    broadcast(joinMessage, this);
                }

                // Listen for messages
                while (running) {
                    try {
                        Message message = (Message) in.readObject();
                        
                        if (message.getType() == Message.MessageType.CHAT) {
                            broadcast(message, this);
                        } else if (message.getType() == Message.MessageType.LOGOUT) {
                            break;
                        }
                    } catch (EOFException e) {
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[CHAT] Client error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        public void sendMessage(Message message) {
            try {
                synchronized (out) {
                    out.writeObject(message);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("[CHAT] Error sending to " + username + ": " + e.getMessage());
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
                Message leaveMessage = new Message(Message.MessageType.CHAT, "Server", 
                                                   username + " has left the chat");
                broadcast(leaveMessage, this);
            }
            close();
        }
    }
}
