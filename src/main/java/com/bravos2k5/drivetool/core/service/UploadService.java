package com.bravos2k5.drivetool.core.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;

public class UploadService {

    private final Drive service;

    public UploadService() {
        this.service = DriveAuthenticator.getInstance().getDrive();
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

