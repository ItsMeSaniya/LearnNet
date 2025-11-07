# 🚀 NetQuiz Quick Start Guide

## 5-Minute Setup

### Step 1: Build the Project (1 minute)

```bash
cd netquiz-backend
mvn clean package
```

### Step 2: Start the Server (30 seconds)

**Windows PowerShell:**

```powershell
.\run-server.bat
```

**Windows Command Prompt:**

```cmd
run-server.bat
```

**Mac/Linux:**

```bash
chmod +x run-server.sh
./run-server.sh
```

**Or use Maven directly:**

```bash
mvn exec:java -Dexec.mainClass="com.netQuiz.ServerMain"
```

> **Note**: First run will take longer as Maven downloads dependencies.

✅ **Success**: You should see all 5 server modules start:

```
Quiz Server started on port 5001
File Server started on port 5002
Chat Server started on port 5003
User Management Server started on port 5004
Notification Server started (UDP Broadcasting)
```

### Step 3: Launch Clients (1 minute)

Open **NEW terminal windows** (keep server running!) and run:

**Windows PowerShell:**

```powershell
.\run-client.bat
```

**Windows Command Prompt:**

```cmd
run-client.bat
```

**Mac/Linux:**

```bash
chmod +x run-client.sh
./run-client.sh
```

**Or use Maven directly:**

```bash
mvn javafx:run
```

### Step 4: Test the Application (2 minutes)

1. **Login**: Enter username "Alice" → Click Login
2. **Chat**: Type "Hello everyone!" → Click Send
3. **Quiz**: Go to Quiz tab → Select a quiz → Start Quiz → Answer → Submit
4. **Files**: Go to Files tab → Upload a test file
5. **Users**: Check Users tab to see who's online

Launch a **second client** (in a new terminal) as "Bob" to test multi-user features!

---

## What Each Tab Does

| Tab                  | What You Can Do                    |
| -------------------- | ---------------------------------- |
| 💬 **Chat**          | Send instant messages to all users |
| 📝 **Quizzes**       | Take quizzes and see your score    |
| 📁 **Files**         | Upload/download study materials    |
| 👥 **Users**         | See who's online right now         |
| 🔔 **Notifications** | Receive system announcements       |

---

## Testing Multi-User Features

### Test Chat (2 clients)

1. Open Client 1 as "Alice"
2. Open Client 2 as "Bob"
3. Send message from Alice
4. See it appear on Bob's screen
5. Reply from Bob

### Test User List

1. Start Client 1 (Alice)
2. Check Users tab shows "Alice"
3. Start Client 2 (Bob)
4. Both clients now show "Alice, Bob"
5. Close Bob's client
6. Alice's list updates to just "Alice"

### Test File Sharing

1. On Alice's client: Files tab → Upload File
2. On Bob's client: Files tab → Refresh
3. See Alice's file in the list
4. Select file → Download

### Test Notifications

1. Upload a file from any client
2. All clients receive UDP notification
3. Check Notifications tab to see the message

---

## Common Issues & Quick Fixes

### ❌ "Connection refused"

**Problem**: Client can't reach server  
**Fix**: Make sure server is running first!

### ❌ "Address already in use"

**Problem**: Port is occupied  
**Fix**: Kill the process or restart your computer

### ❌ JavaFX errors

**Problem**: JavaFX not found  
**Fix**: Use `mvn javafx:run` instead of direct Java command

### ❌ Nothing happens when clicking buttons

**Problem**: Not connected to server  
**Fix**: Restart server, then reconnect client

---

## Demo Walkthrough Script

Perfect for showing your project to someone:

```
[Start Server]
"This is a multi-module server with 5 independent components..."

[Start Client 1 - Alice]
"Here's our JavaFX client logging in as Alice..."

[Show Chat]
"Real-time chat using TCP sockets with message broadcasting..."

[Start Client 2 - Bob]
"Let's add a second user - Bob"

[Send messages between Alice and Bob]
"You can see messages appear instantly on both clients..."

[Show Users tab]
"The user presence system tracks who's online using Java NIO..."

[Take a Quiz on Alice's client]
"The quiz module uses multi-threading to handle multiple participants..."

[Upload file from Bob]
"File sharing uses buffered TCP streams for efficient transfer..."
"Notice the UDP notification appears on all clients!"

[Show Notifications tab]
"The notification system uses UDP broadcasting for lightweight announcements..."

[Logout Bob]
"Watch the user list update automatically..."
```

---

## Keyboard Shortcuts

- **Chat**: Press `Enter` in message field to send
- **Alt + Tab**: Switch between clients (if running multiple)

---

## Stop the Application

### Stop Server

Press `Ctrl + C` in the server terminal

### Close Client

Just close the window (logout happens automatically)

---

## Next Steps

1. ✅ Try all features
2. ✅ Read `TECHNICAL_DOCS.md` to understand the implementation
3. ✅ Explore the source code
4. ✅ Add your own features!

---

## Need Help?

1. Check `README.md` for detailed documentation
2. Read `TECHNICAL_DOCS.md` for technical details
3. Review server console for error messages
4. Ensure all required ports are available (5001-5005)

---

**Happy Networking! 🎓**
