#!/bin/powershell
# Build Script cho Game Caro

param(
    [Parameter(Position=0)]
    [ValidateSet("build", "server", "client", "clean")]
    [string]$action = "build"
)

$JDBC_JAR = "lib/mysql-connector-java-8.0.33.jar"
$CLASS_PATH = "bin;$JDBC_JAR"

Write-Host "========== Game Caro Build Script ==========" -ForegroundColor Cyan

switch($action) {
    "build" {
        Write-Host "`n[1/3] Kiểm tra thư mục..." -ForegroundColor Yellow
        if (!(Test-Path bin)) {
            New-Item -ItemType Directory -Path bin > $null
            Write-Host "✓ Tạo thư mục bin"
        }
        
        if (!(Test-Path $JDBC_JAR)) {
            Write-Host "`n⚠ CẢNH BÁO: Không tìm thấy $JDBC_JAR" -ForegroundColor Red
            Write-Host "Vui lòng tải MySQL JDBC Driver từ:"
            Write-Host "https://mvnrepository.com/artifact/mysql/mysql-connector-java" -ForegroundColor Green
            Write-Host "Sau đó copy vào thư mục lib/" -ForegroundColor Green
        }
        
        Write-Host "`n[2/3] Biên dịch code..." -ForegroundColor Yellow
        javac -encoding UTF-8 -d bin `
            src/game/client/*.java `
            src/game/model/*.java `
            src/game/server/*.java `
            src/*.java
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Biên dịch thành công" -ForegroundColor Green
        } else {
            Write-Host "✗ Biên dịch thất bại" -ForegroundColor Red
            exit 1
        }
    }
    
    "server" {
        Write-Host "`nBiên dịch trước..." -ForegroundColor Yellow
        javac -encoding UTF-8 -d bin `
            src/game/client/*.java `
            src/game/model/*.java `
            src/game/server/*.java `
            src/*.java
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ Biên dịch thất bại" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "`n[3/3] Chạy Server..." -ForegroundColor Yellow
        Write-Host "Khởi động server trên cổng 8888..." -ForegroundColor Cyan
        Write-Host "Đảm bảo MySQL (XAMPP) đã bật!" -ForegroundColor Yellow
        Write-Host "`nServer đang chạy. Nhấn Ctrl+C để dừng.`n" -ForegroundColor Cyan
        
        java -cp $CLASS_PATH -XX:+ShowCodeDetailsInExceptionMessages game.server.Server
    }
    
    "client" {
        Write-Host "`nBiên dịch trước..." -ForegroundColor Yellow
        javac -encoding UTF-8 -d bin `
            src/game/client/*.java `
            src/game/model/*.java `
            src/game/server/*.java `
            src/*.java
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ Biên dịch thất bại" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "`n[3/3] Chạy Client..." -ForegroundColor Yellow
        Write-Host "Khởi động client..." -ForegroundColor Cyan
        Write-Host "`nClient đang chạy. Đóng cửa sổ để thoát.`n" -ForegroundColor Cyan
        
        java -cp bin -XX:+ShowCodeDetailsInExceptionMessages game.client.GuiClient
    }
    
    "clean" {
        Write-Host "`nXóa thư mục build..." -ForegroundColor Yellow
        if (Test-Path bin) {
            Remove-Item -Recurse -Force bin
            Write-Host "✓ Đã xóa thư mục bin" -ForegroundColor Green
        }
    }
}

Write-Host "`n==========================================" -ForegroundColor Cyan
