package com.bravos2k5.drivetool.core.service;

import com.bravos2k5.drivetool.core.model.Directory;
import com.bravos2k5.drivetool.core.model.FileItem;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        return file != null &&
                file.getMimeType().equals("application/vnd.google-apps.folder");
    }

    private void downloadFile(FileItem file) {
        try {
            java.io.File f = new java.io.File(file.getParentPath());
            f.mkdirs();
            try(OutputStream outputStream = new FileOutputStream(file.getAbsolutePath())) {
                service.files().get(file.getFile().getId()).executeMediaAndDownloadTo(outputStream);
                System.out.println("Đã tải xong: " + file.getAbsolutePath());
                return;
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi tải file: " + file.getAbsolutePath());
        }
    }

    private List<String> getLinksFromFile(String filePath) throws IOException {
        new java.io.File(filePath).createNewFile();
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

    private Directory createVirtualDirectory(String path) {
        List<String> urlList = null;
        try {
            urlList = getLinksFromFile("download-url.txt");
        } catch (IOException e) {
            System.err.println("Xảy ra lỗi khi đọc file download-url.txt");
        }
        Directory vDirectory = new Directory(path, null, null);

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
                    vDirectory.addSubFolder(file.getName(), file);
                } else {
                    vDirectory.addFile(file.getName(), file, true);
                }
            } catch (IOException e) {
                System.err.println("URL:" + url + " không tồn tại hoặc bạn không có quyền truy cập");
            }

        }

        for (Directory directory : vDirectory.getSubFolders().values()) {
            completeDirectory(directory);
        }

        return vDirectory;
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
                    parent.addFile(file.getName(), file, true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fastDownload(Directory virtualDirectory) {
        List<FileItem> miniSizeFile = new ArrayList<>();
        List<FileItem> largeSizeFile = new ArrayList<>();
        handleClassifyFiles(virtualDirectory, miniSizeFile, largeSizeFile);
        if (!miniSizeFile.isEmpty()) {
            try (ExecutorService executorService = Executors.newFixedThreadPool(Math.min(miniSizeFile.size(), 8))) {
                for (FileItem item : miniSizeFile) {
                    executorService.submit(() -> downloadFile(item));
                }
                executorService.shutdown();
                if (!executorService.awaitTermination(999,TimeUnit.DAYS)) {
                    throw new RuntimeException("Lỗi tải xuống!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (!largeSizeFile.isEmpty()) {
            for (FileItem item : largeSizeFile) {
                downloadFile(item);
            }
        }
    }

    private void handleClassifyFiles(Directory directory, List<FileItem> miniSizeFile, List<FileItem> largeSizeFile) {
        for (FileItem item : directory.getFiles().values()) {
            if (item.getFile().getSize() > 10 * 1024 * 1024) {
                largeSizeFile.add(item);
            } else {
                miniSizeFile.add(item);
            }
        }
        for (Directory subFolder : directory.getSubFolders().values()) {
            handleClassifyFiles(subFolder, miniSizeFile, largeSizeFile);
        }
    }

    public void start(String destination) {
        System.out.println("Đang chuẩn bị dữ liệu tải xuống, vui lòng chờ...");
        Directory virtualDirectory = createVirtualDirectory(destination);
        long totalSize = virtualDirectory.getSize();
        System.out.println("Tổng dung lượng là: " + totalSize / 1024 + " MB");
        if(new java.io.File(destination).getUsableSpace() / 1024 <= totalSize) {
            System.err.println("Không đủ dung lượng để tải xuống, giải phóng bớt dung lượng và thử lại");
        }
        long startTime = System.currentTimeMillis();
        fastDownload(virtualDirectory);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + " ms");
    }

}
