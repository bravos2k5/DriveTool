# Google Drive API Downloader/Uploader Tool
# Need JRE/JDK version 8+
## Hướng Dẫn Lấy Credentials (Obtaining Credentials)

### Bước 1: Truy Cập Google Cloud Console
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo một dự án mới hoặc chọn dự án hiện tại

### Bước 2: Kích Hoạt Google Drive API
1. Chọn "Enable APIs and Services"
2. Tìm và chọn "Google Drive API"
3. Nhấn nút "Enable"

### Bước 3: Tạo Credentials
1. Chuyển đến mục "Credentials"
2. Nhấn "Create Credentials"
3. Chọn "OAuth client ID"
4. Chọn loại ứng dụng: "Desktop app"
5. Đặt tên cho ứng dụng
6. Nhấn "Create"

### Bước 4: Tải Xuống Credentials
1. Sau khi tạo, nhấn nút tải xuống
2. Lưu file `credentials.json` vào thư mục dự án của bạn

## Chức Năng Ứng Dụng (Application Features)

### Tải Xuống (Download)
- Mở file `download-url.txt`
- Dán các đường link Google Drive
- Mỗi link được ngăn cách bằng dấu xuống dòng
- Ví dụ:
  ```
  https://drive.google.com/file/d/link1
  https://drive.google.com/file/d/link2
  https://drive.google.com/file/d/link3
  ```
- Chọn đường dẫn lưu file
- Ngồi ăn bánh uống trà đợi tải xong

### Tải Lên (Upload)
- Sử dụng FileChooser để chọn file/thư mục
- Nhấn nút "Select" để bắt đầu quá trình tải lên

---

# Google Drive API Downloader/Uploader Tool
## Obtaining Credentials

### Step 1: Access Google Cloud Console
1. Visit [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing project

### Step 2: Enable Google Drive API
1. Choose "Enable APIs and Services"
2. Find and select "Google Drive API"
3. Press the "Enable" button

### Step 3: Create Credentials
1. Go to "Credentials" section
2. Click "Create Credentials"
3. Select "OAuth client ID"
4. Choose application type: "Desktop app"
5. Name your application
6. Click "Create"

### Step 4: Download Credentials
1. After creation, press the download button
2. Save the `credentials.json` file in your project directory

## Application Features

### Download
- Open `download-url.txt`
- Paste Google Drive links
- Separate links with line breaks
- Example:
  ```
  https://drive.google.com/file/d/link1
  https://drive.google.com/file/d/link2
  https://drive.google.com/file/d/link3
  ```
- Select destination to save files

### Upload
- Use FileChooser to select files/folders
- Press "Select" button to start upload process

## Lưu Ý Quan Trọng (Important Notes)
- Giữ bảo mật file `credentials.json`
- Không chia sẻ file này công khai
- Keep `credentials.json` file secure
- Do not share this file publicly
