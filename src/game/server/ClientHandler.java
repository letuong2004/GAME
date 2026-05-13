package game.server;

import game.model.Account;
import game.model.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final PrintWriter out;
    private final BufferedReader in;
    private String username;
    private Account account;
    private Room currentRoom;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            sendLine("Chào mừng đến với Caro Server!");
            sendLine("Dùng /register hoặc /login để bắt đầu.");
            String line;
            while ((line = in.readLine()) != null) {
                process(line.trim());
            }
        } catch (IOException e) {
            server.log("Kết nối bị mất: " + socket.getRemoteSocketAddress());
        } finally {
            close();
        }
    }

    private void process(String line) {
        if (line.isEmpty()) {
            return;
        }
        if (line.startsWith("/")) {
            String[] parts = line.split(" ", 2);
            String command = parts[0].toLowerCase();
            String args = parts.length > 1 ? parts[1].trim() : "";
            switch (command) {
                case "/register":
                    handleRegister(args);
                    break;
                case "/login":
                    handleLogin(args);
                    break;
                case "/chat":
                    handleChat(args);
                    break;
                case "/create-room":
                    handleCreateRoom(args);
                    break;
                case "/list-rooms":
                    handleListRooms();
                    break;
                case "/join-room":
                    handleJoinRoom(args);
                    break;
                case "/quick-play":
                    handleQuickPlay();
                    break;
                case "/bot":
                    handleBot();
                    break;
                case "/move":
                    handleMove(args);
                    break;
                case "/forfeit":
                    handleForfeit();
                    break;
                case "/draw":
                    handleDraw();
                    break;
                case "/leave":
                    handleLeave();
                    break;
                case "/status":
                    handleStatus();
                    break;
                case "/help":
                    sendHelp();
                    break;
                default:
                    sendLine("Lệnh không hợp lệ. Gõ /help để xem danh sách lệnh.");
            }
        } else {
            if (loggedIn()) {
                server.broadcastLobby(username + ": " + line);
            } else {
                sendLine("Bạn phải đăng nhập trước khi chat. Dùng /login hoặc /register.");
            }
        }
    }

    private void handleRegister(String args) {
        if (loggedIn()) {
            sendLine("Bạn đã đăng nhập rồi.");
            return;
        }
        String[] parts = args.split(" ");
        if (parts.length < 4) {
            sendLine("Cú pháp: /register username password nickname avatar");
            return;
        }
        String username = parts[0];
        String password = parts[1];
        String nickname = parts[2];
        String avatar = parts[3];
        if (server.registerAccount(username, password, nickname, avatar)) {
            sendLine("Đăng ký thành công. Dùng /login để đăng nhập.");
            server.log("Tài khoản mới: " + username);
        } else {
            sendLine("Tên đăng nhập đã tồn tại.");
        }
    }

    private void handleLogin(String args) {
        if (loggedIn()) {
            sendLine("Bạn đã đăng nhập rồi.");
            return;
        }
        String[] parts = args.split(" ");
        if (parts.length < 2) {
            sendLine("Cú pháp: /login username password");
            return;
        }
        String username = parts[0];
        String password = parts[1];
        Account account = server.loginAccount(username, password, this);
        if (account != null) {
            this.username = username;
            this.account = account;
            sendLine("Đăng nhập thành công. Chào " + account.getNickname());
            sendHelp();
            server.broadcastLobby("[System] " + account.getNickname() + " đã online.");
            server.log("Đăng nhập: " + username);
            sendLobbyInfo();
        } else {
            sendLine("Đăng nhập thất bại. Tên đăng nhập hoặc mật khẩu sai, hoặc đã online.");
        }
    }

    private void handleChat(String args) {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để chat.");
            return;
        }
        if (args.isEmpty()) {
            sendLine("Cú pháp: /chat nội dung tin nhắn");
            return;
        }
        server.broadcastLobby(account.getNickname() + ": " + args);
    }

    private void handleCreateRoom(String args) {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để tạo phòng.");
            return;
        }
        String[] parts = args.split(" ", 2);
        String roomName = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "Phòng của " + username;
        String password = parts.length > 1 ? parts[1] : "";
        int roomId = server.createRoom(username, roomName, password);
        currentRoom = server.getRoom(roomId);
        sendLine("Phòng được tạo: " + currentRoom.getSummary());
        server.broadcastLobby("[Room] " + account.getNickname() + " tạo phòng " + roomId);
    }

    private void handleListRooms() {
        sendLine("Phòng hiện có:");
        for (Room room : server.listRooms()) {
            sendLine(room.getSummary());
        }
    }

    private void handleJoinRoom(String args) {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để vào phòng.");
            return;
        }
        String[] parts = args.split(" ");
        if (parts.length < 1) {
            sendLine("Cú pháp: /join-room roomId [password]");
            return;
        }
        try {
            int roomId = Integer.parseInt(parts[0]);
            String password = parts.length > 1 ? parts[1] : "";
            Room room = server.getRoom(roomId);
            if (room == null) {
                sendLine("Không tìm thấy phòng.");
                return;
            }
            if (room.join(username, password)) {
                currentRoom = room;
                sendLine("Bạn đã vào phòng " + roomId);
                server.broadcastRoom(room, "[Room] " + account.getNickname() + " đã vào phòng.");
                if (room.getStatus() == Room.Status.PLAYING) {
                    server.broadcastRoom(room, "Trận đấu bắt đầu! \n" + room.getBoardDisplay());
                    server.broadcastRoom(room, "Lượt của: " + room.getCurrentPlayer());
                }
            } else {
                sendLine("Không thể vào phòng. Kiểm tra mật khẩu hoặc trạng thái phòng.");
            }
        } catch (NumberFormatException e) {
            sendLine("ID phòng phải là số.");
        }
    }

    private void handleQuickPlay() {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để chơi nhanh.");
            return;
        }
        Room room = server.quickPlay(username);
        if (room == null) {
            sendLine("Không tìm thấy phòng trống. Đang tạo phòng chờ...");
            int roomId = server.createRoom(username, "Phòng nhanh", "");
            currentRoom = server.getRoom(roomId);
            sendLine("Đã tạo phòng nhanh: " + currentRoom.getSummary());
        } else {
            currentRoom = room;
            sendLine("Bạn đã được ghép vào phòng " + room.getRoomId());
            server.broadcastRoom(room, "[Room] " + account.getNickname() + " tham gia Quick Play.");
            server.broadcastRoom(room, "Trận đấu bắt đầu!\n" + room.getBoardDisplay());
            server.broadcastRoom(room, "Lượt của: " + room.getCurrentPlayer());
        }
    }

    private void handleBot() {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để chơi với bot.");
            return;
        }
        int roomId = server.createRoom(username, "Chơi với BOT", "");
        Room room = server.getRoom(roomId);
        if (room == null) {
            sendLine("Không thể tạo phòng BOT.");
            return;
        }
        room.joinBot();
        currentRoom = room;
        sendLine("Đã tạo phòng BOT: " + room.getSummary());
        server.broadcastRoom(room, "Trận đấu BOT bắt đầu!\n" + room.getBoardDisplay());
    }

    private void handleMove(String args) {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để đánh.");
            return;
        }
        if (currentRoom == null || currentRoom.getStatus() != Room.Status.PLAYING) {
            sendLine("Bạn hiện chưa ở trong trận đấu.");
            return;
        }
        String[] parts = args.split(" ");
        if (parts.length < 2) {
            sendLine("Cú pháp: /move hàng cột (vd: /move 8 12)");
            return;
        }
        try {
            int row = Integer.parseInt(parts[0]) - 1;
            int col = Integer.parseInt(parts[1]) - 1;
            boolean ok = currentRoom.placeMove(username, row, col);
            if (!ok) {
                sendLine("Nước đi không hợp lệ hoặc không đến lượt bạn.");
                return;
            }
            server.broadcastRoom(currentRoom, "Bàn cờ:\n" + currentRoom.getBoardDisplay());
            if (currentRoom.getStatus() == Room.Status.FINISHED) {
                if (currentRoom.isDraw()) {
                    server.broadcastRoom(currentRoom, "Trận hòa! Cả hai đều không có 5 quân liên tiếp.");
                    server.updateStats(currentRoom, null);
                } else {
                    server.broadcastRoom(currentRoom, "Người thắng: " + username);
                    server.updateStats(currentRoom, username);
                }
                currentRoom = null;
            } else {
                server.broadcastRoom(currentRoom, "Lượt tiếp theo: " + currentRoom.getCurrentPlayer());
                server.maybeBotMove(currentRoom);
            }
        } catch (NumberFormatException e) {
            sendLine("Hàng và cột phải là số.");
        }
    }

    private void handleForfeit() {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để xin thua.");
            return;
        }
        if (currentRoom == null || currentRoom.getStatus() != Room.Status.PLAYING) {
            sendLine("Bạn hiện không trong trận đấu.");
            return;
        }
        server.broadcastRoom(currentRoom, account.getNickname() + " xin thua.");
        server.updateStats(currentRoom, getOpponent(currentRoom));
        currentRoom.setFinished();
        currentRoom = null;
    }

    private void handleDraw() {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để xin hòa.");
            return;
        }
        if (currentRoom == null || currentRoom.getStatus() != Room.Status.PLAYING) {
            sendLine("Bạn hiện không trong trận đấu.");
            return;
        }
        server.broadcastRoom(currentRoom, account.getNickname() + " đề nghị hòa.");
        server.broadcastRoom(currentRoom, "Trận đấu kết thúc hòa.");
        server.updateStats(currentRoom, null);
        currentRoom.setFinished();
        currentRoom = null;
    }

    private void handleLeave() {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập.");
            return;
        }
        if (currentRoom != null && currentRoom.getStatus() == Room.Status.PLAYING) {
            server.broadcastRoom(currentRoom, account.getNickname() + " rời khỏi phòng.");
            server.updateStats(currentRoom, getOpponent(currentRoom));
            currentRoom.setFinished();
        }
        currentRoom = null;
        sendLine("Bạn đã rời phòng.");
    }

    private void handleStatus() {
        if (!loggedIn()) {
            sendLine("Bạn phải đăng nhập để xem trạng thái.");
            return;
        }
        sendLobbyInfo();
    }

    private void sendLobbyInfo() {
        sendLine("=== THÔNG TIN CÁ NHÂN ===");
        sendLine(account.toString());
        sendLine("========================");
        sendLine("Đang online: ");
        for (Account onlineAccount : server.getOnlineAccounts()) {
            sendLine("- " + onlineAccount.toString());
        }
        sendLine("--- Phòng có sẵn ---");
        for (Room room : server.listRooms()) {
            sendLine(room.getSummary());
        }
    }

    private void sendHelp() {
        sendLine("Lệnh hỗ trợ:");
        sendLine("/chat tin nhắn - Gửi chat chung lobby");
        sendLine("/create-room tên [mật khẩu] - Tạo phòng");
        sendLine("/list-rooms - Xem phòng");
        sendLine("/join-room id [mật khẩu] - Vào phòng");
        sendLine("/quick-play - Chơi nhanh");
        sendLine("/bot - Chơi với máy");
        sendLine("/move hàng cột - Đánh nước đi");
        sendLine("/forfeit - Xin thua");
        sendLine("/draw - Xin hòa");
        sendLine("/leave - Rời phòng");
        sendLine("/status - Xem lại lobby");
        sendLine("/help - Hiển thị lệnh này");
    }

    private String getOpponent(Room room) {
        for (String player : room.getPlayers()) {
            if (!player.equals(username)) {
                return player;
            }
        }
        return null;
    }

    private boolean loggedIn() {
        return username != null && account != null;
    }

    public void sendLine(String message) {
        out.println(message);
    }

    public void close() {
        try {
            if (account != null) {
                account.setOnline(false);
                server.removeOnline(username);
                server.broadcastLobby("[System] " + account.getNickname() + " đã offline.");
            }
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
