# SETUP DATABASE - HƯỚNG DẪN NHANH

## BƯỚC 1: TẢI MYSQL JDBC DRIVER

### Cách nhanh nhất:
1. Truy cập: https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/
2. Click file: `mysql-connector-java-8.0.33.jar` để tải
3. Copy file vào thư mục `GAME/lib/` trong project

### Hoặc dùng lệnh (nếu có curl):
```powershell
cd GAME/lib
curl -O https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
```

---

## BƯỚC 2: TẠO DATABASE XAMPP

### Khởi động XAMPP:
1. Mở XAMPP Control Panel
2. Click **Start** cho MySQL (xanh lá nghĩa là đang chạy)

### Tạo database:
1. Mở trình duyệt, truy cập: **http://localhost/phpmyadmin**
2. Copy đoạn SQL dưới và paste vào tab SQL:

```sql
CREATE DATABASE IF NOT EXISTS game_caro 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE game_caro;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    avatar VARCHAR(255) NOT NULL,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    points INT DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

3. Click **Execute** (hoặc Go)

---

## BƯỚC 3: CHẠY PROJECT

### Cách 1: Dùng Build Script (EASIEST)
```powershell
# Biên dịch
.\build.ps1 build

# Chạy Server
.\build.ps1 server

# Chạy Client (terminal khác)
.\build.ps1 client
```

### Cách 2: Dùng lệnh thủ công
```powershell
# Biên dịch
javac -d bin src/game/client/*.java src/game/model/*.java src/game/server/*.java src/*.java

# Chạy Server
java -cp bin;lib/mysql-connector-java-8.0.33.jar -XX:+ShowCodeDetailsInExceptionMessages game.server.Server

# Chạy Client (terminal khác)
java -cp bin -XX:+ShowCodeDetailsInExceptionMessages game.client.GuiClient
```

---

## KIỂM TRA KẾT NỐI

Khi server chạy, bạn sẽ thấy:
```
[DB] Kết nối MySQL thành công
[DB] Bảng 'users' đã được kiểm tra/tạo
[DB] Đăng ký thành công: alice
[DB] Đăng ký thành công: bob
[DB] Đăng ký thành công: chi
[SERVER] Server chạy trên cổng 8888
```

---

## TEST ĐĂNG NHẬP

**Tài khoản demo (tự động thêm vào database):**
- Username: `alice`, Password: `123`
- Username: `bob`, Password: `123`
- Username: `chi`, Password: `123`

**Hoặc đăng ký tài khoản mới** ở tab "Đăng ký"

---

## LỖI THƯỜNG GẶP

| Lỗi | Nguyên nhân | Cách sửa |
|-----|-----------|---------|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Thiếu MySQL JDBC JAR | Tải jar và copy vào lib/ |
| `Connection refused` | MySQL không chạy | Khởi động MySQL từ XAMPP |
| `Access denied for user 'root'` | Sai mật khẩu MySQL | Cập nhật DatabaseConnection.java |
| `Unknown database 'game_caro'` | Chưa tạo database | Tạo database bằng phpMyAdmin |

---

## CHI TIẾT TẬP TIN ĐƯỢC THÊM

1. **`src/game/server/DatabaseConnection.java`** - Kết nối database
2. **`DATABASE_SETUP.md`** - Hướng dẫn chi tiết
3. **`build.ps1`** - Script build PowerShell

---

## CẤU HÌNH NÂNG CAO

Nếu MySQL của bạn có mật khẩu, chỉnh sửa trong `DatabaseConnection.java`:

```java
private static final String PASSWORD = "your_password_here";
```

---

**Bây giờ bạn đã có đầy đủ để test đăng nhập/đăng ký!** 🎉
