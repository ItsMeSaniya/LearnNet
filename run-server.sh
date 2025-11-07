#!/bin/bash

echo "================================================"
echo "NetQuiz Server Launcher"
echo "================================================"
echo ""
echo "Starting all server modules..."
echo ""

mvn exec:java -Dexec.mainClass="com.netQuiz.ServerMain"
