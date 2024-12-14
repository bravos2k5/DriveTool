package com.bravos2k5.drivetool.core;

import com.bravos2k5.drivetool.core.service.DownloadService;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(null,"Select");
        if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().canWrite()) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            DownloadService downloadService = new DownloadService();
            downloadService.start(path);
        }
        else{
            System.err.println("Bạn không có quyền ghi vào thư mục này hoặc nó không tồn tại");
        }
    }

}
