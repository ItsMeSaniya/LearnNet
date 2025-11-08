package com.netQuiz.shared;

public class Constants {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 5002; // Single port for all TCP operations (changed from 5000)
    public static final int UDP_NOTIFICATION_PORT = 5003; // Separate UDP port for broadcasts (changed from 5001)

    public static final int BUFFER_SIZE = 8192;
    public static final String FILES_DIRECTORY = "server_files";
    public static final String QUIZZES_FILE = "quizzes.json";
    
    // Request types for routing
    public static final String QUIZ_REQUEST = "QUIZ";
    public static final String FILE_REQUEST = "FILE";
    public static final String CHAT_REQUEST = "CHAT";
    public static final String USER_REQUEST = "USER";
}
