package game.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public enum Status {
        WAITING,
        PLAYING,
        FINISHED
    }

    private final int roomId;
    private final String owner;
    private final String name;
    private final String password;
    private final boolean privateRoom;
    private final List<String> players;
    private final Board board;
    private Status status;
    private String currentPlayer;
    private char currentSymbol;
    private boolean vsBot;
    private long turnStartMillis;

    public Room(int roomId, String owner, String name, String password) {
        this.roomId = roomId;
        this.owner = owner;
        this.name = name;
        this.password = password == null ? "" : password;
        this.privateRoom = this.password.length() > 0;
        this.players = new ArrayList<>();
        this.players.add(owner);
        this.board = new Board(20);
        this.status = Status.WAITING;
        this.currentPlayer = owner;
        this.currentSymbol = 'X';
        this.vsBot = false;
        this.turnStartMillis = System.currentTimeMillis();
    }

    public int getRoomId() {
        return roomId;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public String getPassword() {
        return password;
    }

    public synchronized boolean join(String username, String password) {
        if (status != Status.WAITING || players.size() >= 2) {
            return false;
        }
        if (privateRoom && !this.password.equals(password)) {
            return false;
        }
        players.add(username);
        status = Status.PLAYING;
        turnStartMillis = System.currentTimeMillis();
        return true;
    }

    public synchronized void joinBot() {
        if (players.size() == 1) {
            players.add("BOT");
            status = Status.PLAYING;
            vsBot = true;
            turnStartMillis = System.currentTimeMillis();
        }
    }

    public synchronized boolean isFull() {
        return players.size() >= 2;
    }

    public synchronized List<String> getPlayers() {
        return new ArrayList<>(players);
    }

    public synchronized Status getStatus() {
        return status;
    }

    public synchronized String getStatusLabel() {
        if (status == Status.WAITING) {
            return "Đang đợi" + (privateRoom ? " (Mật khẩu)" : "");
        }
        if (status == Status.PLAYING) {
            return "Đang chơi";
        }
        return "Kết thúc";
    }

    public synchronized String getBoardDisplay() {
        return board.toDisplay();
    }

    public synchronized String getCurrentPlayer() {
        return currentPlayer;
    }

    public synchronized boolean placeMove(String username, int row, int col) {
        if (status != Status.PLAYING || !username.equals(currentPlayer)) {
            return false;
        }
        char symbol = currentSymbol;
        if (!board.place(row, col, symbol)) {
            return false;
        }
        if (board.checkWin(row, col, symbol)) {
            status = Status.FINISHED;
            return true;
        }
        if (board.isFull()) {
            status = Status.FINISHED;
            return true;
        }
        switchTurn();
        turnStartMillis = System.currentTimeMillis();
        return true;
    }

    private void switchTurn() {
        if (players.size() < 2) {
            return;
        }
        String next = players.get(0).equals(currentPlayer) ? players.get(1) : players.get(0);
        currentPlayer = next;
        currentSymbol = currentSymbol == 'X' ? 'O' : 'X';
    }

    public synchronized boolean isDraw() {
        return status == Status.FINISHED && !board.isFull();
    }

    public synchronized boolean timeoutExpired(int limitSeconds) {
        return System.currentTimeMillis() - turnStartMillis >= limitSeconds * 1000L;
    }

    public synchronized String getSummary() {
        return String.format("[%d] %s - %s - %s - %d/2", roomId, name, getStatusLabel(), privateRoom ? "Khóa" : "Công cộng", players.size());
    }

    public synchronized boolean isPlayingWithBot() {
        return vsBot;
    }

    public synchronized void setFinished() {
        status = Status.FINISHED;
    }
}
