package com.netQuiz.client.service;

import com.netQuiz.shared.Constants;
import com.netQuiz.shared.FileInfo;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    
    public void uploadFile(File file, String uploader) throws IOException {
        try (Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            
            // Send request type
            out.writeUTF(Constants.FILE_REQUEST);
            out.writeUTF("UPLOAD");
            out.writeUTF(file.getName());
            out.writeUTF(uploader);
            out.writeLong(file.length());
            out.flush();
            
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                
                byte[] buffer = new byte[Constants.BUFFER_SIZE];
                int bytesRead;
                
                while ((bytesRead = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                
                out.flush();
            }
            
            String response = in.readUTF();
            if (!"SUCCESS".equals(response)) {
                throw new IOException("File upload failed");
            }
        }
    }
    
    public void downloadFile(String fileName, File destination) throws IOException {
        try (Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            
            // Send request type
            out.writeUTF(Constants.FILE_REQUEST);
            out.writeUTF("DOWNLOAD");
            out.writeUTF(fileName);
            out.flush();
            
            String response = in.readUTF();
            long fileSize = in.readLong();
            
            if (!"SUCCESS".equals(response)) {
                throw new IOException("File not found on server");
            }
            
            try (FileOutputStream fos = new FileOutputStream(destination);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                
                byte[] buffer = new byte[Constants.BUFFER_SIZE];
                long totalBytesRead = 0;
                int bytesRead;
                
                while (totalBytesRead < fileSize && 
                       (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                
                bos.flush();
            }
        }
    }
    
    public List<FileInfo> getFileList() throws IOException {
        try (Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            
            // Send request type
            out.writeUTF(Constants.FILE_REQUEST);
            out.writeUTF("LIST");
            out.flush();
            
            List<FileInfo> fileList = new ArrayList<>();
            int count = in.readInt();
            
            for (int i = 0; i < count; i++) {
                String fileName = in.readUTF();
                long fileSize = in.readLong();
                fileList.add(new FileInfo(fileName, fileSize, "Unknown"));
            }
            
            return fileList;
        }
    }
}
