package com.netQuiz.client.service;

import com.netQuiz.shared.Constants;
import com.netQuiz.shared.Quiz;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class QuizService {
    
    @SuppressWarnings("unchecked")
    public List<String> getQuizList() throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            // Send request type
            out.writeUTF(Constants.QUIZ_REQUEST);
            out.writeUTF("LIST_QUIZZES");
            out.flush();
            
            ObjectInputStream ois = new ObjectInputStream(in);
            return (List<String>) ois.readObject();
        }
    }
    
    public Quiz getQuiz(String quizId) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            // Send request type
            out.writeUTF(Constants.QUIZ_REQUEST);
            out.writeUTF("GET_QUIZ");
            out.writeUTF(quizId);
            out.flush();
            
            ObjectInputStream ois = new ObjectInputStream(in);
            return (Quiz) ois.readObject();
        }
    }
    
    public int submitAnswers(String userId, String quizId, int[] answers) 
            throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            // Send request type
            out.writeUTF(Constants.QUIZ_REQUEST);
            out.writeUTF("SUBMIT_ANSWERS");
            out.writeUTF(userId);
            out.writeUTF(quizId);
            out.flush();
            
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(answers);
            oos.flush();
            
            return in.readInt();
        }
    }
}
