@echo off
echo ================================================
echo NetQuiz Server Launcher
echo ================================================
echo.
echo Compiling project...
call mvn compile -q
echo.
echo Starting all server modules...
echo.

java -cp "target/classes;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" com.netQuiz.ServerMain

pause
