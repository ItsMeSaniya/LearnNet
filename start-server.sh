#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║            🚀 NetQuiz Server Starter (Port 5002-5003)           ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

# Kill any existing servers on required ports
echo "🛑 Stopping any existing servers..."
lsof -ti :5002,:5003,:5004 2>/dev/null | xargs kill -9 2>/dev/null
sleep 1

# Verify ports are free
echo "✅ Checking ports are free..."
if lsof -i :5002,:5003 &> /dev/null; then
    echo "❌ ERROR: Some ports are still in use!"
    echo "Ports in use:"
    lsof -i :5002,:5003
    exit 1
else
    echo "✅ All ports (5002, 5003) are free"
fi

echo ""
echo "🔨 Building project..."
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting NetQuiz Server..."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    mvn exec:java -Dexec.mainClass="com.netQuiz.server.NetQuizServer"
else
    echo "❌ Build failed!"
    exit 1
fi

