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
        statusLabel.setText("Connecting...");
        
        // Connect to user management server
        new Thread(() -> {
            try {
                serviceManager.setUsername(username);
                // Login with username and password (using "password" as default for now)
                boolean success = serviceManager.getUserService().login(username, "password", null);
                
                Platform.runLater(() -> {
                    if (success) {
                        // Connect to chat after successful login
                        serviceManager.connectChat();
                        openMainWindow();
                    } else {
                        statusLabel.setText("Login failed. Please try again.");
                        loginButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Connection error: " + e.getMessage());
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
