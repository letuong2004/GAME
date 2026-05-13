-- ========================================
-- DATABASE: game_caro
-- QUẢN LÝ NGƯỜI CHƠI & XẾP HẠNG
-- ========================================

-- 1. TẠO DATABASE
CREATE DATABASE IF NOT EXISTS game_caro 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE game_caro;

-- 2. BẢNG NGƯỜI CHƠI
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    avatar VARCHAR(255) NOT NULL DEFAULT 'default',
    
    -- Thống kê trận đấu
    wins INT DEFAULT 0 COMMENT 'Số trận thắng',
    losses INT DEFAULT 0 COMMENT 'Số trận thua',
    draws INT DEFAULT 0 COMMENT 'Số trận hòa',
    
    -- Điểm số
    points INT DEFAULT 1000 COMMENT 'Tổng điểm (ELO)',
    
    -- Xếp hạng
    rank_tier VARCHAR(20) DEFAULT 'IRON_IV' COMMENT 'Cấp bậc xếp hạng',
    rank_position INT DEFAULT 0 COMMENT 'Vị trí trong xếp hạng',
    
    -- Thông tin khác
    win_rate FLOAT GENERATED ALWAYS AS (
        CASE 
            WHEN (wins + losses + draws) = 0 THEN 0
            ELSE ROUND((wins * 100.0) / (wins + losses + draws), 2)
        END
    ) STORED COMMENT 'Tỉ lệ thắng %',
    
    total_games INT GENERATED ALWAYS AS (wins + losses + draws) STORED COMMENT 'Tổng số trận',
    
    is_online BOOLEAN DEFAULT FALSE COMMENT 'Người chơi đang online?',
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Lần đăng nhập cuối',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo tài khoản',
    
    -- Indexes
    INDEX idx_points (points DESC),
    INDEX idx_username (username),
    INDEX idx_rank_tier (rank_tier)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG LỊCH SỬ TRẬN ĐẤU
CREATE TABLE IF NOT EXISTS match_history (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    
    -- Kết quả
    winner_id INT COMMENT 'NULL nếu hòa',
    
    -- Điểm thay đổi
    player1_points_change INT DEFAULT 0,
    player2_points_change INT DEFAULT 0,
    
    -- Thời gian
    match_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    match_duration INT COMMENT 'Thời lượng trận (giây)',
    
    -- Loại trận
    match_type ENUM('Quick Play', 'Private Room', 'Bot') DEFAULT 'Quick Play',
    
    FOREIGN KEY (player1_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (player2_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES users(user_id) ON DELETE SET NULL,
    
    INDEX idx_date (match_date DESC),
    INDEX idx_players (player1_id, player2_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. BẢNG HỆ THỐNG XẾP HẠNG
CREATE TABLE IF NOT EXISTS rank_tiers (
    tier_id INT AUTO_INCREMENT PRIMARY KEY,
    tier_name VARCHAR(20) UNIQUE NOT NULL,
    min_points INT NOT NULL,
    max_points INT NOT NULL,
    tier_icon VARCHAR(50),
    tier_description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. CHÈN DỮ LIỆU XẾPPPP HẠNG
INSERT INTO rank_tiers (tier_name, min_points, max_points, tier_icon, tier_description) VALUES
('IRON_IV', 0, 399, '⚫', 'Sắt 4 - Mới bắt đầu'),
('IRON_III', 400, 499, '⚫', 'Sắt 3'),
('IRON_II', 500, 599, '⚫', 'Sắt 2'),
('IRON_I', 600, 699, '⚫', 'Sắt 1'),
('BRONZE_IV', 700, 799, '🟤', 'Đồng 4'),
('BRONZE_III', 800, 899, '🟤', 'Đồng 3'),
('BRONZE_II', 900, 999, '🟤', 'Đồng 2'),
('BRONZE_I', 1000, 1199, '🟤', 'Đồng 1'),
('SILVER_IV', 1200, 1399, '⚪', 'Bạc 4'),
('SILVER_III', 1400, 1599, '⚪', 'Bạc 3'),
('SILVER_II', 1600, 1799, '⚪', 'Bạc 2'),
('SILVER_I', 1800, 1999, '⚪', 'Bạc 1'),
('GOLD_IV', 2000, 2299, '🟡', 'Vàng 4'),
('GOLD_III', 2300, 2599, '🟡', 'Vàng 3'),
('GOLD_II', 2600, 2899, '🟡', 'Vàng 2'),
('GOLD_I', 2900, 3199, '🟡', 'Vàng 1'),
('DIAMOND_IV', 3200, 3599, '💎', 'Kim cương 4'),
('DIAMOND_III', 3600, 3999, '💎', 'Kim cương 3'),
('DIAMOND_II', 4000, 4399, '💎', 'Kim cương 2'),
('DIAMOND_I', 4400, 4999, '💎', 'Kim cương 1'),
('MASTER', 5000, 5999, '👑', 'Master'),
('GRANDMASTER', 6000, 10000, '🏆', 'Grandmaster');

-- ========================================
-- STORED PROCEDURES - QUẢN LÝ STATS
-- ========================================

-- 1. CẬP NHẬT XẾP HẠNG THEO ĐIỂM
DELIMITER $$

CREATE PROCEDURE update_rank_tier(IN p_user_id INT)
BEGIN
    DECLARE v_points INT;
    DECLARE v_tier VARCHAR(20);
    
    SELECT points INTO v_points FROM users WHERE user_id = p_user_id;
    
    SELECT tier_name INTO v_tier 
    FROM rank_tiers 
    WHERE v_points BETWEEN min_points AND max_points
    LIMIT 1;
    
    UPDATE users 
    SET rank_tier = v_tier 
    WHERE user_id = p_user_id;
END$$

-- 2. CẬP NHẬT THẮNG/THUA/HÒA
CREATE PROCEDURE update_match_result(
    IN p_winner_id INT,
    IN p_loser_id INT,
    IN p_points_change INT,
    IN p_match_type VARCHAR(20)
)
BEGIN
    DECLARE v_winner_points INT;
    DECLARE v_loser_points INT;
    
    -- Cập nhật thắng cho người thắng
    UPDATE users 
    SET wins = wins + 1,
        points = points + p_points_change
    WHERE user_id = p_winner_id;
    
    -- Cập nhật thua cho người thua
    UPDATE users 
    SET losses = losses + 1,
        points = GREATEST(0, points - p_points_change)
    WHERE user_id = p_loser_id;
    
    -- Cập nhật xếp hạng
    CALL update_rank_tier(p_winner_id);
    CALL update_rank_tier(p_loser_id);
    
    -- Ghi lại lịch sử
    INSERT INTO match_history 
    (player1_id, player2_id, winner_id, player1_points_change, player2_points_change, match_type)
    VALUES 
    (p_winner_id, p_loser_id, p_winner_id, p_points_change, -p_points_change, p_match_type);
END$$

-- 3. CẬP NHẬT TRẬN HÒA
CREATE PROCEDURE update_match_draw(
    IN p_player1_id INT,
    IN p_player2_id INT,
    IN p_points_change INT,
    IN p_match_type VARCHAR(20)
)
BEGIN
    -- Cập nhật hòa cho cả hai
    UPDATE users 
    SET draws = draws + 1,
        points = points + p_points_change
    WHERE user_id = p_player1_id OR user_id = p_player2_id;
    
    -- Cập nhật xếp hạng
    CALL update_rank_tier(p_player1_id);
    CALL update_rank_tier(p_player2_id);
    
    -- Ghi lại lịch sử (NULL cho winner = hòa)
    INSERT INTO match_history 
    (player1_id, player2_id, winner_id, player1_points_change, player2_points_change, match_type)
    VALUES 
    (p_player1_id, p_player2_id, NULL, p_points_change, p_points_change, p_match_type);
END$$

-- 4. LẤY BẢNG XẾP HẠNG TOP 100
CREATE PROCEDURE get_leaderboard()
BEGIN
    SELECT 
        ROW_NUMBER() OVER (ORDER BY points DESC) as rank_position,
        user_id,
        username,
        nickname,
        points,
        rank_tier,
        wins,
        losses,
        draws,
        total_games,
        win_rate,
        avatar
    FROM users
    WHERE total_games > 0
    ORDER BY points DESC, wins DESC
    LIMIT 100;
END$$

-- 5. LẤY THÔNG TIN NGƯỜI CHƠI
CREATE PROCEDURE get_player_info(IN p_username VARCHAR(50))
BEGIN
    SELECT 
        user_id,
        username,
        nickname,
        avatar,
        points,
        rank_tier,
        wins,
        losses,
        draws,
        total_games,
        win_rate,
        is_online,
        last_login,
        created_at
    FROM users
    WHERE username = p_username;
END$$

-- 6. CẬP NHẬT TRẠNG THÁI ONLINE
CREATE PROCEDURE set_player_online(IN p_username VARCHAR(50), IN p_online BOOLEAN)
BEGIN
    UPDATE users 
    SET is_online = p_online,
        last_login = CURRENT_TIMESTAMP
    WHERE username = p_username;
END$$

DELIMITER ;

-- ========================================
-- VIEWS - HIỂN THỊ DỮ LIỆU
-- ========================================

-- 1. VIEW: TOP 10 NGƯỜI CHƠI
CREATE OR REPLACE VIEW top_10_players AS
SELECT 
    ROW_NUMBER() OVER (ORDER BY points DESC) as rank_position,
    username,
    nickname,
    points,
    rank_tier,
    wins,
    losses,
    draws,
    win_rate
FROM users
WHERE total_games >= 5
ORDER BY points DESC
LIMIT 10;

-- 2. VIEW: LỊCH SỬ TRẬN ĐẤU GẦN ĐÂY
CREATE OR REPLACE VIEW recent_matches AS
SELECT 
    m.match_id,
    u1.username as player1,
    u2.username as player2,
    u3.username as winner,
    m.player1_points_change,
    m.player2_points_change,
    m.match_type,
    m.match_date
FROM match_history m
LEFT JOIN users u1 ON m.player1_id = u1.user_id
LEFT JOIN users u2 ON m.player2_id = u2.user_id
LEFT JOIN users u3 ON m.winner_id = u3.user_id
ORDER BY m.match_date DESC
LIMIT 50;

-- 3. VIEW: THỐNG KÊ TOÀN SERVER
CREATE OR REPLACE VIEW server_stats AS
SELECT 
    (SELECT COUNT(*) FROM users) as total_players,
    (SELECT COUNT(*) FROM users WHERE is_online = TRUE) as online_players,
    (SELECT COUNT(*) FROM match_history) as total_matches,
    (SELECT AVG(win_rate) FROM users WHERE total_games > 0) as avg_win_rate,
    (SELECT MAX(points) FROM users) as highest_rating,
    (SELECT username FROM users ORDER BY points DESC LIMIT 1) as top_player;

-- ========================================
-- DỮ LIỆU DEMO
-- ========================================

INSERT INTO users (username, password, nickname, avatar, wins, losses, draws, points)
VALUES 
('alice', MD5('123'), 'Alice', 'lion', 45, 15, 5, 2500),
('bob', MD5('123'), 'Bob', 'tiger', 38, 22, 3, 2000),
('chi', MD5('123'), 'Chi', 'dragon', 32, 28, 8, 1500),
('duy', MD5('123'), 'Duy', 'eagle', 20, 40, 2, 800),
('eva', MD5('123'), 'Eva', 'phoenix', 55, 10, 10, 3200);

-- Cập nhật xếp hạng cho tất cả
CALL update_rank_tier(1);
CALL update_rank_tier(2);
CALL update_rank_tier(3);
CALL update_rank_tier(4);
CALL update_rank_tier(5);

-- ========================================
-- TRUY VẤNỮ THƯỜNG DÙNG
-- ========================================

-- Xem TOP 10
SELECT * FROM top_10_players;

-- Xem thông tin người chơi
CALL get_player_info('alice');

-- Xem bảng xếp hạng
CALL get_leaderboard();

-- Xem lịch sử trận đấu
SELECT * FROM recent_matches;

-- Xem thống kê server
SELECT * FROM server_stats;

-- Cập nhật kết quả trận (người alice thắng bob, +30 điểm)
-- CALL update_match_result(1, 2, 30, 'Quick Play');

-- Cập nhật trận hòa
-- CALL update_match_draw(1, 2, 10, 'Quick Play');

-- Đặt người chơi online
-- CALL set_player_online('alice', TRUE);
-- CALL set_player_online('alice', FALSE);
