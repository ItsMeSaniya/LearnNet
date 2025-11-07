package com.netQuiz;

import com.netQuiz.server.NetQuizServer;

/**
 * Server Entry Point
 * Run this class to start the NetQuiz server with all modules
 */
public class ServerMain {
    public static void main(String[] args) {
        NetQuizServer server = new NetQuizServer();
        server.start();
    }
}
