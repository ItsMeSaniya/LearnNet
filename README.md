# LearnNet

Network programming assignment for learning socket programming and multithreading.

## Features

- Chat server with multiple client support
- Quiz system
- File sharing
- User management
- Notifications

## Quick Start

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

## Requirements

- Java 11+
- Basic understanding of sockets and threads

