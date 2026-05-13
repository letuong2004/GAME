package game.model;

import java.util.Arrays;

public class Board {
    private final char[][] grid;
    private final int size;

    public Board(int size) {
        this.size = size;
        grid = new char[size][size];
        for (char[] row : grid) {
            Arrays.fill(row, '.');
        }
    }

    public boolean place(int row, int col, char symbol) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        if (grid[row][col] != '.') {
            return false;
        }
        grid[row][col] = symbol;
        return true;
    }

    public boolean isFull() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkWin(int row, int col, char symbol) {
        return checkDirection(row, col, symbol, 1, 0)
            || checkDirection(row, col, symbol, 0, 1)
            || checkDirection(row, col, symbol, 1, 1)
            || checkDirection(row, col, symbol, 1, -1);
    }

    private boolean checkDirection(int row, int col, char symbol, int dr, int dc) {
        int count = 1;
        count += countLine(row, col, symbol, dr, dc);
        count += countLine(row, col, symbol, -dr, -dc);
        return count >= 5;
    }

    private int countLine(int row, int col, char symbol, int dr, int dc) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < size && c >= 0 && c < size && grid[r][c] == symbol) {
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }

    public String toDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int c = 1; c <= size; c++) {
            sb.append(String.format("%2d", c));
        }
        sb.append(System.lineSeparator());
        for (int r = 0; r < size; r++) {
            sb.append(String.format("%2d ", r + 1));
            for (int c = 0; c < size; c++) {
                sb.append(" ").append(grid[r][c]);
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
