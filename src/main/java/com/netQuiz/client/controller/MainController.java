package com.netQuiz.client.controller;

import com.netQuiz.client.service.*;
import com.netQuiz.client.util.MessageFormatter;
import com.netQuiz.shared.FileInfo;
import com.netQuiz.shared.Message;
import com.netQuiz.shared.Quiz;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {
    
    // Chat Tab
    @FXML private TextArea chatArea;
    @FXML private TextField chatMessageField;
    @FXML private Button sendChatButton;
    
    // Quiz Tab
    @FXML private ListView<String> quizListView;
    @FXML private Button startQuizButton;
    @FXML private VBox quizContentBox;
    
    // File Tab
    @FXML private ListView<String> fileListView;
    @FXML private Button uploadButton;
    @FXML private Button downloadButton;
    @FXML private Button refreshFilesButton;
    
    // Users Tab
    @FXML private ListView<String> userListView;
    @FXML private Button sendPrivateMessageButton;
    
    // Notifications Tab
    @FXML private TextArea notificationArea;
    
    private ClientServiceManager serviceManager;
    private ObservableList<String> quizList;
    private ObservableList<String> fileList;
    private ObservableList<String> userList;
    
    private Quiz currentQuiz;
    private List<RadioButton> quizAnswers;
    
    public MainController() {
        serviceManager = ClientServiceManager.getInstance();
        quizList = FXCollections.observableArrayList();
        fileList = FXCollections.observableArrayList();
        userList = FXCollections.observableArrayList();
        quizAnswers = new ArrayList<>();
    }
    
    @FXML
    public void initialize() {
        // Setup Chat
        setupChat();
        
        // Setup Quiz
        quizListView.setItems(quizList);
        loadQuizList();
        
        // Setup Files
        fileListView.setItems(fileList);
        loadFileList();
        
        // Setup Users
        userListView.setItems(userList);
        setupUserListContextMenu();
        loadOnlineUsers();
        
        // Setup Notifications
        setupNotifications();
    }
    
    private void setupChat() {
        // The chat is already connected from LoginController
        // We need to update the message handler to show messages in the UI
        ChatService chatService = serviceManager.getChatService();
        
        // Set up message handler for incoming messages
        chatService.setMessageHandler(msg -> {
            Platform.runLater(() -> {
                // Handle different message types
                if (msg.getType() == Message.MessageType.USER_LIST) {
                    // Update user list
                    String[] users = msg.getContent().split(",");
                    updateUserList(java.util.Arrays.asList(users));
                } else if (msg.getType() == Message.MessageType.PRIVATE_CHAT) {
                    // Display private message
                    String formattedMsg = MessageFormatter.formatPrivateMessage(msg) + "\n";
                    chatArea.appendText(formattedMsg);
                    chatArea.setScrollTop(Double.MAX_VALUE);
                } else {
                    // Display regular chat message
                    String formattedMsg = MessageFormatter.formatIncomingMessage(msg) + "\n";
                    chatArea.appendText(formattedMsg);
                    chatArea.setScrollTop(Double.MAX_VALUE);
                }
            });
        });
        
        // Allow Enter key to send message
        chatMessageField.setOnAction(e -> handleSendChat());
        
        // Welcome message using formatter
        chatArea.appendText(MessageFormatter.createWelcomeBanner(serviceManager.getUsername()));
    }
    
    private void setupNotifications() {
        NotificationService notificationService = serviceManager.getNotificationService();
        
        notificationService.setNotificationCallback(notification -> {
            Platform.runLater(() -> {
                notificationArea.appendText("[" + java.time.LocalTime.now() + "] " + 
                                           notification + "\n");
            });
        });
    }
    
    @FXML
    private void handleSendChat() {
        String message = chatMessageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                serviceManager.getChatService().sendMessage(serviceManager.getUsername(), message);
                
                // Display sent message using formatter
                Platform.runLater(() -> {
                    chatArea.appendText(MessageFormatter.formatOutgoingMessage(message) + "\n");
                    chatArea.setScrollTop(Double.MAX_VALUE);
                });
                
                chatMessageField.clear();
            } catch (Exception e) {
                showAlert("Error", "Failed to send message: " + e.getMessage());
            }
        }
    }
    
    private void loadQuizList() {
        new Thread(() -> {
            try {
                List<String> quizzes = serviceManager.getQuizService().getQuizList();
                Platform.runLater(() -> {
                    quizList.clear();
                    quizList.addAll(quizzes);
                });
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showAlert("Error", "Failed to load quiz list: " + e.getMessage())
                );
            }
        }).start();
    }
    
    @FXML
    private void handleStartQuiz() {
        String selected = quizListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Quiz Selected", "Please select a quiz to start.");
            return;
        }
        
        String quizId = selected.split(":")[0];
        
        new Thread(() -> {
            try {
                currentQuiz = serviceManager.getQuizService().getQuiz(quizId);
                Platform.runLater(() -> displayQuiz());
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showAlert("Error", "Failed to load quiz: " + e.getMessage())
                );
            }
        }).start();
    }
    
    private void displayQuiz() {
        if (currentQuiz == null) return;
        
        quizContentBox.getChildren().clear();
        quizAnswers.clear();
        
        Label titleLabel = new Label(currentQuiz.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        quizContentBox.getChildren().add(titleLabel);
        
        List<Quiz.Question> questions = currentQuiz.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Quiz.Question question = questions.get(i);
            
            Label questionLabel = new Label((i + 1) + ". " + question.getQuestion());
            questionLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10 0 5 0;");
            quizContentBox.getChildren().add(questionLabel);
            
            ToggleGroup group = new ToggleGroup();
            for (int j = 0; j < question.getOptions().size(); j++) {
                RadioButton rb = new RadioButton(question.getOptions().get(j));
                rb.setToggleGroup(group);
                rb.setUserData(j);
                quizContentBox.getChildren().add(rb);
                if (j == 0) quizAnswers.add(rb);
            }
            quizAnswers.set(i, (RadioButton) group.getToggles().get(0));
        }
        
        Button submitButton = new Button("Submit Quiz");
        submitButton.setOnAction(e -> handleSubmitQuiz());
        submitButton.setStyle("-fx-margin: 10 0 0 0;");
        quizContentBox.getChildren().add(submitButton);
    }
    
    @FXML
    private void handleSubmitQuiz() {
        if (currentQuiz == null) return;
        
        int[] answers = new int[currentQuiz.getQuestions().size()];
        for (int i = 0; i < quizAnswers.size(); i++) {
            RadioButton rb = quizAnswers.get(i);
            if (rb.getToggleGroup().getSelectedToggle() != null) {
                answers[i] = (int) rb.getToggleGroup().getSelectedToggle().getUserData();
            }
        }
        
        new Thread(() -> {
            try {
                int score = serviceManager.getQuizService().submitAnswers(
                    serviceManager.getUsername(), currentQuiz.getId(), answers);
                
                Platform.runLater(() -> {
                    showAlert("Quiz Complete", 
                        "Your score: " + score + "/" + currentQuiz.getQuestions().size());
                    quizContentBox.getChildren().clear();
                });
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showAlert("Error", "Failed to submit quiz: " + e.getMessage())
                );
            }
        }).start();
    }
    
    private void loadFileList() {
        new Thread(() -> {
            try {
                List<FileInfo> files = serviceManager.getFileService().getFileList();
                Platform.runLater(() -> {
                    fileList.clear();
                    for (FileInfo file : files) {
                        fileList.add(file.getFileName() + " (" + formatFileSize(file.getFileSize()) + ")");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showAlert("Error", "Failed to load file list: " + e.getMessage())
                );
            }
        }).start();
    }
    
    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        
        if (file != null) {
            new Thread(() -> {
                try {
                    serviceManager.getFileService().uploadFile(file, serviceManager.getUsername());
                    Platform.runLater(() -> {
                        showAlert("Success", "File uploaded successfully!");
                        loadFileList();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> 
                        showAlert("Error", "Failed to upload file: " + e.getMessage())
                    );
                }
            }).start();
        }
    }
    
    @FXML
    private void handleDownloadFile() {
        String selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No File Selected", "Please select a file to download.");
            return;
        }
        
        String fileName = selected.split(" \\(")[0];
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());
        
        if (file != null) {
            new Thread(() -> {
                try {
                    serviceManager.getFileService().downloadFile(fileName, file);
                    Platform.runLater(() -> 
                        showAlert("Success", "File downloaded successfully!")
                    );
                } catch (Exception e) {
                    Platform.runLater(() -> 
                        showAlert("Error", "Failed to download file: " + e.getMessage())
                    );
                }
            }).start();
        }
    }
    
    @FXML
    private void handleRefreshFiles() {
        loadFileList();
    }
    
    private void loadOnlineUsers() {
        new Thread(() -> {
            try {
                List<String> users = serviceManager.getUserService().getOnlineUsers();
                updateUserList(users);
            } catch (Exception e) {
                Platform.runLater(() -> 
                    showAlert("Error", "Failed to load user list: " + e.getMessage())
                );
            }
        }).start();
    }
    
    public void updateUserList(List<String> users) {
        Platform.runLater(() -> {
            userList.clear();
            for (String user : users) {
                if (!user.trim().isEmpty() && !user.equals(serviceManager.getUsername())) {
                    userList.add(user);
                }
            }
        });
    }
    
    private void setupUserListContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem sendPrivateMsg = new MenuItem("Send Private Message");
        
        sendPrivateMsg.setOnAction(e -> {
            String selectedUser = userListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                handleSendPrivateMessage(selectedUser);
            }
        });
        
        contextMenu.getItems().add(sendPrivateMsg);
        
        userListView.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                if (userListView.getSelectionModel().getSelectedItem() != null) {
                    contextMenu.show(userListView, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }
    
    @FXML
    private void handleSendPrivateMessage() {
        String selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            handleSendPrivateMessage(selectedUser);
        } else {
            showAlert("No User Selected", "Please select a user to send a private message.");
        }
    }
    
    private void handleSendPrivateMessage(String recipient) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Private Message");
        dialog.setHeaderText("Send private message to " + recipient);
        dialog.setContentText("Message:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            if (!message.trim().isEmpty()) {
                try {
                    serviceManager.getChatService().sendPrivateMessage(
                        serviceManager.getUsername(), recipient, message);
                    
                    // Display sent private message
                    Platform.runLater(() -> {
                        String formattedMsg = String.format("[%s] 💬 Private to %s: %s\n",
                            java.time.LocalTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                            recipient, message);
                        chatArea.appendText(formattedMsg);
                        chatArea.setScrollTop(Double.MAX_VALUE);
                    });
                } catch (Exception e) {
                    showAlert("Error", "Failed to send private message: " + e.getMessage());
                }
            }
        });
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / (1024.0 * 1024.0));
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
