# NetQuiz - Technical Documentation

## Architecture Overview

NetQuiz is built on a **client-server architecture** with 5 independent server modules, each demonstrating different networking concepts. The JavaFX client connects to all modules simultaneously.

```
                          ┌─────────────────┐
                          │  JavaFX Client  │
                          └────────┬────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
    ┌─────▼─────┐           ┌─────▼─────┐           ┌─────▼─────┐
    │Quiz Server│           │File Server│           │Chat Server│
    │TCP:5001   │           │TCP:5002   │           │TCP:5003   │
    │Multi-thread│          │Buffered IO│           │Multi-thread│
    └───────────┘           └───────────┘           └───────────┘
          │                        │                        │
    ┌─────▼─────┐           ┌─────▼─────┐
    │User Mgmt  │           │Notify Srv │
    │TCP:5004   │           │UDP:5005   │
    │Java NIO   │           │Broadcast  │
    └───────────┘           └───────────┘
```

## Module 1: Quiz Server (TCP + Multi-threading)

### Purpose

Manages quiz creation, distribution, and scoring for multiple concurrent users.

### Implementation Details

**Technology**: TCP Sockets with multi-threading

**File**: `QuizServer.java`

**Key Features**:

1. **Quiz Storage**: Uses in-memory `ConcurrentHashMap` for thread-safe quiz storage
2. **Persistence**: Saves quizzes to JSON file using Gson
3. **Multi-threading**: Each client request spawns a new thread via `QuizClientHandler`
4. **Object Serialization**: Quiz objects transmitted using Java serialization

**Protocol**:

```
Client -> Server: "LIST_QUIZZES"
Server -> Client: List<String> (quiz IDs and titles)

Client -> Server: "GET_QUIZ" + quizId
Server -> Client: Quiz object

Client -> Server: "SUBMIT_ANSWERS" + userId + quizId + int[] answers
Server -> Client: int (score)
```

**Thread Safety**:

- `ConcurrentHashMap` for quiz storage (thread-safe)
- Each client handled in isolated thread
- Synchronized file I/O for quiz persistence

**Code Snippet**:

```java
Thread clientThread = new Thread(new QuizClientHandler(clientSocket));
clientThread.start();
```

---

## Module 2: File Server (TCP + Buffered Streams)

### Purpose

Enables users to upload and download study materials efficiently.

### Implementation Details

**Technology**: TCP Sockets with buffered streams

**File**: `FileServer.java`

**Key Features**:

1. **Buffered I/O**: Uses `BufferedInputStream` and `BufferedOutputStream` (8KB buffer)
2. **Chunked Transfer**: Transfers files in chunks to handle large files
3. **File Metadata**: Tracks file name, size, and uploader
4. **Directory Management**: Auto-creates `server_files/` directory

**Protocol**:

```
UPLOAD:
Client -> Server: "UPLOAD" + fileName + uploader + fileSize
Client -> Server: [file bytes in chunks]
Server -> Client: "SUCCESS" or "ERROR"

DOWNLOAD:
Client -> Server: "DOWNLOAD" + fileName
Server -> Client: "SUCCESS" + fileSize + [file bytes]

LIST:
Client -> Server: "LIST"
Server -> Client: count + (fileName + fileSize)*count
```

**Efficiency**:

- Buffer size: 8192 bytes (optimal for most file sizes)
- Minimizes system calls
- Progress tracking via chunk counting

**Code Snippet**:

```java
byte[] buffer = new byte[Constants.BUFFER_SIZE];
while ((bytesRead = bis.read(buffer)) != -1) {
    out.write(buffer, 0, bytesRead);
}
```

---

## Module 3: Chat Server (TCP + Multi-threading)

### Purpose

Real-time messaging platform for instant communication between all connected users.

### Implementation Details

**Technology**: TCP Sockets with multi-threading and broadcasting

**File**: `ChatServer.java`

**Key Features**:

1. **Client Registry**: Maintains set of connected clients using `ConcurrentHashMap.newKeySet()`
2. **Message Broadcasting**: Distributes messages to all connected clients
3. **Persistent Connections**: Keeps socket open for continuous communication
4. **Thread-per-Client**: Each client has dedicated listener thread

**Protocol**:

```
Login:
Client -> Server: Message(LOGIN, username, "")
Server -> All: Message(CHAT, "Server", "username joined")

Chat:
Client -> Server: Message(CHAT, username, content)
Server -> All Others: Message(CHAT, username, content)

Logout:
Client -> Server: Message(LOGOUT, username, "")
Server -> All: Message(CHAT, "Server", "username left")
```

**Broadcasting Algorithm**:

```java
for (ChatClientHandler client : clients) {
    if (client != sender) {
        client.sendMessage(message);
    }
}
```

**Synchronization**:

- Synchronized `sendMessage()` to prevent concurrent writes
- Thread-safe client set for add/remove operations

---

## Module 4: User Management Server (Java NIO + Selectors)

### Purpose

Tracks online users and provides presence information using non-blocking I/O.

### Implementation Details

**Technology**: Java NIO with Selectors (non-blocking I/O)

**File**: `UserManagementServer.java`

**Key Features**:

1. **Non-blocking I/O**: Uses `Selector` to monitor multiple channels
2. **Single-threaded**: One thread handles all connections
3. **Scalability**: Can manage thousands of connections efficiently
4. **Real-time Updates**: Broadcasts user list changes immediately

**NIO Components**:

- **Selector**: Multiplexes I/O operations on multiple channels
- **ServerSocketChannel**: Non-blocking server socket
- **SocketChannel**: Non-blocking client connection
- **SelectionKey**: Represents channel registration with selector

**Protocol**:

```
Login:
Client -> Server: "LOGIN:username\n"
Server -> Client: "LOGIN_SUCCESS\n"
Server -> All: "USERS:user1,user2,user3,\n"

Get Users:
Client -> Server: "GET_USERS\n"
Server -> Client: "USERS:user1,user2,user3,\n"

Logout:
Client -> Server: "LOGOUT\n"
Server closes connection
Server -> All: "USERS:user1,user2,\n"
```

**NIO Event Loop**:

```java
while (running) {
    selector.select();
    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

    while (keys.hasNext()) {
        SelectionKey key = keys.next();
        keys.remove();

        if (key.isAcceptable()) {
            handleAccept(key);
        } else if (key.isReadable()) {
            handleRead(key);
        }
    }
}
```

**Advantages**:

- Lower memory footprint (no thread per connection)
- Better CPU utilization
- Handles many concurrent connections efficiently

---

## Module 5: Notification Server (UDP Broadcasting)

### Purpose

Sends system-wide announcements using connectionless UDP broadcasts.

### Implementation Details

**Technology**: UDP Datagram Sockets with broadcasting

**File**: `NotificationServer.java`

**Key Features**:

1. **Broadcast Mode**: Uses IP address `255.255.255.255`
2. **Fire-and-Forget**: No acknowledgment required
3. **Lightweight**: Minimal overhead
4. **Message Queue**: Thread-safe queue for pending notifications

**Protocol**:

```
Server broadcasts to 255.255.255.255:5005

Message formats:
"NEW_QUIZ:Quiz Title"
"NEW_FILE:filename uploaded by username"
"SYSTEM:System message"
```

**Client Listening**:

```java
DatagramSocket socket = new DatagramSocket(Constants.UDP_NOTIFICATION_PORT);
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet); // Blocks until packet arrives
```

**Use Cases**:

- New quiz announcements
- File upload notifications
- System maintenance messages
- Non-critical updates

**UDP vs TCP**:
| Feature | UDP (Module 5) | TCP (Modules 1-4) |
|---------|----------------|-------------------|
| Connection | Connectionless | Connection-oriented |
| Reliability | No guarantee | Guaranteed delivery |
| Order | May arrive out of order | In-order delivery |
| Speed | Faster | Slightly slower |
| Use case | Notifications | Critical data |

---

## Client Architecture

### Service Layer Pattern

Each server module has a corresponding client service:

```
ClientServiceManager (Singleton)
    ├── QuizService
    ├── FileService
    ├── ChatService
    ├── UserService
    └── NotificationService
```

### JavaFX UI Structure

**FXML Files**:

- `login.fxml`: Login screen
- `main.fxml`: Tabbed interface (Chat, Quiz, Files, Users, Notifications)

**Controllers**:

- `LoginController`: Handles authentication
- `MainController`: Manages all tabs and user interactions

**Threading**:

- UI updates via `Platform.runLater()` to avoid blocking
- Network operations in background threads
- Callbacks for async data reception

---

## Data Models

### Message

```java
class Message {
    MessageType type;  // CHAT, LOGIN, LOGOUT, etc.
    String sender;
    String content;
    long timestamp;
}
```

### Quiz

```java
class Quiz {
    String id;
    String title;
    List<Question> questions;

    class Question {
        String question;
        List<String> options;
        int correctAnswer;
    }
}
```

### FileInfo

```java
class FileInfo {
    String fileName;
    long fileSize;
    String uploader;
    long uploadTime;
}
```

---

## Concurrency & Thread Safety

### Quiz Server

- ✅ `ConcurrentHashMap` for quiz storage
- ✅ Thread-per-request model
- ✅ Synchronized file I/O

### File Server

- ✅ Thread-per-connection
- ✅ Independent file operations (no shared state)

### Chat Server

- ✅ Thread-safe client set (`ConcurrentHashMap.newKeySet()`)
- ✅ Synchronized `sendMessage()`
- ✅ Proper cleanup on disconnect

### User Management

- ✅ Single-threaded NIO (no concurrency issues)
- ✅ Thread-safe user map

### Notification Server

- ✅ Thread-safe `BlockingQueue` for messages
- ✅ Single broadcaster thread

---

## Error Handling

### Connection Failures

- Graceful degradation (individual modules can fail independently)
- User-friendly error messages via JavaFX alerts
- Automatic reconnection for persistent services (chat, user management)

### File Operations

- Validates file existence before download
- Checks available disk space
- Handles corrupted transfers

### Network Issues

- Timeout handling
- Socket closure detection
- Resource cleanup in finally blocks

---

## Performance Considerations

### Scalability

| Module        | Max Clients               | Bottleneck               |
| ------------- | ------------------------- | ------------------------ |
| Quiz          | ~100 (limited by threads) | Thread creation overhead |
| File          | ~50 (limited by disk I/O) | Bandwidth and disk speed |
| Chat          | ~100                      | Broadcasting overhead    |
| User Mgmt     | ~1000+                    | CPU (NIO is efficient)   |
| Notifications | Unlimited                 | Network bandwidth        |

### Optimization Techniques

1. **Buffer Sizing**: 8KB buffers balance memory and performance
2. **NIO for User Mgmt**: Handles more connections with fewer resources
3. **Object Pooling**: Could be added for frequent object creation
4. **Connection Pooling**: Could reuse sockets instead of creating new ones

---

## Security Considerations

⚠️ **Note**: This is an educational project. Production systems would require:

1. **Authentication**: Currently uses simple username (no passwords)
2. **Encryption**: All data transmitted in plaintext
3. **Authorization**: No role-based access control
4. **Input Validation**: Limited validation of user inputs
5. **Rate Limiting**: No protection against spam or DoS
6. **File Validation**: No virus scanning or type checking

**Potential Enhancements**:

- Add SSL/TLS for encrypted communication
- Implement proper authentication (hashing, salting)
- Add access control lists (ACLs)
- Validate and sanitize all inputs
- Implement rate limiting and throttling

---

## Testing Scenarios

### Multi-User Chat

1. Start server
2. Launch 3+ clients
3. Log in with different usernames
4. Send messages from each client
5. Verify all receive broadcasts

### Concurrent Quiz

1. Start server
2. Launch 2+ clients
3. Both start same quiz simultaneously
4. Submit answers
5. Verify correct scores

### File Sharing

1. Upload file from Client A
2. Verify appears in file list on Client B
3. Download from Client B
4. Compare checksums

### User Presence

1. Log in with Client A
2. Observe user list on Client B
3. Log out Client A
4. Verify Client B's list updates

### Notifications

1. Upload file from any client
2. Verify all clients receive UDP notification
3. Check notification appears in notification tab

---

## Extension Ideas

1. **Private Messaging**: Direct messages between users
2. **Quiz Creator UI**: Let users create quizzes through GUI
3. **File Preview**: Preview text files before downloading
4. **User Profiles**: Avatar, bio, statistics
5. **Chat History**: Persist chat messages to database
6. **Admin Panel**: Server monitoring and management
7. **Themes**: Dark/light mode for UI
8. **Leaderboard**: Top quiz scores
9. **File Categories**: Organize files by subject
10. **Voice Chat**: Add UDP streaming for voice

---

## Troubleshooting Guide

### Server Won't Start

**Symptom**: "Address already in use"
**Solution**: Kill process using the port or change port in `Constants.java`

### Client Can't Connect

**Symptom**: "Connection refused"
**Solution**:

- Ensure server is running
- Check firewall settings
- Verify localhost/IP address

### JavaFX Not Found

**Symptom**: "JavaFX runtime components are missing"
**Solution**:

- Use Maven: `mvn javafx:run`
- Or add JavaFX to module path manually

### Files Not Uploading

**Symptom**: Upload fails silently
**Solution**:

- Check `server_files/` directory permissions
- Verify file size is reasonable
- Check server logs for errors

### Notifications Not Received

**Symptom**: No UDP messages
**Solution**:

- Check Windows Firewall allows UDP 5005
- Verify client started notification listener
- Use Wireshark to confirm packets sent

---

## Learning Resources

### Java Networking

- Oracle's Java Networking Tutorial
- "Java Network Programming" by Elliotte Rusty Harold

### JavaFX

- OpenJFX Documentation
- "Learn JavaFX 17" by Kishori Sharan

### Concurrent Programming

- "Java Concurrency in Practice" by Brian Goetz
- Oracle's Concurrency Tutorial

### NIO

- Java NIO Documentation
- "Java NIO" by Ron Hitchens

---

**Built with ❤️ for learning Java networking concepts**
