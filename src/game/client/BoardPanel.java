package game.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel {
    private static final int BOARD_SIZE = 20;
    private static final int CELL_SIZE = 28;
    private MoveListener moveListener;

    public BoardPanel() {
        setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE + 1, BOARD_SIZE * CELL_SIZE + 1));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = e.getY() / CELL_SIZE;
                int col = e.getX() / CELL_SIZE;
                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && moveListener != null) {
                    moveListener.onMove(row, col);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(245, 245, 220));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.GRAY);
        for (int i = 0; i <= BOARD_SIZE; i++) {
            g.drawLine(0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
        }
    }

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public interface MoveListener {
        void onMove(int row, int col);
    }
}
