package com.bravos2k5.drivetool.core.model;

import com.google.api.services.drive.model.File;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class FileItem {

    @NonNull
    private String name;

    @NonNull
    private File file;

    private Directory parentFolder;

    public FileItem(@NonNull String name, @NonNull File file) {
        this.name = name;
        this.file = file;
        this.parentFolder = null;
    }

    public FileItem(@NonNull String name, @NonNull File file, Directory parentFolder) {
        this.name = name;
        this.file = file;
        this.parentFolder = parentFolder;
    }

    public String getAbsolutePath() {
        if(parentFolder == null) {
            return "/";
        }
        return parentFolder.getAbsolutePath() + "/" + name;
    }

}
