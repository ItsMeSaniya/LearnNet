package com.netQuiz.server;

import com.netQuiz.server.handlers.*;
import com.netQuiz.server.notification.NotificationServer;
import com.netQuiz.shared.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main Server Application - Single Port Architecture
 * All TCP services run on port 5000, UDP notifications on port 5001
 * Routes client requests to appropriate handler module
 */
public class NetQuizServer {
    private ServerSocket serverSocket;
    private boolean running;

    // Handler instances
    private QuizHandler quizHandler;
    private FileHandler fileHandler;
    private ChatHandler chatHandler;
    private UserHandler userHandler;
    private NotificationServer notificationServer;

    public NetQuizServer() {
        this.running = false;

        // Initialize and start the notification server
        this.notificationServer = new NotificationServer();
        Thread notificationThread = new Thread(notificationServer);
        notificationThread.setDaemon(true);
        notificationThread.start();
        System.out.println("Notification server thread started");

        this.quizHandler = new QuizHandler(notificationServer);
        this.fileHandler = new FileHandler(notificationServer);
        this.chatHandler = new ChatHandler(notificationServer);
        this.userHandler = new UserHandler();
    }

    // Add a method to test notifications
    public void sendTestNotification() {
        if (notificationServer != null) {
            notificationServer.testBroadcast();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            running = true;

            // Start background services (chat, user management, notifications)
            new Thread(chatHandler, "ChatService").start();
            new Thread(userHandler, "UserService").start();

            printStartupBanner();

            // Main server loop - accepts connections and routes requests
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[CONNECTION] Client connected: " +
                            clientSocket.getInetAddress().getHostAddress());

                    // Handle each client in a separate thread
                    Thread clientThread = new Thread(new ClientRouter(clientSocket));
                    clientThread.start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[ERROR] Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[FATAL] Server startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        System.out.println("\n[SHUTDOWN] Stopping NetQuiz Server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            chatHandler.stop();
            userHandler.stop();

            System.out.println("[SHUTDOWN] Server stopped successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Error during shutdown: " + e.getMessage());
        }
    }

    private void printStartupBanner() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("               NetQuiz Multi-Module Server");
        System.out.println("=".repeat(70));
        System.out.println("  Main Server Port (TCP):        " + Constants.SERVER_PORT);
        System.out.println("  Notifications Port (UDP):      " + Constants.UDP_NOTIFICATION_PORT);
        System.out.println("=".repeat(70));
        System.out.println("\n  ✓ Module 1: Quiz System          (TCP + Multi-threading)");
        System.out.println("  ✓ Module 2: File Sharing         (TCP + Buffered I/O)");
        System.out.println("  ✓ Module 3: Real-Time Chat       (TCP + Broadcasting)");
        System.out.println("  ✓ Module 4: User Management      (Request-based)");
        System.out.println("  ✓ Module 5: Notifications        (UDP Broadcasting)");
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  Server Status: ONLINE");
        System.out.println("  Waiting for client connections...");
        System.out.println("=".repeat(70) + "\n");
    }

    public NotificationServer getNotificationServer() {
        return notificationServer;
    }

    /**
     * Routes client requests to appropriate handler based on request type
     */
    private class ClientRouter implements Runnable {
        private Socket socket;

        public ClientRouter(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                // Read request type to route to correct handler
                String requestType = in.readUTF();
                System.out.println("[REQUEST] " + requestType + " from " +
                        socket.getInetAddress().getHostAddress());

                // Route to appropriate handler
                switch (requestType) {
                    case Constants.QUIZ_REQUEST:
                        quizHandler.handleRequest(socket, in, out);
                        socket.close();
                        break;

                    case Constants.FILE_REQUEST:
                        fileHandler.handleRequest(socket, in, out);
                        socket.close();
                        break;

                    case Constants.CHAT_REQUEST:
                        // Chat needs persistent connection - hand off to chat handler
                        chatHandler.addClient(socket, in, out);
                        // Don't close socket - chat handler manages it
                        break;

                    case Constants.USER_REQUEST:
                        // User management request
                        userHandler.handleRequest(socket, in, out);
                        // Don't close socket - user handler manages it
                        break;

                    default:
                        System.err.println("[ERROR] Unknown request type: " + requestType);
                        out.writeUTF("ERROR");
                        out.flush();
                        socket.close();
                }

            } catch (IOException e) {
                System.err.println("[ERROR] Client router error: " + e.getMessage());
                try {
                    socket.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
    }

    public static void main(String[] args) {
        NetQuizServer server = new NetQuizServer();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));

        new Thread(() -> {
        try {
            Thread.sleep(2000); // Wait 2 seconds for server to start
            System.out.println("Sending test notification...");
            server.sendTestNotification();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();

        server.start();
    }
}
