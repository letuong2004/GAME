# HƯỚNG DẪN SETUP DATABASE XAMPP

## 1. TẢI MYSQL JDBC DRIVER

### Cách 1: Tải từ Maven Central (Dễ nhất)
1. Truy cập: https://mvnrepository.com/artifact/mysql/mysql-connector-java
2. Tìm phiên bản 8.0.33 (hoặc mới nhất)
3. Click "JAR" để tải file `mysql-connector-java-8.0.33.jar`
4. Copy file vào thư mục: `GAME/lib/`

### Cách 2: Dùng Maven/Gradle (Nếu bạn có)
- Nếu dùng Maven, thêm dependency vào pom.xml
- Nếu dùng Gradle, thêm dependency vào build.gradle

## 2. CẤU HÌNH XAMPP

### Bước 1: Mở XAMPP Control Panel
1. Khởi động XAMPP Control Panel
2. Click "Start" trên Apache (nếu cần)
3. Click "Start" trên MySQL

### Bước 2: Truy cập phpMyAdmin
1. Mở trình duyệt
2. Truy cập: http://localhost/phpmyadmin
3. Đăng nhập với:
   - Username: root
   - Password: (để trống)

### Bước 3: Tạo Database
1. Trong phpMyAdmin, click tab "SQL"
2. Dán đoạn code dưới và click "Go":

```sql
-- Tạo database
CREATE DATABASE IF NOT EXISTS game_caro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tạo bảng
CREATE TABLE IF NOT EXISTS game_caro.users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    avatar VARCHAR(255) NOT NULL,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    points INT DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

3. Hoặc sử dụng lệnh command line:
```
mysql -u root
CREATE DATABASE game_caro;
USE game_caro;
CREATE TABLE users (...);
```

## 3. CẬP NHẬT CLASSPATH BIÊN DỰ

### Cach 1: Update classpath khi chạy (Dễ)
Khi chạy server, thêm jar vào classpath:

```powershell
javac -cp lib/mysql-connector-java-8.0.33.jar -d bin src/game/client/*.java src/game/model/*.java src/game/server/*.java src/*.java

java -cp bin;lib/mysql-connector-java-8.0.33.jar game.server.Server
```

### Cách 2: Copy jar vào thư mục lib (Nếu có)
1. Copy file jar vào thư mục `GAME/lib/`
2. Java sẽ tự động tìm thấy

## 4. KIỂM TRA KẾT NỐI

### Khi chạy server, bạn sẽ thấy:
```
[SERVER] ...
[DB] Kết nối MySQL thành công
[DB] Bảng 'users' đã được kiểm tra/tạo
[DB] Đăng ký thành công: alice
[DB] Đăng ký thành công: bob
[DB] Đăng ký thành công: chi
[SERVER] Server chạy trên cổng 8888
```

### Nếu lỗi:
- Kiểm tra MySQL có đang chạy không
- Kiểm tra database/bảng đã được tạo chưa
- Kiểm tra MySQL JDBC driver đã ở trong lib chưa

## 5. TEST ĐĂNG NHẬP/ĐĂNG KÝ

1. Chạy server: `java -cp bin;lib/mysql-connector-java-8.0.33.jar game.server.Server`
2. Chạy client: `java -cp bin game.client.GuiClient`
3. Đăng nhập với tài khoản demo:
   - Username: alice, Password: 123
   - Username: bob, Password: 123
   - Username: chi, Password: 123
4. Hoặc đăng ký tài khoản mới ở tab "Đăng ký"

## 6. CÁCH KIỂM TRA DỮ LIỆU

Vào phpMyAdmin → Database game_caro → Table users → Browse

Hoặc sử dụng SQL:
```sql
SELECT * FROM game_caro.users;
```

## GHI CHÚ

- Nếu dùng MySQL 8.0+, cần tải mysql-connector-java 8.0.x
- Nếu dùng MySQL 5.7, dùng mysql-connector-java 5.1.x
- Mật khẩu MySQL mặc định là trống (root user)
- Nếu cài đặt mật khẩu MySQL, cập nhật trong DatabaseConnection.java
