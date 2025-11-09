package com.netQuiz.client;

import com.netQuiz.client.service.UserService;
import java.io.*;
import java.util.Scanner;

/**
 * Simple Console-based Chat Client for Testing Phase 2
 * Asks for username and allows chat with Phase 2 features
 */
public class ConsoleClient {
    private UserService userService;
    private String currentUsername;
    private boolean running;

    public ConsoleClient() {
        this.userService = new UserService();
        this.running = false;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║              NetQuiz Console Chat Client                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Ask for username
        System.out.print("Enter your username: ");
        currentUsername = scanner.nextLine().trim();

        if (currentUsername.isEmpty()) {
            currentUsername = "Guest" + System.currentTimeMillis();
            System.out.println("Using auto-generated username: " + currentUsername);
        }

        // Ask for password (can be anything for testing)
        System.out.print("Enter password (or press Enter): ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            password = "password";
        }

        // Connect to server
        System.out.println("\n🔄 Connecting to server...");
        try {
            boolean loginSuccess = userService.login(
                currentUsername,
                password,
                users -> {
                    // User list handler
                    System.out.println("\n📋 Connected users: " + String.join(", ", users));
                },
                message -> {
                    // Message handler - display incoming messages
                    System.out.println("\n💬 " + message);
                    System.out.print(currentUsername + "> ");
                    System.out.flush();
                }
            );

            if (loginSuccess) {
                System.out.println("✅ Connected successfully!");
                System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("Commands:");
                System.out.println("  /msg <username> <message>  - Send private message");
                System.out.println("  /users                     - List connected users");
                System.out.println("  /help                      - Show help");
                System.out.println("  /quit                      - Exit");
                System.out.println("  <any text>                 - Send public message");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println();

                running = true;
                chatLoop(scanner);
            } else {
                System.out.println("❌ Login failed! Server may be full or username is taken.");
            }

        } catch (IOException e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            System.err.println("Make sure the server is running on ports 5002 (main) and 5004 (chat)");
        }
    }

    private void chatLoop(Scanner scanner) {
        System.out.print(currentUsername + "> ");

        while (running) {
            try {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        System.out.print(currentUsername + "> ");
                        continue;
                    }

                    if (input.equalsIgnoreCase("/quit") || input.equalsIgnoreCase("/exit")) {
                        System.out.println("\n👋 Disconnecting...");
                        running = false;
                        break;
                    }

                    // Send message to server
                    userService.sendMessage(input);

                    // Show prompt again (unless it was a command that expects response)
                    if (!input.startsWith("/")) {
                        System.out.print(currentUsername + "> ");
                    }
                }
            } catch (IOException e) {
                System.err.println("\n❌ Send error: " + e.getMessage());
                running = false;
            }
        }

        // Cleanup
        userService.logout(currentUsername);
        scanner.close();
        System.out.println("✅ Disconnected.");
    }

    public static void main(String[] args) {
        ConsoleClient client = new ConsoleClient();
        client.start();
    }
}

