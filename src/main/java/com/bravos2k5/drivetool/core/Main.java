package com.bravos2k5.drivetool.core;

import com.bravos2k5.drivetool.core.service.DownloadService;
import com.bravos2k5.drivetool.core.service.DriveAuthenticator;
import com.bravos2k5.drivetool.core.service.UploadService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (!new File("credentials.json").exists()) {
            System.err.println("Không tìm thấy file credentials.json, làm theo hướng dẫn và thử lại");
            return;
        }

        while (true) {
            printMenu();

            try {
                String input = scanner.nextLine().trim();
                int choice = Integer.parseInt(input);

                switch (choice) {
                    case 1:
                        System.out.print("Bạn có muốn tắt máy sau khi tải xong không? (y/n): ");
                        String shutdown = scanner.nextLine().trim();
                        download();
                        if (shutdown.equalsIgnoreCase("y")) {
                            try {
                                Runtime.getRuntime().exec("shutdown -s -t 0");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        System.out.print("Ấn phím Enter để tiếp tục...");
                        scanner.nextLine();
                        break;
                    case 2:
                        upload();
                        System.out.print("Ấn phím Enter để tiếp tục...");
                        scanner.nextLine();
                        break;
                    case 3:
                        DriveAuthenticator.getInstance().logOut();
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
        clearConsole();
        System.out.println("===== DRIVE TOOL =====");
        System.out.println("1. Tải xuống");
        System.out.println("2. Tải lên");
        System.out.println("3. Đăng xuất");
        System.out.println("0. Thoát");
        System.out.print("Chọn chức năng: ");
    }

    public static void download() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(null, "Select");
        if (fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().canWrite()) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            DownloadService downloadService = new DownloadService();
            downloadService.start(path);
        } else {
            System.err.println("Bạn chưa chọn đường dẫn hoặc không có quyền ghi dữ liệu vào đường dẫn này");
        }
    }

    public static void upload() {
        List<String> uploadFiles = getUploadFiles();
        if(!uploadFiles.isEmpty()) {
            UploadService uploadService = new UploadService();
            String folderUrl;

            System.out.print("Nhập url thư mục google drive để tải lên (có thể bỏ trống): ");
            folderUrl = scanner.nextLine().trim();
            if(folderUrl.isEmpty()) {
                System.out.println("Không chọn thư mục tải lên sẽ mặc định trong drive của tôi");
            }

            uploadService.start(uploadFiles, folderUrl.isEmpty() ? null : folderUrl);
        }
    }

    public static List<String> getUploadFiles() {
        List<String> selectedPaths = new ArrayList<>();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogTitle("Chọn file hoặc thư mục để tải lên");
        int result = fileChooser.showDialog(null, "Chọn");

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            if (selectedFiles.length > 0) {

                for (File file : selectedFiles) {
                    selectedPaths.add(file.getAbsolutePath());
                }

                System.out.println("Các đường dẫn được chọn để tải lên:");
                for (String path : selectedPaths) {
                    System.out.println(path);
                }

            } else {
                System.out.println("Không có file nào được chọn.");
            }
        } else {
            System.out.println("Hủy tải lên.");
        }

        return selectedPaths;
    }

    public static void clearConsole() {
        try {
            String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}