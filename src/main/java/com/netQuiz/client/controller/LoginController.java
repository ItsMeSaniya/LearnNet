package com.netQuiz.client.controller;

import com.netQuiz.client.service.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button loginButton;
    
    private ClientServiceManager serviceManager;
    
    public LoginController() {
        serviceManager = ClientServiceManager.getInstance();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            statusLabel.setText("Please enter a username");
            return;
        }
        
        loginButton.setDisable(true);
        statusLabel.setText("Connecting to server...");
        
        // Connect to user management server with timeout
        new Thread(() -> {
            try {
                // Test if server is reachable first
                java.net.Socket testSocket = null;
                try {
                    testSocket = new java.net.Socket();
                    testSocket.connect(
                        new java.net.InetSocketAddress(
                            com.netQuiz.shared.Constants.SERVER_HOST, 
                            com.netQuiz.shared.Constants.SERVER_PORT
                        ), 
                        3000 // 3 second timeout
                    );
                    testSocket.close();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("❌ Cannot connect to server. Is it running?");
                        loginButton.setDisable(false);
                    });
                    return;
                }
                
                Platform.runLater(() -> statusLabel.setText("Logging in..."));
                
                serviceManager.setUsername(username);
                // Login with username and password (using "password" as default for now)
                boolean success = serviceManager.getUserService().login(username, "password", null);
                
                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setText("Connecting to chat...");
                        try {
                            // Connect to chat after successful login with timeout
                            Thread chatThread = new Thread(() -> {
                                try {
                                    serviceManager.connectChat();
                                    Platform.runLater(() -> openMainWindow());
                                } catch (Exception e) {
                                    Platform.runLater(() -> {
                                        statusLabel.setText("❌ Chat connection failed: " + e.getMessage());
                                        loginButton.setDisable(false);
                                    });
                                }
                            });
                            chatThread.setDaemon(true);
                            chatThread.start();
                            
                            // Timeout after 5 seconds
                            new Thread(() -> {
                                try {
                                    Thread.sleep(5000);
                                    if (chatThread.isAlive()) {
                                        chatThread.interrupt();
                                        Platform.runLater(() -> {
                                            statusLabel.setText("❌ Chat connection timeout");
                                            loginButton.setDisable(false);
                                        });
                                    }
                                } catch (InterruptedException e) {
                                    // Ignore
                                }
                            }).start();
                            
                        } catch (Exception e) {
                            statusLabel.setText("❌ Chat connection failed: " + e.getMessage());
                            loginButton.setDisable(false);
                        }
                    } else {
                        statusLabel.setText("❌ Login failed. Please try again.");
                        loginButton.setDisable(false);
                    }
                });
            } catch (java.net.SocketTimeoutException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Connection timeout. Check if server is running.");
                    loginButton.setDisable(false);
                });
            } catch (java.net.ConnectException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Connection refused. Server not running on port " 
                        + com.netQuiz.shared.Constants.SERVER_PORT);
                    loginButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Error: " + e.getMessage());
                    System.err.println("Login error: ");
                    e.printStackTrace();
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }
    
    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("NetQuiz - " + serviceManager.getUsername());
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Start notification listener
            serviceManager.getNotificationService().startListening();
            
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error opening main window: " + e.getMessage());
            loginButton.setDisable(false);
        }
    }
}
