# Phase 3 - JavaFX GUI Integration (Frontend)

## Overview
Phase 3 implementation provides a fully functional GUI-based real-time chat client using JavaFX with enhanced features including timestamps, message formatting, and auto-scrolling.

## Features Implemented

### ✅ Real-Time Chat Interface
- **TextArea** - Displays incoming and outgoing messages with timestamps
- **TextField** - User input box with Enter key support
- **Send Button** - Styled button to send messages
- **Auto-scroll** - Automatically scrolls to show latest messages
- **Welcome Banner** - ASCII art welcome message on connection

### ✅ Message Formatting
- **Timestamps** - All messages include `[HH:mm:ss]` format
- **Sender Identification** - Clear distinction between "You" and other users
- **System Messages** - Special formatting for system notifications
- **Monospace Font** - Courier New for better readability

### ✅ Threading Architecture
- **Message Receiver Thread** - Runs in background, listens for incoming messages
- **UI Thread** - All UI updates via `Platform.runLater()`
- **Message Sender** - Non-blocking async message sending

### ✅ Additional Features
- **Multiple Tabs** - Chat, Quizzes, Files, Users, Notifications
- **Online Users List** - Real-time user presence
- **File Sharing** - Upload/Download functionality
- **Quiz System** - Interactive quiz taking
- **UDP Notifications** - Broadcast system messages

## Project Structure

```
src/main/
├── java/com/netQuiz/client/
│   ├── controller/
│   │   ├── LoginController.java       # Login screen controller
│   │   └── MainController.java        # Main app controller (ENHANCED)
│   ├── service/
│   │   ├── ChatService.java           # Chat backend logic
│   │   ├── ClientServiceManager.java  # Service coordinator
│   │   ├── UserService.java           # User management
│   │   ├── FileService.java           # File operations
│   │   ├── QuizService.java           # Quiz operations
│   │   └── NotificationService.java   # UDP notifications
│   ├── util/
│   │   └── MessageFormatter.java      # Message formatting utility (NEW)
│   └── ClientApp.java                 # JavaFX application entry
└── resources/
    ├── fxml/
    │   ├── login.fxml                 # Login screen layout
    │   └── main.fxml                  # Main app layout (ENHANCED)
    └── styles/
        └── application.css            # Custom CSS styling (NEW)
```

## Key Components

### 1. MainController.java
**Location:** `src/main/java/com/netQuiz/client/controller/MainController.java`

**Enhanced Methods:**
- `setupChat()` - Initializes chat with message handler and welcome banner
- `handleSendChat()` - Sends message with timestamp formatting
- Auto-scroll implementation for chat area
- Integration with MessageFormatter utility

### 2. MessageFormatter.java (NEW)
**Location:** `src/main/java/com/netQuiz/client/util/MessageFormatter.java`

**Utility Methods:**
- `formatIncomingMessage()` - Format messages from other users
- `formatOutgoingMessage()` - Format messages from current user
- `formatSystemMessage()` - Format system notifications
- `createWelcomeBanner()` - ASCII art welcome message
- `formatTimestamp()` - Consistent time formatting

### 3. ChatService.java
**Location:** `src/main/java/com/netQuiz/client/service/ChatService.java`

**Features:**
- Socket connection to chat server
- Background thread for receiving messages
- Message callback handling via Consumer<Message>
- Clean disconnect handling

### 4. Enhanced FXML
**Location:** `src/main/resources/fxml/main.fxml`

**Improvements:**
- Better styling for chat area (light background)
- Larger send button with better visibility
- Improved input field with placeholder text
- Enhanced button styling with hover effects

## How to Run

### Start the Server
```bash
./start-server.sh
```

### Run Multiple GUI Clients

**Option 1: Using Shell Script**
```bash
./run-client.sh
```

**Option 2: Using Maven**
```bash
mvn clean javafx:run
```

**Option 3: From IDE**
Run `ClientApp.java` main method

### Testing Real-Time Chat

1. Start the server first
2. Launch multiple client instances
3. Login with different usernames
4. Send messages from one client
5. Verify messages appear instantly in all clients with timestamps
6. Check auto-scrolling behavior
7. Test Enter key to send messages

## Code Examples

### Sending a Message
```java
@FXML
private void handleSendChat() {
    String message = chatMessageField.getText().trim();
    if (!message.isEmpty()) {
        try {
            serviceManager.getChatService().sendMessage(
                serviceManager.getUsername(), message);
            
            Platform.runLater(() -> {
                chatArea.appendText(
                    MessageFormatter.formatOutgoingMessage(message) + "\n");
                chatArea.setScrollTop(Double.MAX_VALUE);
            });
            
            chatMessageField.clear();
        } catch (Exception e) {
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }
}
```

### Receiving Messages
```java
private void setupChat() {
    serviceManager.connectChat(msg -> {
        Platform.runLater(() -> {
            String formattedMsg = MessageFormatter.formatIncomingMessage(msg) + "\n";
            chatArea.appendText(formattedMsg);
            chatArea.setScrollTop(Double.MAX_VALUE);
        });
    });
    
    chatMessageField.setOnAction(e -> handleSendChat());
    chatArea.appendText(MessageFormatter.createWelcomeBanner(
        serviceManager.getUsername()));
}
```

## Testing Checklist

### Basic Functionality
- [x] Client connects to server successfully
- [x] Login screen appears and accepts credentials
- [x] Main chat interface loads
- [x] Send button works
- [x] Enter key sends messages
- [x] Messages display with timestamps
- [x] Multiple clients can connect simultaneously

### Real-Time Features
- [x] Messages from other users appear instantly
- [x] Chat area auto-scrolls to latest message
- [x] No message loss or duplication
- [x] Timestamps are accurate
- [x] User can identify their own messages

### UI/UX
- [x] Welcome banner displays on connection
- [x] Text is readable with monospace font
- [x] Input field clears after sending
- [x] Send button has hover effects
- [x] Layout is responsive
- [x] All tabs are accessible

### Error Handling
- [x] Connection failures show alerts
- [x] Empty messages are not sent
- [x] Server disconnect is handled gracefully
- [x] Thread cleanup on application close

## Architecture Highlights

### Threading Model
```
┌─────────────────────────────────────┐
│         JavaFX UI Thread            │
│  (MainController, FXML Updates)     │
└──────────────┬──────────────────────┘
               │
               │ Platform.runLater()
               │
┌──────────────▼──────────────────────┐
│      Background Receiver Thread      │
│    (ChatService.receiveMessages)    │
└──────────────┬──────────────────────┘
               │
               │ Socket I/O
               │
┌──────────────▼──────────────────────┐
│          Chat Server                │
│     (Broadcasts to all clients)     │
└─────────────────────────────────────┘
```

### Message Flow
```
User Input → TextField
    ↓
handleSendChat()
    ↓
ChatService.sendMessage()
    ↓
Socket → Server
    ↓
Server Broadcast
    ↓
All Clients' Receiver Threads
    ↓
Message Callback
    ↓
Platform.runLater() → TextArea Update
```

## Troubleshooting

### Chat messages not appearing
- Check server is running (`./start-server.sh`)
- Verify client logged in successfully
- Check console for connection errors

### Multiple instances won't start
- Each instance needs different username
- Check if port 12345 is available
- Try using Maven: `mvn javafx:run`

### Timestamps incorrect
- Verify system timezone settings
- Check MessageFormatter time zone configuration

### Auto-scroll not working
- Ensure `chatArea.setScrollTop(Double.MAX_VALUE)` is called
- Check Platform.runLater() wraps UI updates

## Future Enhancements (Optional)

1. **Private Messaging** - Direct messages between users
2. **Message History** - Save and load chat history
3. **Emoji Support** - Add emoji picker
4. **File Attachments** - Send files through chat
5. **User Status** - Online/Away/Busy indicators
6. **Message Reactions** - Like/React to messages
7. **Dark Mode** - Theme toggle
8. **Sound Notifications** - Alert on new messages
9. **Typing Indicators** - Show when users are typing
10. **Message Search** - Find messages in history

## Deliverable Status

✅ **Fully functional GUI-based real-time chat client**
- TextArea displays messages ✓
- TextField for user input ✓
- Send button functionality ✓
- Message listener thread ✓
- Message sender integration ✓
- Timestamps on all messages ✓
- Message formatting ✓
- Multiple clients tested ✓
- Real-time message delivery ✓

## Technologies Used

- **JavaFX 17** - GUI framework
- **Java Sockets** - Network communication
- **Maven** - Dependency management
- **FXML** - UI layout
- **CSS** - Styling
- **Multi-threading** - Concurrent message handling

## Credits

Phase 3 Implementation - Real-Time Chat GUI
Developed for NetQuiz Platform
