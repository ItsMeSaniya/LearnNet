package com.netQuiz.shared;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String fileName;
    private long fileSize;
    private String uploader;
    private long uploadTime;
    
    public FileInfo(String fileName, long fileSize, String uploader) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploader = uploader;
        this.uploadTime = System.currentTimeMillis();
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getUploader() {
        return uploader;
    }
    
    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
    
    public long getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }
}
