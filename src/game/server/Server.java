package game.server;

import game.model.Account;
import game.model.Room;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final int port;
    private final Map<String, Account> onlineAccounts;  // Các account đang online
    private final Map<String, ClientHandler> onlineClients;
    private final Map<Integer, Room> rooms;
    private final AtomicInteger roomIdGenerator;
    private final ExecutorService clientPool;
    private DatabaseConnection db;

    public Server(int port) {
        this.port = port;
        this.onlineAccounts = new ConcurrentHashMap<>();
        this.onlineClients = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.roomIdGenerator = new AtomicInteger(100);
        this.clientPool = Executors.newCachedThreadPool();
        
        // Kết nối database
        try {
            this.db = new DatabaseConnection();
            seedDemoAccounts();
        } catch (SQLException e) {
            System.out.println("[SERVER] Lỗi kết nối database: " + e.getMessage());
            System.exit(1);
        }
    }

    private void seedDemoAccounts() {
        // Thêm tài khoản demo nếu chưa tồn tại
        if (!db.userExists("alice")) {
            db.registerAccount("alice", "123", "Alice", "lion");
        }
        if (!db.userExists("bob")) {
            db.registerAccount("bob", "123", "Bob", "tiger");
        }
        if (!db.userExists("chi")) {
            db.registerAccount("chi", "123", "Chi", "dragon");
        }
    }

    public boolean registerAccount(String username, String password, String nickname, String avatar) {
        return db.registerAccount(username, password, nickname, avatar);
    }

    public Account loginAccount(String username, String password, ClientHandler handler) {
        Account account = db.loginAccount(username, password);
        if (account != null) {
            onlineAccounts.put(username, account);
            onlineClients.put(username, handler);
            return account;
        }
        return null;
    }

    public void removeOnline(String username) {
        Account account = onlineAccounts.get(username);
        if (account != null) {
            db.updateStats(username, account.getWins(), account.getLosses(), account.getPoints());
        }
        onlineAccounts.remove(username);
        onlineClients.remove(username);
    }

    public List<Account> getOnlineAccounts() {
        return new ArrayList<>(onlineAccounts.values());
    }

    public int createRoom(String owner, String name, String password) {
        int id = roomIdGenerator.incrementAndGet();
        Room room = new Room(id, owner, name, password);
        rooms.put(id, room);
        return id;
    }

    public Room getRoom(int roomId) {
        return rooms.get(roomId);
    }

    public List<Room> listRooms() {
        List<Room> list = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (room.getStatus() != Room.Status.FINISHED) {
                list.add(room);
            }
        }
        return list;
    }

    public Room quickPlay(String username) {
        for (Room room : rooms.values()) {
            if (room.getStatus() == Room.Status.WAITING && !room.isPrivateRoom() && !room.isFull()) {
                if (room.join(username, "")) {
                    return room;
                }
            }
        }
        return null;
    }

    public void broadcastLobby(String message) {
        for (ClientHandler handler : onlineClients.values()) {
            handler.sendLine(message);
        }
    }

    public void broadcastRoom(Room room, String message) {
        for (String player : room.getPlayers()) {
            ClientHandler handler = onlineClients.get(player);
            if (handler != null) {
                handler.sendLine(message);
            }
        }
    }

    public void updateStats(Room room, String winnerUsername) {
        if (room == null) {
            return;
        }
        List<String> players = room.getPlayers();
        if (winnerUsername == null) {
            for (String username : players) {
                Account account = onlineAccounts.get(username);
                if (account != null) {
                    account.addDraw();
                }
            }
        } else {
            for (String username : players) {
                Account account = onlineAccounts.get(username);
                if (account == null) {
                    continue;
                }
                if (username.equals(winnerUsername)) {
                    account.addWin();
                } else {
                    account.addLoss();
                }
            }
        }
    }

    public void maybeBotMove(Room room) {
        if (room == null || !room.isPlayingWithBot() || !"BOT".equals(room.getCurrentPlayer())) {
            return;
        }
        boolean moved = false;
        for (int r = 0; r < 20 && !moved; r++) {
            for (int c = 0; c < 20 && !moved; c++) {
                if (room.placeMove("BOT", r, c)) {
                    moved = true;
                    broadcastRoom(room, "BOT đã đánh: " + (r + 1) + " " + (c + 1));
                    broadcastRoom(room, room.getBoardDisplay());
                    if (room.getStatus() == Room.Status.FINISHED) {
                        broadcastRoom(room, "BOT đã hoàn thành trận đấu.");
                        updateStats(room, room.isDraw() ? null : room.getCurrentPlayer());
                    } else {
                        broadcastRoom(room, "Lượt tiếp theo: " + room.getCurrentPlayer());
                    }
                }
            }
        }
    }

    public void watchRooms() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            for (Room room : new ArrayList<>(rooms.values())) {
                if (room.getStatus() == Room.Status.PLAYING && room.timeoutExpired(60)) {
                    String loser = room.getCurrentPlayer();
                    String winner = room.getPlayers().stream().filter(p -> !p.equals(loser)).findFirst().orElse(null);
                    broadcastRoom(room, "Hết thời gian lượt. " + loser + " thua.");
                    if (winner != null && !winner.equals("BOT")) {
                        updateStats(room, winner);
                    } else if (winner == null) {
                        updateStats(room, null);
                    }
                    room.setFinished();
                }
            }
        }
    }

    public void log(String message) {
        System.out.println("[SERVER] " + message);
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server chạy trên cổng " + port);
            new Thread(this::adminConsole).start();
            new Thread(this::watchRooms).start();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Kết nối từ " + clientSocket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientPool.submit(handler);
            }
        }
    }

    private void adminConsole() {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while ((input = console.readLine()) != null) {
                if (input.startsWith("/announce ")) {
                    String message = input.substring(10).trim();
                    broadcastLobby("[THÔNG BÁO SERVER] " + message);
                    log("Gửi thông báo: " + message);
                } else if (input.equals("/status")) {
                    log("Online: " + onlineClients.size() + ", Phòng: " + listRooms().size());
                } else if (input.equals("/clients")) {
                    log("Người chơi online:");
                    for (Account account : getOnlineAccounts()) {
                        log("- " + account.toString());
                    }
                } else if (input.equals("/rooms")) {
                    log("Danh sách phòng:");
                    for (Room room : listRooms()) {
                        log(room.getSummary());
                    }
                } else if (input.equals("/exit")) {
                    log("Tắt server.");
                    System.exit(0);
                } else {
                    log("Lệnh admin: /announce message | /status | /clients | /rooms | /exit");
                }
            }
        } catch (Exception e) {
            log("Lỗi admin console: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        Server server = new Server(port);
        server.start();
    }
}
