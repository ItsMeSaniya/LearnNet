@echo off
echo ================================================
echo NetQuiz Swing Client Launcher
echo ================================================
echo.
echo Starting Swing client...
echo.

mvn exec:java -Dexec.mainClass=com.netQuiz.client.swing.SwingClientApp

pause
