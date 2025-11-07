package com.netQuiz.client.service;

import com.netQuiz.shared.Constants;
import com.netQuiz.shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatService {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread receiverThread;
    private Consumer<Message> messageHandler;
    private volatile boolean running = false;
    
    public void connect(String username, Consumer<Message> messageHandler) throws IOException {
        this.messageHandler = messageHandler;
        socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        
        // Send request type
        out.writeUTF(Constants.CHAT_REQUEST);
        out.writeUTF("CONNECT");
        out.writeUTF(username);
        out.flush();
        
        running = true;
        receiverThread = new Thread(this::receiveMessages);
        receiverThread.setDaemon(true);
        receiverThread.start();
    }
    
    public void sendMessage(String sender, String content) throws IOException {
        if (out != null) {
            out.writeUTF("MESSAGE");
            out.writeUTF(sender);
            out.writeUTF(content);
            out.flush();
        }
    }
    
    private void receiveMessages() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                String sender = in.readUTF();
                String content = in.readUTF();
                long timestamp = in.readLong();
                
                Message msg = new Message(sender, content, timestamp);
                if (messageHandler != null) {
                    messageHandler.accept(msg);
                }
            }
        } catch (IOException e) {
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
                out.writeUTF("DISCONNECT");
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
