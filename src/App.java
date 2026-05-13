import game.client.GuiClient;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiClient client = new GuiClient();
            client.setVisible(true);
        });
    }
}
