package com.netQuiz.shared;

import java.io.Serializable;
import java.util.List;

public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private List<Question> questions;
    
    public Quiz(String id, String title, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.questions = questions;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public static class Question implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String question;
        private List<String> options;
        private int correctAnswer;
        
        public Question(String question, List<String> options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public void setQuestion(String question) {
            this.question = question;
        }
        
        public List<String> getOptions() {
            return options;
        }
        
        public void setOptions(List<String> options) {
            this.options = options;
        }
        
        public int getCorrectAnswer() {
            return correctAnswer;
        }
        
        public void setCorrectAnswer(int correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
        
        public boolean isCorrect(int answer) {
            return answer == correctAnswer;
        }
    }
}
