# 🚀 Quick Start Guide - NetQuiz Chat

## ⚠️ IMPORTANT: Start Server FIRST!

The login will hang/buffer if the server is not running.

---

## Step-by-Step Instructions

### 1️⃣ **Terminal 1: Start the Server**

```bash
cd /Users/pawanhasthika/Documents/network\ programming\ assignment/LearnNet
./start-server.sh
```

**Wait for this output:**
```
======================================================================
               NetQuiz Multi-Module Server
======================================================================
  Main Server Port (TCP):        5002
  Notifications Port (UDP):      5003
======================================================================
[CHAT] Service started
  Server Status: ONLINE
  Waiting for client connections...
======================================================================
```

✅ **Server is ready when you see "Server Status: ONLINE"**

---

### 2️⃣ **Terminal 2: Start the Client**

```bash
cd /Users/pawanhasthika/Documents/network\ programming\ assignment/LearnNet
mvn javafx:run
```

**What should happen:**
1. Login window appears
2. Enter any username (e.g., "Pawan")
3. Click "Login"
4. Status should change:
   - "Connecting to server..." ✓
   - "Logging in..." ✓
   - "Connecting to chat..." ✓
5. Main window opens with tabs

---

### 3️⃣ **Use the Chat**

1. Click the **💬 Chat** tab
2. You should see:
   ```
   ╔══════════════════════════════════════╗
   ║    Welcome to NetQuiz Chat Room      ║
   ╠══════════════════════════════════════╣
   ║ Connected as: Pawan                  ║
   ║ Time: 12:34:56                       ║
   ╚══════════════════════════════════════╝
   ```
3. Type a message in the text field
4. Press **Enter** or click **Send**
5. Message appears with timestamp!

---

### 4️⃣ **Test Multi-User Chat (Optional)**

**Terminal 3: Start another client**
```bash
mvn javafx:run
```

1. Login with different username (e.g., "Alice")
2. Go to Chat tab
3. Send messages between the two clients
4. Messages appear in real-time! 🎉

---

## 🐛 Troubleshooting

### Problem: Login just shows "Connecting..." and hangs

**Solution:** The server is not running!
1. Check Terminal 1 - is the server running?
2. If not, start it: `./start-server.sh`
3. Wait until you see "Server Status: ONLINE"
4. Then try login again

### Problem: "Cannot connect to server" error

**Cause:** Server not running on port 5002

**Fix:**
```bash
# Check if something is using port 5002
lsof -i :5002

# Kill it if needed
kill -9 <PID>

# Restart server
./start-server.sh
```

### Problem: "Connection refused" error

**Cause:** Server crashed or stopped

**Fix:** Restart the server in Terminal 1

### Problem: Login window appears but is blank/frozen

**Cause:** JavaFX rendering issue

**Fix:**
1. Close the window
2. Stop the client (Ctrl+C in terminal)
3. Run again: `mvn javafx:run`

---

## ✅ Success Checklist

- [ ] Server started and shows "ONLINE"
- [ ] Client login window appears
- [ ] Can enter username
- [ ] Login succeeds (main window opens)
- [ ] Chat tab shows welcome banner
- [ ] Can send messages
- [ ] Messages show with timestamps
- [ ] No "Broken Pipe" errors
- [ ] Multiple clients can chat together

---

## 🎯 Quick Commands Reference

**Start Server:**
```bash
./start-server.sh
```

**Start Client:**
```bash
mvn javafx:run
```

**Stop Server:**
Press `Ctrl+C` in Terminal 1

**Stop Client:**
Press `Ctrl+C` in Terminal 2 or close the window

**Recompile After Changes:**
```bash
mvn clean compile
```

---

## 📊 Expected Server Console Output

When client connects:
```
[CONNECTION] Client connected: 127.0.0.1
[REQUEST] USER from 127.0.0.1
[USER] Login: Pawan
[CONNECTION] Client connected: 127.0.0.1
[REQUEST] CHAT from 127.0.0.1
[CHAT] User joined: Pawan
```

When client sends message:
```
[CHAT] Broadcasting from Pawan: Hello World
```

---

## 🎨 What You Should See

### Login Screen:
```
┌─────────────────────────┐
│   NetQuiz - Login       │
├─────────────────────────┤
│                         │
│  Username: [________]   │
│                         │
│  Status: Ready          │
│                         │
│      [  Login  ]        │
│                         │
└─────────────────────────┘
```

### Main Application:
```
┌────────────────────────────────────────────┐
│         NetQuiz Platform                   │
├────────────────────────────────────────────┤
│ 💬 Chat | 📝 Quizzes | 📁 Files | 👥 Users │
├────────────────────────────────────────────┤
│  ╔══════════════════════════════════════╗ │
│  ║  Welcome to NetQuiz Chat Room        ║ │
│  ╠══════════════════════════════════════╣ │
│  ║  Connected as: Pawan                 ║ │
│  ╚══════════════════════════════════════╝ │
│                                            │
│  [12:34:56] You: Hello World              │
│  [12:35:01] Alice: Hi there!              │
│                                            │
│  Type message... [          ]  [Send]     │
└────────────────────────────────────────────┘
```

---

## 🔥 Pro Tips

1. **Always start server first** - Save yourself the headache!
2. **Check server terminal** - Look for connection logs
3. **Use descriptive usernames** - Makes testing easier
4. **Test with 2+ clients** - Verify real-time broadcasting
5. **Watch timestamps** - Confirm messages are fresh

---

## 📝 Need Help?

If you see any of these errors, refer to troubleshooting above:
- ❌ Cannot connect to server
- ❌ Connection refused
- ❌ Connection timeout
- ❌ Broken Pipe
- 🔄 Login buffering/hanging

**Most common fix:** Make sure the server is running! 🚀
