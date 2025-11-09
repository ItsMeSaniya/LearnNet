package com.netQuiz.server.handlers;

import com.netQuiz.shared.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member 3 – Real-Time Chat Handler
 * Runs as dedicated server on port 5004 using object streams
 * No routing needed - direct object stream communication
 */
public class ChatHandler implements Runnable {
    private Set<ChatClientHandler> clients;
    private boolean running;
    private ServerSocket chatServerSocket;

    public ChatHandler() {
        this.clients = ConcurrentHashMap.newKeySet();
        this.running = false;
    }

    @Override
    public void run() {
        running = true;
        
        try {
            // Start dedicated chat server on port 5004
            chatServerSocket = new ServerSocket(com.netQuiz.shared.Constants.CHAT_PORT);
            System.out.println("[CHAT] Dedicated chat server started on port " + 
                             com.netQuiz.shared.Constants.CHAT_PORT);
            
            // Accept chat connections
            while (running) {
                try {
                    Socket socket = chatServerSocket.accept();
                    System.out.println("[CHAT] New chat connection from " + 
                                     socket.getInetAddress().getHostAddress());
                    addClient(socket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[CHAT] Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[CHAT] Failed to start chat server: " + e.getMessage());
        }
    }

    public void addClient(Socket socket) {
        ChatClientHandler handler = new ChatClientHandler(socket);
        clients.add(handler);
        Thread thread = new Thread(handler, "ChatHandler-" + socket.getInetAddress());
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        
        try {
            if (chatServerSocket != null && !chatServerSocket.isClosed()) {
                chatServerSocket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        for (ChatClientHandler client : clients) {
            client.close();
        }
    }

    private void broadcast(Message message, ChatClientHandler sender) {
        System.out.println("[CHAT] Broadcasting from " + message.getSender() + ": " + message.getContent());
        System.out.println("[CHAT] Active clients: " + clients.size());
        
        int sent = 0;
        for (ChatClientHandler client : clients) {
            // Send to everyone EXCEPT the original sender
            if (client != sender && client.out != null) {
                client.sendMessage(message);
                sent++;
            }
        }
        System.out.println("[CHAT] Message sent to " + sent + " client(s)");
    }
    
    private void sendPrivateMessage(Message message, ChatClientHandler sender) {
        System.out.println("[CHAT] Private message from " + message.getSender() + 
                         " to " + message.getRecipient());
        
        boolean found = false;
        for (ChatClientHandler client : clients) {
            if (client.username != null && client.username.equals(message.getRecipient())) {
                client.sendMessage(message);
                found = true;
                System.out.println("[CHAT] Private message delivered to " + message.getRecipient());
                break;
            }
        }
        
        if (!found) {
            // Send error back to sender
            Message errorMsg = new Message(Message.MessageType.CHAT, "Server", 
                                          "User '" + message.getRecipient() + "' not found or offline");
            sender.sendMessage(errorMsg);
            System.out.println("[CHAT] User " + message.getRecipient() + " not found");
        }
    }
    
    private void broadcastUserList() {
        List<String> usernames = new ArrayList<>();
        for (ChatClientHandler client : clients) {
            if (client.username != null) {
                usernames.add(client.username);
            }
        }
        
        Message userListMsg = new Message(Message.MessageType.USER_LIST, "Server", 
                                         String.join(",", usernames));
        
        System.out.println("[CHAT] Broadcasting user list: " + usernames);
        for (ChatClientHandler client : clients) {
            client.sendMessage(userListMsg);
        }
    }

    private class ChatClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ChatClientHandler(Socket socket) {
            this.socket = socket;
            try {
                // Create object streams directly from socket
                // IMPORTANT: Create ObjectOutputStream FIRST, then ObjectInputStream
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.out.flush(); // Flush the header immediately
                
                this.in = new ObjectInputStream(socket.getInputStream());
                
                System.out.println("[CHAT] Object streams initialized for new client");
            } catch (IOException e) {
                System.err.println("[CHAT] Error creating streams: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Read login message
                System.out.println("[CHAT] Waiting for login message...");
                Message loginMessage = (Message) in.readObject();
                System.out.println("[CHAT] Received message type: " + loginMessage.getType());
                
                if (loginMessage.getType() == Message.MessageType.LOGIN) {
                    username = loginMessage.getSender();
                    System.out.println("[CHAT] User joined: " + username + " (Total clients: " + clients.size() + ")");
                    
                    // Send welcome message to the new user
                    Message welcomeMsg = new Message(Message.MessageType.CHAT, "Server", 
                                                     "Welcome to the chat, " + username + "!");
                    sendMessage(welcomeMsg);
                    
                    // Broadcast join message to all OTHER clients
                    Message joinMessage = new Message(Message.MessageType.CHAT, "Server", 
                                                     username + " has joined the chat");
                    broadcast(joinMessage, this);
                    
                    // Broadcast updated user list to all clients
                    broadcastUserList();
                }

                // Listen for messages
                while (running) {
                    try {
                        Message message = (Message) in.readObject();
                        System.out.println("[CHAT] Received " + message.getType() + " from " + message.getSender());
                        
                        if (message.getType() == Message.MessageType.CHAT) {
                            // Broadcast to all other clients
                            broadcast(message, this);
                        } else if (message.getType() == Message.MessageType.PRIVATE_CHAT) {
                            // Send private message to specific user
                            sendPrivateMessage(message, this);
                        } else if (message.getType() == Message.MessageType.LOGOUT) {
                            System.out.println("[CHAT] " + username + " logging out");
                            break;
                        }
                    } catch (EOFException e) {
                        System.out.println("[CHAT] Client " + username + " disconnected (EOF)");
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[CHAT] Client error for " + username + ": " + e.getMessage());
                e.printStackTrace();
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
                
                // Broadcast updated user list to all clients
                broadcastUserList();
            }
            close();
        }
    }
}
