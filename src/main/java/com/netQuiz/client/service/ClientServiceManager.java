package com.netQuiz.client.service;

public class ClientServiceManager {
    private static ClientServiceManager instance;
    
    private String username;
    private QuizService quizService;
    private FileService fileService;
    private ChatService chatService;
    private UserService userService;
    private NotificationService notificationService;
    
    private ClientServiceManager() {
        this.quizService = new QuizService();
        this.fileService = new FileService();
        this.chatService = new ChatService();
        this.userService = new UserService();
        this.notificationService = new NotificationService();
    }
    
    public static synchronized ClientServiceManager getInstance() {
        if (instance == null) {
            instance = new ClientServiceManager();
        }
        return instance;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        // Chat will be connected after successful login
    }
    
    public void connectChat() {
        if (username != null) {
            try {
                // Connect to chat with username and message handler
                chatService.connect(username, message -> {
                    System.out.println("Chat message: " + message.getContent());
                });
            } catch (Exception e) {
                System.err.println("Chat connection error: " + e.getMessage());
            }
        }
    }
    
    public QuizService getQuizService() {
        return quizService;
    }
    
    public FileService getFileService() {
        return fileService;
    }
    
    public ChatService getChatService() {
        return chatService;
    }
    
    public UserService getUserService() {
        return userService;
    }
    
    public NotificationService getNotificationService() {
        return notificationService;
    }
    
    public void shutdown() {
        chatService.disconnect();
        if (username != null) {
            userService.logout(username);
        }
        notificationService.stop();
    }
}
