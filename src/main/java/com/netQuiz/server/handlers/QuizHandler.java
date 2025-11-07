package com.netQuiz.server.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netQuiz.shared.Constants;
import com.netQuiz.shared.Quiz;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member 1 – Quiz Module Handler
 * Handles quiz requests through unified server port
 */
public class QuizHandler {
    private Map<String, Quiz> quizzes;
    private Map<String, Integer> scores;
    private Gson gson;

    public QuizHandler() {
        this.quizzes = new ConcurrentHashMap<>();
        this.scores = new ConcurrentHashMap<>();
        this.gson = new Gson();
        initializeQuizzes();
        loadQuizzes();
    }

    private void initializeQuizzes() {
        // Sample quiz data
        List<Quiz.Question> questions1 = Arrays.asList(
            new Quiz.Question("What is the capital of France?", 
                Arrays.asList("London", "Paris", "Berlin", "Madrid"), 1),
            new Quiz.Question("Which programming language runs on the JVM?", 
                Arrays.asList("Python", "Java", "C++", "JavaScript"), 1),
            new Quiz.Question("What does TCP stand for?", 
                Arrays.asList("Transfer Control Protocol", "Transmission Control Protocol", 
                             "Transport Communication Protocol", "Technical Control Protocol"), 1)
        );
        
        List<Quiz.Question> questions2 = Arrays.asList(
            new Quiz.Question("What is 2 + 2?", 
                Arrays.asList("3", "4", "5", "6"), 1),
            new Quiz.Question("What is the largest planet in our solar system?", 
                Arrays.asList("Mars", "Jupiter", "Saturn", "Neptune"), 1),
            new Quiz.Question("Who wrote Romeo and Juliet?", 
                Arrays.asList("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"), 1)
        );
        
        quizzes.put("QUIZ001", new Quiz("QUIZ001", "General Knowledge Quiz", questions1));
        quizzes.put("QUIZ002", new Quiz("QUIZ002", "Basic Quiz", questions2));
        
        saveQuizzes();
    }

    private void saveQuizzes() {
        try (FileWriter writer = new FileWriter(Constants.QUIZZES_FILE)) {
            gson.toJson(quizzes, writer);
        } catch (IOException e) {
            System.err.println("[QUIZ] Error saving quizzes: " + e.getMessage());
        }
    }

    private void loadQuizzes() {
        File file = new File(Constants.QUIZZES_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Map<String, Quiz> loadedQuizzes = gson.fromJson(reader, 
                    new TypeToken<Map<String, Quiz>>(){}.getType());
                if (loadedQuizzes != null) {
                    quizzes.putAll(loadedQuizzes);
                }
            } catch (IOException e) {
                System.err.println("[QUIZ] Error loading quizzes: " + e.getMessage());
            }
        }
    }

    public void handleRequest(Socket socket, DataInputStream in, DataOutputStream out) {
        try {
            String command = in.readUTF();
            System.out.println("[QUIZ] Command: " + command);
            
            switch (command) {
                case "LIST_QUIZZES":
                    sendQuizList(out);
                    break;
                case "GET_QUIZ":
                    String quizId = in.readUTF();
                    sendQuiz(quizId, out);
                    break;
                case "SUBMIT_ANSWERS":
                    String userId = in.readUTF();
                    String submittedQuizId = in.readUTF();
                    int[] answers = (int[]) new ObjectInputStream(socket.getInputStream()).readObject();
                    int score = calculateScore(submittedQuizId, answers);
                    scores.put(userId, score);
                    out.writeInt(score);
                    out.flush();
                    System.out.println("[QUIZ] User " + userId + " scored " + score);
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[QUIZ] Handler error: " + e.getMessage());
        }
    }

    private void sendQuizList(DataOutputStream out) throws IOException {
        List<String> quizTitles = new ArrayList<>();
        for (Quiz quiz : quizzes.values()) {
            quizTitles.add(quiz.getId() + ":" + quiz.getTitle());
        }
        new ObjectOutputStream(out).writeObject(quizTitles);
        out.flush();
    }

    private void sendQuiz(String quizId, DataOutputStream out) throws IOException {
        Quiz quiz = quizzes.get(quizId);
        new ObjectOutputStream(out).writeObject(quiz);
        out.flush();
    }

    private int calculateScore(String quizId, int[] answers) {
        Quiz quiz = quizzes.get(quizId);
        if (quiz == null) return 0;
        
        int score = 0;
        List<Quiz.Question> questions = quiz.getQuestions();
        for (int i = 0; i < Math.min(answers.length, questions.size()); i++) {
            if (questions.get(i).isCorrect(answers[i])) {
                score++;
            }
        }
        return score;
    }
}
