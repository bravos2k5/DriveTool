package com.bravos2k5.drivetool.core.model;

import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.TreeMap;

@Getter
@Setter
public class Directory {

    private File file;

    private String name;

    private Directory parentFolder;

    private TreeMap<String, Directory> subFolders;

    private TreeMap<String, FileItem> files;

    public Directory(String name, File file, Directory parentFolder) {
        this.file = file;
        this.parentFolder = parentFolder;
        this.name = name;
        this.subFolders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.files = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public Directory(String name, File file) {
        this.file = file;
        this.parentFolder = null;
        this.name = name;
        this.subFolders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.files = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public Directory addSubFolder(String name, File file) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Folder name and driveId mustn't empty");
        }

        if (subFolders.containsKey(name) && files.containsKey(name)) {
            throw new IllegalArgumentException("This name is already exist");
        }

        Directory newDirectory = new Directory(name, file, this);
        subFolders.put(name, newDirectory);
        return newDirectory;
    }

    public Directory addSubFolder(Directory directory) {
        return addSubFolder(directory.getName(), directory.getFile());
    }

    public FileItem addFile(File file, String name, boolean override) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Folder name and driveId mustn't empty");
        }

        if (!override && subFolders.containsKey(name) && files.containsKey(name)) {
            throw new IllegalArgumentException("This name is already exist");
        }

        FileItem fileItem = new FileItem(name, file, this);
        return files.put(name, fileItem);
    }

    public Directory getFolder(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Folder name mustn't empty");
        }
        return subFolders.get(name);
    }

    public FileItem getFile(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name mustn't empty");
        }
        return files.get(name);
    }

    public String getAbsolutePath() {
        if (parentFolder == null) {
            return name;
        }
        return parentFolder.getAbsolutePath() + "/" + name;
    }

    public void deleteFile(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name mustn't empty");
        }
        files.remove(name);
    }

    public void deleteFolder(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Folder name mustn't empty");
        }
        subFolders.remove(name);
    }

    public long getSize() {
        long size = 0;
        for (var file : files.entrySet()) {
            size += file.getValue().getFile().getSize();
        }
        for (var folder : subFolders.entrySet()) {
            size += folder.getValue().getSize();
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Directory directory = (Directory) o;
        return Objects.equals(file, directory.file) &&
                Objects.equals(name, directory.name) &&
                Objects.equals(parentFolder, directory.parentFolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, name, parentFolder);
    }

}
