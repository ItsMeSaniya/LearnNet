# LearnNet

Network programming assignment for learning socket programming and multithreading.

## Features

### Phase 1 - Core Chat Logic
- Multi-threaded chat server (port 5002)
- Multiple client support
- Real-time message broadcasting

### Phase 2 - User Identity & Messaging (NEW!)
- ✅ Username requirement on join
- ✅ Join/leave notifications broadcast to all users
- ✅ Private messaging with `/msg username message`
- ✅ User list command `/users`
- ✅ Help command `/help`
- ✅ Duplicate username prevention

### Additional Features
- Quiz system
- File sharing
- User management (integrated with NetQuizServer)
- Notifications

## Quick Start

### Phase 1 - Simple Chat

Compile:
```bash
javac -d target/classes src/main/java/com/netQuiz/server/ChatServer.java src/main/java/com/netQuiz/server/ClientHandler.java
javac -d target/classes src/main/java/com/netQuiz/client/ChatClient.java src/main/java/com/netQuiz/client/MessageReader.java
```

Run server:
```bash
java -cp target/classes com.netQuiz.server.ChatServer
```

Run client (multiple terminals):
```bash
java -cp target/classes com.netQuiz.client.ChatClient
```

### Phase 2 - Enhanced Chat with NetQuizServer

The main NetQuizServer now includes Phase 2 features through the enhanced UserHandler.

Run the main server:
```bash
./run-server.sh
```

Connect with NetQuiz clients to use:
- Join/leave notifications
- Private messaging: `/msg username message`
- User list: `/users`
- Help: `/help`

## Phase 2 Testing

1. **Test Join Notifications:**
   - Start 3 clients with usernames: Alice, Bob, Charlie
   - Verify each client sees join messages for others

2. **Test Leave Notifications:**
   - Disconnect one client
   - Verify remaining clients see leave message

3. **Test Private Messaging:**
   - Alice types: `/msg Bob Hello Bob!`
   - Only Bob should see the private message
   - Alice sees confirmation

4. **Test User List:**
   - Type `/users` in any client
   - Should show all connected usernames

5. **Test Duplicate Username:**
   - Try logging in with an existing username
   - Should be rejected with error message

## Requirements

- Java 11+
- Basic understanding of sockets and threads

