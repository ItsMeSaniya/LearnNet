package com.netQuiz.shared;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String sender;
    private String content;
    private long timestamp;
    
    public enum MessageType {
        CHAT, LOGIN, LOGOUT, USER_LIST, QUIZ_REQUEST, QUIZ_RESPONSE, 
        FILE_UPLOAD, FILE_DOWNLOAD, FILE_LIST, NOTIFICATION, ANSWER_SUBMIT, SCORE
    }
    
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Message(String sender, String content, long timestamp) {
        this.type = MessageType.CHAT;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
