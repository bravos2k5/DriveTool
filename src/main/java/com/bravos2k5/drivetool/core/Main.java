package com.bravos2k5.drivetool.core;

import com.bravos2k5.drivetool.core.service.DownloadService;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {

        if(!new File("credentials.json").exists()) {
            System.err.println("Không tìm thấy file credentials.json, làm theo hướng dẫn và thử lại");
            return;
        }

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
            System.err.println("Bạn không có quyền ghi dữ liệu vào đường dẫn này");
        }

    }

}
