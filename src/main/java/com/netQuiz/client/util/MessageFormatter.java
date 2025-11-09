package com.netQuiz.client.util;

import com.netQuiz.shared.Message;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatting chat messages with timestamps and styling.
 */
public class MessageFormatter {
    
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * Format an incoming message with timestamp and sender.
     */
    public static String formatIncomingMessage(Message message) {
        String timestamp = formatTimestamp(message.getTimestamp());
        return String.format("[%s] %s: %s", 
            timestamp, message.getSender(), message.getContent());
    }
    
    /**
     * Format an outgoing message (sent by current user).
     */
    public static String formatOutgoingMessage(String content) {
        String timestamp = formatCurrentTime();
        return String.format("[%s] You: %s", timestamp, content);
    }
    
    /**
     * Format a system message (e.g., user joined/left).
     */
    public static String formatSystemMessage(String message) {
        String timestamp = formatCurrentTime();
        return String.format("[%s] *** %s ***", timestamp, message);
    }
    
    /**
     * Format a private message.
     */
    public static String formatPrivateMessage(Message message) {
        String timestamp = formatTimestamp(message.getTimestamp());
        return String.format("[%s] 💬 Private from %s: %s", 
            timestamp, message.getSender(), message.getContent());
    }
    
    /**
     * Format timestamp from epoch milliseconds.
     */
    public static String formatTimestamp(long timestamp) {
        LocalTime time = LocalTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
        return time.format(TIME_FORMATTER);
    }
    
    /**
     * Format current time.
     */
    public static String formatCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }
    
    /**
     * Create a welcome banner for chat.
     */
    public static String createWelcomeBanner(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║    Welcome to NetQuiz Chat Room      ║\n");
        sb.append("╠══════════════════════════════════════╣\n");
        sb.append(String.format("║ Connected as: %-23s║\n", username));
        sb.append(String.format("║ Time: %-31s║\n", formatCurrentTime()));
        sb.append("╚══════════════════════════════════════╝\n\n");
        return sb.toString();
    }
    
    /**
     * Create a separator line.
     */
    public static String createSeparator() {
        return "─".repeat(50) + "\n";
    }
}
