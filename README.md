# LearnNet - Multi-User Quiz & Communication Platform

A comprehensive Java networking application demonstrating TCP/UDP sockets, multi-threading, NIO, and JavaFX GUI.

## 🎯 Features

### Module 1: Quiz System (TCP + Multi-threading)

- Server stores and manages quiz questions
- Clients request quizzes via TCP sockets
- Server sends questions, receives answers, and calculates scores
- Multi-threaded to handle multiple quiz participants simultaneously

### Module 2: File Sharing (TCP + Buffered Streams)

- Upload and download study notes through the server
- Efficient file transfer using buffered streams
- File listing and management

### Module 3: Real-Time Chat (TCP + Multi-threading)

- Instant messaging between connected clients
- Server broadcasts messages to all participants
- Each client runs in its own thread for parallel communication

### Module 4: User Management (Java NIO + Selectors)

- User login/logout handling
- Real-time online user tracking
- Non-blocking I/O using Java NIO selectors
- Live user list updates

### Module 5: Notification System (UDP Broadcasting)

- Broadcasts announcements via UDP packets
- Lightweight, connectionless communication
- System-wide notifications (e.g., "New quiz added")

## 🏗️ Project Structure

```
netquiz-backend/
├── src/main/java/com/netQuiz/
│   ├── ServerMain.java                    # Server entry point
│   ├── server/
│   │   ├── NetQuizServer.java            # Main server coordinator
│   │   ├── quiz/QuizServer.java          # Module 1: Quiz handling
│   │   ├── file/FileServer.java          # Module 2: File sharing
│   │   ├── chat/ChatServer.java          # Module 3: Real-time chat
│   │   ├── user/UserManagementServer.java # Module 4: User management
│   │   └── notification/NotificationServer.java # Module 5: UDP notifications
│   ├── client/
│   │   ├── ClientApp.java                # JavaFX client entry point
│   │   ├── controller/
│   │   │   ├── LoginController.java      # Login UI controller
│   │   │   └── MainController.java       # Main UI controller
│   │   └── service/
│   │       ├── ClientServiceManager.java # Service coordinator
│   │       ├── QuizService.java          # Quiz client service
│   │       ├── FileService.java          # File client service
│   │       ├── ChatService.java          # Chat client service
│   │       ├── UserService.java          # User client service
│   │       └── NotificationService.java  # Notification listener
│   └── shared/
│       ├── Message.java                  # Shared message model
│       ├── Quiz.java                     # Quiz data model
│       ├── FileInfo.java                 # File metadata model
│       └── Constants.java                # Shared constants
└── src/main/resources/fxml/
    ├── login.fxml                        # Login UI layout
    └── main.fxml                         # Main application UI layout
```

## 🚀 How to Run

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Step 1: Build the Project

```bash
mvn clean package
```

### Step 2: Start the Server

Open a terminal and run:

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.netQuiz.ServerMain"

# Or using the launcher scripts:
# Windows PowerShell:
.\run-server.bat

# Windows Command Prompt or Mac/Linux:
run-server.bat  # Windows CMD
./run-server.sh # Mac/Linux

# Or using Java directly after building
java -cp target/NetQuiz-Backend-1.0-SNAPSHOT.jar com.netQuiz.ServerMain
```

> **PowerShell Users**: Use `.\run-server.bat` (with the `.\` prefix)

You should see output indicating all server modules have started:

```
==================================================
Starting NetQuiz Server Application
==================================================
Quiz Server started on port 5001
File Server started on port 5002
Chat Server started on port 5003
User Management Server started on port 5004
Notification Server started (UDP Broadcasting)
==================================================
All server modules started successfully!
==================================================
```

### Step 3: Start Client(s)

Open **separate terminals** for each client and run:

```bash
# Using Maven JavaFX plugin (recommended)
mvn javafx:run

# Or using launcher scripts:
# Windows PowerShell:
.\run-client.bat

# Windows Command Prompt or Mac/Linux:
run-client.bat  # Windows CMD
./run-client.sh # Mac/Linux

# Or using Java directly (Windows example - adjust paths for your system)
java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp target/NetQuiz-Backend-1.0-SNAPSHOT.jar com.netQuiz.client.ClientApp
```

> **PowerShell Users**: Use `.\run-client.bat` (with the `.\` prefix)

**Note:** You can run multiple clients simultaneously to test multi-user features!

## 📡 Server Ports

| Module          | Protocol | Port | Purpose                 |
| --------------- | -------- | ---- | ----------------------- |
| Quiz Server     | TCP      | 5001 | Quiz management         |
| File Server     | TCP      | 5002 | File sharing            |
| Chat Server     | TCP      | 5003 | Real-time messaging     |
| User Management | TCP      | 5004 | User tracking (NIO)     |
| Notifications   | UDP      | 5005 | Broadcast announcements |

## 🎮 Using the Application

### Login

1. Enter a username
2. Click "Login"
3. The main application window will open

### Chat

- Navigate to the "💬 Chat" tab
- Type messages and click "Send"
- Messages are broadcast to all connected users

### Quizzes

1. Go to "📝 Quizzes" tab
2. Select a quiz from the list
3. Click "Start Quiz"
4. Answer the questions using radio buttons
5. Click "Submit Quiz" to see your score

### File Sharing

1. Go to "📁 Files" tab
2. Click "Upload File" to share a file
3. Select a file from the list
4. Click "Download Selected" to download

### Online Users

- Check "👥 Users" tab to see all connected users
- List updates automatically when users join/leave

### Notifications

- "🔔 Notifications" tab shows UDP broadcast messages
- System announcements appear here in real-time

## 🔧 Technical Implementation Details

### Multi-threading

- **Quiz Server**: Each client request handled in a separate thread
- **File Server**: Concurrent file transfers with thread-per-connection
- **Chat Server**: Dedicated thread per connected client for message broadcasting

### Non-blocking I/O (NIO)

- **User Management Server**: Uses Java NIO Selectors for efficient connection handling
- Single thread manages multiple client connections
- Demonstrates scalability advantages of NIO

### TCP vs UDP

- **TCP (Ports 5001-5004)**: Reliable, connection-oriented communication for critical data
- **UDP (Port 5005)**: Connectionless broadcasting for non-critical notifications

### Buffered Streams

- File transfers use `BufferedInputStream` and `BufferedOutputStream`
- 8KB buffer size for efficient data transfer
- Minimizes I/O operations

## 📝 Sample Quizzes

The server comes pre-loaded with two sample quizzes:

1. **General Knowledge Quiz** - Tests general knowledge
2. **Basic Quiz** - Simple questions

## 🛠️ Development

### Adding New Quizzes

Edit the `initializeQuizzes()` method in `QuizServer.java` to add more quizzes.

### Customizing Ports

Modify `Constants.java` to change server ports.

### Extending Features

Each module is independent - you can extend or modify individual modules without affecting others.

## 🐛 Troubleshooting

### "Connection refused"

- Ensure the server is running before starting clients
- Check firewall settings for the required ports

### JavaFX errors

- Ensure JavaFX libraries are in your classpath
- For Maven: Dependencies are automatically managed
- For manual setup: Download JavaFX SDK and configure module path

### Port already in use

- Stop any existing server instances
- Or change port numbers in `Constants.java`

## 📚 Learning Objectives Demonstrated

✅ **TCP Socket Programming**: Client-server communication  
✅ **UDP Broadcasting**: Connectionless messaging  
✅ **Multi-threading**: Concurrent client handling  
✅ **Java NIO**: Non-blocking I/O with Selectors  
✅ **Buffered I/O**: Efficient file transfers  
✅ **Object Serialization**: Network data transfer  
✅ **JavaFX**: Modern GUI development  
✅ **MVC Pattern**: Separation of concerns  
✅ **Service Layer**: Clean architecture

## 👥 Team Members & Contributions

- **Member 1**: Quiz Module (TCP + Multi-threading)
- **Member 2**: File Sharing Module (TCP + Buffered Streams)
- **Member 3**: Real-Time Chat (TCP + Multi-threading)
- **Member 4**: User Management (Java NIO + Selectors)
- **Member 5**: Notification System (UDP Broadcasting)

## 📄 License

Educational project for learning Java networking concepts.

---

**Enjoy exploring NetQuiz! 🎓🚀**
