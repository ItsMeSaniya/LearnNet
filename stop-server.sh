#!/bin/bash

echo "🛑 Stopping NetQuiz Server..."

# Kill all processes on ports 5002-5004
lsof -ti :5002,:5003,:5004 2>/dev/null | xargs kill -9 2>/dev/null

# Also kill by process name
pkill -f "NetQuizServer" 2>/dev/null

sleep 1

# Verify all stopped
if lsof -i :5002,:5003 &> /dev/null; then
    echo "⚠️  Warning: Some processes still running"
    lsof -i :5002,:5003
else
    echo "✅ All servers stopped successfully"
fi

