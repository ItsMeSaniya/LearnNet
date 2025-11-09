package com.netQuiz.server.handlers;

import com.netQuiz.server.notification.NotificationServer;
import com.netQuiz.shared.Constants;
import com.netQuiz.shared.FileInfo;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private Path filesDirectory;
    private NotificationServer notificationServer;

    public FileHandler(NotificationServer notificationServer) {
        this.notificationServer = notificationServer;
        this.filesDirectory = Paths.get(Constants.FILES_DIRECTORY);
        createFilesDirectory();
    }

    private void createFilesDirectory() {
        try {
            if (!Files.exists(filesDirectory)) {
                Files.createDirectories(filesDirectory);
            }
        } catch (IOException e) {
            System.err.println("[FILE] Error creating files directory: " + e.getMessage());
        }
    }

    public void handleRequest(Socket socket, DataInputStream in, DataOutputStream out) {
        try {
            String command = in.readUTF();
            System.out.println("[FILE] Command: " + command);

            switch (command) {
                case "UPLOAD":
                    handleUpload(in, out);
                    break;
                case "DOWNLOAD":
                    handleDownload(in, out);
                    break;
                case "LIST":
                    handleList(out);
                    break;
            }

        } catch (IOException e) {
            System.err.println("[FILE] Handler error: " + e.getMessage());
        }
    }

    private void handleUpload(DataInputStream in, DataOutputStream out) throws IOException {
        String fileName = in.readUTF();
        String uploader = in.readUTF();
        long fileSize = in.readLong();

        // Notification
        if (notificationServer != null) {
            notificationServer.sendNotification(fileName + " is uploaded by " + uploader);
        }

        File file = filesDirectory.resolve(fileName).toFile();

        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            long totalBytesRead = 0;
            int bytesRead;

            while (totalBytesRead < fileSize && (bytesRead = in.read(buffer, 0,
                    (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            bos.flush();
            System.out.println("[FILE] Uploaded: " + fileName + " by " + uploader +
                    " (" + fileSize + " bytes)");

            out.writeUTF("SUCCESS");
            out.flush();
        } catch (IOException e) {
            System.err.println("[FILE] Upload error: " + e.getMessage());
            out.writeUTF("ERROR");
            out.flush();
        }
    }

    private void handleDownload(DataInputStream in, DataOutputStream out) throws IOException {
        String fileName = in.readUTF();
        File file = filesDirectory.resolve(fileName).toFile();

        if (!file.exists() || !file.isFile()) {
            out.writeUTF("ERROR");
            out.writeLong(0);
            out.flush();
            return;
        }

        out.writeUTF("SUCCESS");
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
            System.out.println("[FILE] Downloaded: " + fileName);
        }
    }

    private void handleList(DataOutputStream out) throws IOException {
        File[] files = filesDirectory.toFile().listFiles();
        List<FileInfo> fileInfoList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileInfoList.add(new FileInfo(file.getName(), file.length(), "Unknown"));
                }
            }
        }

        out.writeInt(fileInfoList.size());
        for (FileInfo fileInfo : fileInfoList) {
            out.writeUTF(fileInfo.getFileName());
            out.writeLong(fileInfo.getFileSize());
        }
        out.flush();
    }
}
