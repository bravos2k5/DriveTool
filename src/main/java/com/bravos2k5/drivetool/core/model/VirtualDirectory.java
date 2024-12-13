package com.bravos2k5.drivetool.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VirtualDirectory {

    private final List<Directory> directoryList = new ArrayList<>();
    private final List<FileItem> fileItemList = new ArrayList<>();

    public void addDirectory(Directory directory) {
        directoryList.add(directory);
    }

    public void addFileItem(FileItem fileItem) {
        fileItemList.add(fileItem);
    }

}
