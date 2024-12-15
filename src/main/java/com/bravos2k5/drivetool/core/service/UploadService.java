package com.bravos2k5.drivetool.core.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;
import lombok.var;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadService {

    private final Drive service;

    public UploadService() {
        this.service = DriveAuthenticator.getInstance().getDrive();
    }

    private String extractIdFolderFromUrl(String url) {

        if (!url.matches("(https?://drive\\.google\\.com/drive/folders/.*)")) {
            throw new IllegalArgumentException("Invalid URL");
        }

        String pattern = "[-\\w]{25,}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        return m.find() ? m.group(0) : null;

    }

    private File uploadFile(String filePath, String parentFolderId) {
        File fileMetadata = new File();
        fileMetadata.setName(Paths.get(filePath).getFileName().toString());

        if (parentFolderId != null) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        java.io.File file = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/octet-stream", file);
        try {
            return service.files().create(fileMetadata, mediaContent)
                    .setFields("id,name")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file: " + fileMetadata.getName());
        }
    }

    private String createNewFolder(String name, String parentFolderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(parentFolderId));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File newFolder = service.files().create(fileMetadata).setFields("id").execute();
        return newFolder.getId();
    }

    private void classifyUploadFile(String folderPath, String parentFolderId, Map<String,String> miniFiles,Map<String,String> largeFiles) throws IOException {
        java.io.File folder = new java.io.File(folderPath);
        java.io.File[] files = folder.listFiles();
        if (files != null) {
            for (var file : files) {
                if(file.isDirectory()) {
                    String newFolderId = createNewFolder(file.getName(),parentFolderId);
                    classifyUploadFile(file.getAbsolutePath(),newFolderId,miniFiles,largeFiles);
                } else {

                    if(file.length() > 10 * 1024 * 1024) {
                        largeFiles.put(file.getAbsolutePath(),parentFolderId);
                    }
                    else {
                        miniFiles.put(file.getAbsolutePath(),parentFolderId);
                    }

                }
            }
        }
    }

    private void classifyUploadFile(List<String> paths, String parentFolderId, Map<String,String> miniFiles,Map<String,String> largeFiles) throws IOException {
        for(String path : paths) {
            java.io.File file = new java.io.File(path);
            if(file.isDirectory()) {
                String newFolderId = createNewFolder(file.getName(),parentFolderId);
                classifyUploadFile(path,newFolderId,miniFiles,largeFiles);
            }
            else {
                if(file.length() > 10 * 1024 * 1024) {
                    largeFiles.put(file.getAbsolutePath(),parentFolderId);
                }
                else {
                    miniFiles.put(file.getAbsolutePath(),parentFolderId);
                }
            }
        }
    }

    private void fastUpload(List<String> paths, String parentFolderId) throws IOException {
        System.out.println("Đang tiến hành upload...");
        long startTime = System.currentTimeMillis();
        Map<String,String> miniFiles = new HashMap<>();
        Map<String,String> largeFiles = new HashMap<>();
        classifyUploadFile(paths,parentFolderId,miniFiles,largeFiles);
        if(!miniFiles.isEmpty()) {
            ExecutorService executorService = Executors.newFixedThreadPool(Math.min(miniFiles.size(),8));
            for(var file : miniFiles.entrySet()) {
                executorService.submit(() -> {
                    File uploadedFile = uploadFile(file.getKey(),file.getValue());
                    System.out.println("Đã tải lên " + uploadedFile.getName());
                });
            }
            executorService.shutdown();
            try {
                if(!executorService.awaitTermination(999, TimeUnit.DAYS)) {
                    throw new RuntimeException("Time out");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if(!largeFiles.isEmpty()) {
            for(var file : largeFiles.entrySet()) {
                File uploadedFile = uploadFile(file.getKey(),file.getValue());
                System.out.println("Đã tải lên " + uploadedFile.getName());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Upload hoàn thành trong " + (endTime - startTime) + " ms");
    }


    public void start(List<String> uploadFiles, String driveFolderUrl) {
        try {
            String driveFolderId = null;
            try {
                if (driveFolderUrl != null) {
                    driveFolderId = extractIdFolderFromUrl(driveFolderUrl);
                }
            } catch (Exception e) {
                System.err.println("URL drive không hợp lệ!");
                return;
            }
            fastUpload(uploadFiles,driveFolderId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

