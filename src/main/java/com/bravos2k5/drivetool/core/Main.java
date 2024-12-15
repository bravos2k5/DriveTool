package com.bravos2k5.drivetool.core;

import com.bravos2k5.drivetool.core.service.DownloadService;

import javax.swing.*;
import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        if (!new File("credentials.json").exists()) {
            System.err.println("Không tìm thấy file credentials.json, làm theo hướng dẫn và thử lại");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        download();
                        System.out.print("Ấn phím bất kỳ để tiếp tục...");
                        scanner.nextLine();
                        break;
                    case 2:
                        upload();
                        System.out.print("Ấn phím bất kỳ để tiếp tục...");
                        scanner.nextLine();
                        break;
                    case 3:
                        logOut();
                        clearScreen();
                        break;
                    case 0:
                        System.out.println("Thoát chương trình. Tạm biệt!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ. Vui lòng thử lại.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số hợp lệ.");
            }
        }
    }

    public static void printMenu() {
        clearScreen();
        System.out.println("===== DRIVE TOOL =====");
        System.out.println("1. Tải xuống");
        System.out.println("2. Tải lên");
        System.out.println("3. Đăng xuất");
        System.out.println("0. Thoát");
        System.out.print("Chọn chức năng: ");
    }

    public static void download() {
        clearScreen();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(null,"Select");
        if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().canWrite()) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            DownloadService downloadService = new DownloadService();
            downloadService.start(path);
            System.gc();
        }
        else{
            System.err.println("Bạn chưa chọn đường dẫn hoặc không có quyền ghi dữ liệu vào đường dẫn này");
        }
    }

    public static void upload() {
        clearScreen();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setDialogTitle("Chọn file để tải lên");

        int result = fileChooser.showDialog(null, "Tải lên");

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            if (selectedFiles.length > 0) {
                System.out.println("Các file được chọn để tải lên:");
                for (File file : selectedFiles) {
                    System.out.println(file.getAbsolutePath());
                }

                System.out.println("Chức năng tải lên đang được phát triển.");
            } else {
                System.out.println("Không có file nào được chọn.");
            }
        } else {
            System.out.println("Hủy tải lên.");
        }
    }

    public static void logOut() {
        File file = new File("tokens/StoredCredential");
        file.setWritable(true);
        if (file.delete()) {
            System.out.println("Đã đăng xuất");
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}