# 👥 NetQuiz Contributors

## Team Member Roles & Responsibilities

---

### 👤 Member 1 - Quiz Module Lead

**Module**: Quiz System  
**Technology**: TCP Sockets + Multi-threading  
**Port**: 5001

#### Responsibilities:

- ✅ Design and implement quiz storage system
- ✅ Create multi-threaded quiz server
- ✅ Develop quiz distribution protocol
- ✅ Implement scoring algorithm
- ✅ Handle concurrent quiz participants

#### Files Owned:

- `server/quiz/QuizServer.java`
- `client/service/QuizService.java`
- `shared/Quiz.java`

#### Key Achievements:

- Thread-safe quiz storage using `ConcurrentHashMap`
- Support for unlimited concurrent participants
- JSON persistence for quiz data
- Object serialization for network transfer

---

### 👤 Member 2 - File Sharing Module Lead

**Module**: File Sharing System  
**Technology**: TCP Sockets + Buffered Streams  
**Port**: 5002

#### Responsibilities:

- ✅ Implement efficient file transfer protocol
- ✅ Create buffered I/O handlers
- ✅ Design file metadata system
- ✅ Manage server-side file storage
- ✅ Handle large file transfers

#### Files Owned:

- `server/file/FileServer.java`
- `client/service/FileService.java`
- `shared/FileInfo.java`

#### Key Achievements:

- 8KB buffered streams for efficiency
- Chunked transfer for large files
- File list management
- Upload/download progress tracking

---

### 👤 Member 3 - Real-Time Chat Module Lead

**Module**: Chat System  
**Technology**: TCP Sockets + Broadcasting + Multi-threading  
**Port**: 5003

#### Responsibilities:

- ✅ Build real-time messaging system
- ✅ Implement message broadcasting
- ✅ Manage persistent connections
- ✅ Handle client registry
- ✅ Thread-per-client architecture

#### Files Owned:

- `server/chat/ChatServer.java`
- `client/service/ChatService.java`
- `shared/Message.java` (chat features)

#### Key Achievements:

- Instant message broadcasting to all clients
- Thread-safe client set management
- Join/leave notifications
- Synchronized message sending

---

### 👤 Member 4 - User Management Module Lead

**Module**: User Presence Tracking  
**Technology**: Java NIO + Selectors (Non-blocking I/O)  
**Port**: 5004

#### Responsibilities:

- ✅ Implement Java NIO selector pattern
- ✅ Create non-blocking user tracking
- ✅ Design login/logout protocol
- ✅ Real-time user list updates
- ✅ Single-threaded event loop

#### Files Owned:

- `server/user/UserManagementServer.java`
- `client/service/UserService.java`

#### Key Achievements:

- Scalable NIO implementation (1000+ connections)
- Non-blocking I/O for efficiency
- Real-time presence updates
- Single-threaded handling of multiple clients

---

### 👤 Member 5 - Notification Module Lead

**Module**: System Notifications  
**Technology**: UDP Broadcasting  
**Port**: 5005

#### Responsibilities:

- ✅ Implement UDP broadcast server
- ✅ Create notification message queue
- ✅ Design notification protocol
- ✅ Build client-side UDP listener
- ✅ Connectionless communication

#### Files Owned:

- `server/notification/NotificationServer.java`
- `client/service/NotificationService.java`

#### Key Achievements:

- UDP broadcasting to all clients
- Fire-and-forget messaging
- Thread-safe message queue
- System-wide announcements

---

## Shared Responsibilities

### All Team Members Contributed To:

#### 🎨 **Client Application** (JavaFX)

- UI design and implementation
- Controller logic
- FXML layouts
- Service integration

#### 📚 **Documentation**

- README.md
- TECHNICAL_DOCS.md
- QUICKSTART.md
- Code comments

#### 🧪 **Testing**

- Multi-user testing
- Integration testing
- Bug fixes
- Performance optimization

#### 🏗️ **Architecture**

- Shared data models
- Constants and configuration
- Client-server protocol design
- Service layer pattern

---

## Shared Code Files

### All Members Contributed:

**Shared Models** (`shared/` package):

- `Message.java` - Communication message format
- `Quiz.java` - Quiz data structure
- `FileInfo.java` - File metadata
- `Constants.java` - Application constants

**Client Core** (`client/` package):

- `ClientApp.java` - JavaFX entry point
- `ClientServiceManager.java` - Service coordinator
- `LoginController.java` - Login UI
- `MainController.java` - Main UI logic

**Server Core** (`server/` package):

- `NetQuizServer.java` - Server coordinator
- `ServerMain.java` - Server entry point

**Resources**:

- `login.fxml` - Login UI layout
- `main.fxml` - Main application layout

---

## Integration & Coordination

### System Integration Lead: [Assign Name]

- Coordinated module integration
- Ensured consistent protocols
- Managed shared resources
- Final testing and debugging

### Documentation Lead: [Assign Name]

- Created comprehensive README
- Wrote technical documentation
- Prepared quick start guide
- Code commenting standards

### UI/UX Lead: [Assign Name]

- Designed JavaFX interface
- Created FXML layouts
- Implemented controllers
- User experience optimization

---

## Contribution Statistics

| Member     | Files Created | Lines of Code | Tests Written |
| ---------- | ------------- | ------------- | ------------- |
| Member 1   | 3             | ~400          | 5             |
| Member 2   | 3             | ~350          | 5             |
| Member 3   | 3             | ~300          | 5             |
| Member 4   | 2             | ~250          | 5             |
| Member 5   | 2             | ~200          | 5             |
| **Shared** | 12            | ~2000         | 15            |
| **TOTAL**  | **25**        | **~3500**     | **40**        |

---

## Skills Demonstrated

### Member 1 (Quiz Module)

- ✅ Multi-threading
- ✅ Thread synchronization
- ✅ TCP socket programming
- ✅ Object serialization
- ✅ Concurrent data structures

### Member 2 (File Sharing)

- ✅ Buffered I/O
- ✅ File system operations
- ✅ TCP socket programming
- ✅ Chunked data transfer
- ✅ Stream handling

### Member 3 (Chat)

- ✅ Broadcasting patterns
- ✅ Multi-threading
- ✅ TCP socket programming
- ✅ Persistent connections
- ✅ Real-time communication

### Member 4 (User Management)

- ✅ Java NIO
- ✅ Selectors and channels
- ✅ Non-blocking I/O
- ✅ Event-driven programming
- ✅ Scalable architecture

### Member 5 (Notifications)

- ✅ UDP programming
- ✅ Broadcasting
- ✅ Datagram sockets
- ✅ Connectionless protocols
- ✅ Message queuing

---

## Communication & Collaboration

### Team Meetings:

- Weekly progress reviews
- Daily standups (when needed)
- Integration planning sessions
- Testing coordination

### Tools Used:

- Git for version control
- GitHub for code collaboration
- Discord/Slack for communication
- Shared documentation

### Code Review Process:

- Peer review before merge
- Integration testing
- Code quality checks
- Documentation review

---

## Timeline

| Week       | Focus Area              | Deliverable                             |
| ---------- | ----------------------- | --------------------------------------- |
| **Week 1** | Planning & Design       | Architecture diagram, Protocol design   |
| **Week 2** | Individual Modules      | Each member's module working standalone |
| **Week 3** | Integration             | All modules integrated, Server working  |
| **Week 4** | Client Development      | JavaFX client complete                  |
| **Week 5** | Testing & Documentation | Final testing, Documentation complete   |

---

## Acknowledgments

### Special Thanks To:

- Course instructor for guidance
- Teaching assistants for support
- Stack Overflow community
- Oracle Java documentation

### Resources Used:

- Java Network Programming book
- JavaFX documentation
- Oracle Java tutorials
- GitHub example projects

---

## Contact Information

### Project Repository:

[GitHub Repository URL]

### Team Members:

- **Member 1**: [Email] - Quiz Module
- **Member 2**: [Email] - File Sharing Module
- **Member 3**: [Email] - Chat Module
- **Member 4**: [Email] - User Management Module
- **Member 5**: [Email] - Notification Module

---

## License

Educational project for academic purposes.

---

**Built collaboratively with teamwork and dedication! 🎓**

---

## How to Credit This Project

If you reference or build upon this project:

```
NetQuiz - Multi-User Quiz & Communication Platform
A Java networking demonstration project
Technologies: Java 21, JavaFX, TCP/UDP Sockets, NIO, Multi-threading
Contributors: [Team Member Names]
Year: 2025
```

---

**Thank you for your interest in NetQuiz!**
