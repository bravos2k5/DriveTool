package com.bravos2k5.drivetool.core.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
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

    public String uploadFile(String filePath, String parentFolderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(Paths.get(filePath).getFileName().toString());

        if (parentFolderId != null) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        java.io.File file = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/octet-stream", file);
        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return uploadedFile.getId();
    }

}

