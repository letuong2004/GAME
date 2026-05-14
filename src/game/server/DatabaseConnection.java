package game.server;

import game.model.Account;
import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/game_caro";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    public DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Kết nối MySQL thành công");
            createTableIfNotExists();
        } catch (ClassNotFoundException e) {
            System.out.println("[DB] Lỗi: MySQL JDBC Driver không tìm thấy!");
            throw new SQLException("MySQL JDBC Driver không tìm thấy", e);
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi kết nối: " + e.getMessage());
            throw e;
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(50) PRIMARY KEY," +
                "password VARCHAR(255) NOT NULL," +
                "nickname VARCHAR(100) NOT NULL," +
                "avatar VARCHAR(255) NOT NULL," +
                "wins INT DEFAULT 0," +
                "losses INT DEFAULT 0," +
                "points INT DEFAULT 1000," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] Bảng 'users' đã được kiểm tra/tạo");
        }
    }

    public boolean registerAccount(String username, String password, String nickname, String avatar) {
        String sql = "INSERT INTO users (username, password, nickname, avatar) VALUES (?, MD5(?), ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, nickname);
            pstmt.setString(4, avatar);
            pstmt.executeUpdate();
            System.out.println("[DB] Đăng ký thành công: " + username);
            return true;
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi đăng ký: " + e.getMessage());
            return false;
        }
    }

    public Account loginAccount(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = MD5(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("nickname"),
                            rs.getString("avatar")
                    );
                    System.out.println("[DB] Đăng nhập thành công: " + username);
                    return account;
                }
            }
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi đăng nhập: " + e.getMessage());
        }
        return null;
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi kiểm tra user: " + e.getMessage());
        }
        return false;
    }

    public void updateStats(String username, int wins, int losses, int points) {
        String sql = "UPDATE users SET wins = ?, losses = ?, points = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, wins);
            pstmt.setInt(2, losses);
            pstmt.setInt(3, points);
            pstmt.setString(4, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi cập nhật stats: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Đã đóng kết nối");
            }
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi đóng kết nối: " + e.getMessage());
        }
    }
}
