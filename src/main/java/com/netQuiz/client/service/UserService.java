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
    private volatile boolean running = false;
    
    public boolean login(String username, String password, Consumer<List<String>> userListHandler) 
            throws IOException {
        this.userListHandler = userListHandler;
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
        
        if (success) {
            running = true;
            listenerThread = new Thread(this::listenForUpdates);
            listenerThread.setDaemon(true);
            listenerThread.start();
        }
        
        return success;
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
                int count = in.readInt();
                List<String> users = new ArrayList<>();
                
                for (int i = 0; i < count; i++) {
                    users.add(in.readUTF());
                }
                
                if (userListHandler != null) {
                    userListHandler.accept(users);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("User update listener error: " + e.getMessage());
            }
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
