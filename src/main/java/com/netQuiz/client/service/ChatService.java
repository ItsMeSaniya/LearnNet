package com.netQuiz.client.service;

import com.netQuiz.shared.Constants;
import com.netQuiz.shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatService {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread receiverThread;
    private Consumer<Message> messageHandler;
    private volatile boolean running = false;
    private String username;
    
    public void connect(String username, Consumer<Message> messageHandler) throws IOException {
        this.username = username;
        this.messageHandler = messageHandler;
        
        try {
            // Connect to dedicated chat port (not the main server port)
            socket = new Socket(Constants.SERVER_HOST, Constants.CHAT_PORT);
            // No timeout - this is a persistent connection
            
            System.out.println("[CHAT] Connected to chat server on port " + Constants.CHAT_PORT);
            
            // Create object streams directly - no routing needed on dedicated port
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            
            System.out.println("[CHAT] Created ObjectOutputStream");
            
            in = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("[CHAT] Created ObjectInputStream");
            
            // Send login message
            Message loginMsg = new Message(Message.MessageType.LOGIN, username, "");
            out.writeObject(loginMsg);
            out.flush();
            
            System.out.println("[CHAT] Sent login message for: " + username);
            
            running = true;
            receiverThread = new Thread(this::receiveMessages, "ChatReceiver-" + username);
            receiverThread.setDaemon(true);
            receiverThread.start();
            
            System.out.println("[CHAT] Receiver thread started");
            
        } catch (IOException e) {
            System.err.println("[CHAT] Connection error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public void setMessageHandler(Consumer<Message> messageHandler) {
        this.messageHandler = messageHandler;
    }
    
    public void sendMessage(String sender, String content) throws IOException {
        if (out != null && running) {
            Message msg = new Message(Message.MessageType.CHAT, sender, content);
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
            }
        }
    }
    
    public void sendPrivateMessage(String sender, String recipient, String content) throws IOException {
        if (out != null && running) {
            Message msg = new Message(Message.MessageType.PRIVATE_CHAT, sender, recipient, content);
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
            }
        }
    }
    
    private void receiveMessages() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                Message msg = (Message) in.readObject();
                if (messageHandler != null) {
                    messageHandler.accept(msg);
                }
            }
        } catch (EOFException e) {
            // Connection closed
            System.out.println("Chat connection closed");
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                System.err.println("Chat connection error: " + e.getMessage());
            }
        }
    }
    
    public void disconnect() {
        running = false;
        
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        
        try {
            if (out != null) {
                Message logoutMsg = new Message(Message.MessageType.LOGOUT, username, "");
                out.writeObject(logoutMsg);
                out.flush();
                out.close();
            }
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
