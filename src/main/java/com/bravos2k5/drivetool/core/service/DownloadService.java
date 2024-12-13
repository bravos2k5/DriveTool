package com.bravos2k5.drivetool.core.service;

import com.bravos2k5.drivetool.core.model.Directory;
import com.bravos2k5.drivetool.core.model.FileItem;
import com.bravos2k5.drivetool.core.model.VirtualDirectory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadService {

    private final Drive service;

    public DownloadService() {
        this.service = DriveAuthenticator.getInstance().getDrive();
    }

    private String extractIdFromUrl(String url) {

        if (!url.matches("(https?://drive\\.google\\.com/.*)")) {
            throw new IllegalArgumentException("Invalid URL");
        }

        String pattern = "[-\\w]{25,}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        return m.find() ? m.group(0) : null;

    }

    private boolean isFolder(File file) {
        return file.getMimeType().equals("application/vnd.google-apps.folder");
    }

    private void downloadFolder(String folderId, String destinationPath) throws IOException {
        String query = "'" + folderId + "' in parents and trashed = false";
        FileList fileList = service.files().list()
                .setQ(query)
                .setFields("files(id, size, name, mimeType)")
                .execute();

        java.io.File destFolder = new java.io.File(destinationPath);
        if (!destFolder.exists()) destFolder.mkdirs();

        Map<String, String> subFolderMap = new HashMap<>();
        Map<String, String> filesMap = new HashMap<>();

        List<File> files = fileList.getFiles();

        for (File file : files) {
            String subfolderPath = Paths.get(destinationPath, file.getName()).toString();
            if (isFolder(file)) {
                subFolderMap.put(file.getId(), subfolderPath);
            } else {
                filesMap.put(file.getId(), subfolderPath);
            }
        }

        if (!filesMap.isEmpty()) {
            try (ExecutorService executorService = Executors.newFixedThreadPool(Math.min(filesMap.size(), 8))) {
                for (var downloadSession : filesMap.entrySet()) {
                    executorService.submit(() -> downloadFile(downloadSession.getKey(), downloadSession.getValue()));
                }
                executorService.shutdown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (var subFolderItem : subFolderMap.entrySet()) {
            downloadFolder(subFolderItem.getKey(), subFolderItem.getValue());
        }

    }

    private void downloadMultiFolder(String destinationPath, Set<String> folderIds) {
        for (String folderId : folderIds) {
            try {
                downloadFolder(folderId, destinationPath);
            } catch (IOException e) {
                System.err.println("Lỗi khi download: " + folderId);
            }
        }
    }

    private void downloadFile(String fileId, String destinationPath) {
        try {
            OutputStream outputStream = new FileOutputStream(destinationPath);
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.close();
            System.out.println("Đã tải xong: " + destinationPath);
        } catch (IOException e) {
            new java.io.File(destinationPath).deleteOnExit();
            System.err.println("Lỗi khi tải file: " + destinationPath);
        }
    }

    private void downloadSingleFile(String fileId, String destinationFolder) throws IOException {
        File file = service.files().get(fileId).setFields("name").execute();
        downloadFile(fileId, Paths.get(destinationFolder, file.getName()).toString());
    }

    private void downloadMultiFile(String destinationFolder, Set<String> fileIds) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(Math.min(fileIds.size(), 8))) {
            for (String fileId : fileIds) {
                executorService.submit(() -> {
                    try {
                        downloadSingleFile(fileId, destinationFolder);
                    } catch (IOException e) {
                        System.err.println("Không tìm được file có id: " + fileId);
                    }
                });
            }
            executorService.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadMultiLink(String destinationFolder, List<String> links) {
        System.out.println("Đang tiến hành tải xuống vui lòng chờ...");

        new java.io.File(destinationFolder).mkdirs();

        long startTime = System.currentTimeMillis();

        Set<String> singleFileLinks = new HashSet<>();
        Set<String> folderLinks = new HashSet<>();
        String regex = "(https?://drive\\.google\\.com/.*)";

        for (String link : links) {
            if (!link.matches(regex)) {
                System.err.println("Link: " + link + " không hợp lệ");
                continue;
            }
            if (link.contains("folders"))
                folderLinks.add(extractIdFromUrl(link));
            else
                singleFileLinks.add(extractIdFromUrl(link));
        }

        if (!singleFileLinks.isEmpty())
            downloadMultiFile(destinationFolder, singleFileLinks);
        if (!folderLinks.isEmpty())
            downloadMultiFolder(destinationFolder, folderLinks);

        long endTime = System.currentTimeMillis();

        System.out.println("Đã hoàn thành tải xuống trong " + (endTime - startTime) + " ms");
    }

    private List<String> getLinksFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String> links = new ArrayList<>();
            String data;
            while ((data = reader.readLine()) != null) {
                links.add(data);
            }
            return links;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadMultiLinkFromFile(String destinationFolder, String filePath) {
        downloadMultiLink(destinationFolder, getLinksFromFile(filePath));
    }

    private VirtualDirectory createVirtualDirectoryLevel1(String path) {
        List<String> urlList = getLinksFromFile("links.txt");
        VirtualDirectory virtualDirectory = new VirtualDirectory();
        Directory vDirectory = new Directory(path,null,null);
        for (String url : urlList) {

            String id;

            try {
                id = extractIdFromUrl(url);
            } catch (IllegalArgumentException e) {
                System.err.println("URL: " + url + " không hợp lệ");
                continue;
            }

            try {
                File file = service.files().get(id).setFields("id,size,name,mimeType").execute();
                if (isFolder(file)) {
                    Directory directory = new Directory(file.getName(), file, null);
                    virtualDirectory.addDirectory(directory);
                } else {
                    FileItem fileItem = new FileItem(file.getName(), file);
                    virtualDirectory.addFileItem(fileItem);
                }
            } catch (IOException e) {
                System.err.println("URL:" + url + " không tồn tại hoặc bạn không có quyền truy cập");
            }

        }
        return virtualDirectory;
    }

    private VirtualDirectory createCompleteVirtualDirectory(VirtualDirectory virtualDirectory) {
        List<Directory> directoryList = virtualDirectory.getDirectoryList();
        for (Directory directory : directoryList) {
            completeDirectory(directory);
        }
        return virtualDirectory;
    }

    private void completeDirectory(Directory parent) {
        String query = "'" + parent.getFile().getId() + "' in parents and trashed = false";
        FileList fileList;
        try {
            fileList = service.files().list()
                    .setQ(query)
                    .setFields("files(id, size, name, mimeType)")
                    .execute();
            List<File> files = fileList.getFiles();
            for (File file : files) {
                if (isFolder(file)) {
                    Directory directory = parent.addSubFolder(file.getName(), file);
                    completeDirectory(directory);
                } else {
                    parent.addFile(file, file.getName(), true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleFastDownload(String destination, VirtualDirectory virtualDirectory) {
        List<FileItem> miniSizeFile = new ArrayList<>();
        List<FileItem> largeSizeFile = new ArrayList<>();
        for(FileItem item : virtualDirectory.getFileItemList()) {
            if (item.getFile().getSize() > 10192) {
                largeSizeFile.add(item);
            } else {
                miniSizeFile.add(item);
            }
        }
        List<Directory> directories = virtualDirectory.getDirectoryList();

    }

    private void handleClassifyFiles(Directory directory, List<FileItem> miniSizeFile, List<FileItem> largeSizeFile) {
        for (FileItem item : directory.getFiles().values()) {
            if (item.getFile().getSize() > 10192) {
                largeSizeFile.add(item);
            } else {
                miniSizeFile.add(item);
            }
        }
        for(Directory subFolder : directory.getSubFolders().values()) {
            handleClassifyFiles(subFolder,miniSizeFile,largeSizeFile);
        }
    }

}

