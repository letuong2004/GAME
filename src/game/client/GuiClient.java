package game.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GuiClient extends JFrame {
    private CardLayout cardLayout;
    private JPanel cards;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nicknameField;
    private JTextField avatarField;
    private JButton loginButton;
    private JButton registerButton;

    // Separate fields for login tab
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Separate fields for register tab
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JTextField registerNicknameField;
    private JTextField registerAvatarField;

    private JLabel lblNickname;
    private JLabel lblWins;
    private JLabel lblLosses;
    private JLabel lblRate;
    private JLabel lblPoints;
    private JLabel lblRank;
    private JLabel lblAvatar;
    private JLabel lblOpponentAvatar;
    private JLabel lblOpponent;
    private JLabel lblOpponentRank;
    private JLabel lblTimer;
    private JLabel lblRoomInfo;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton btnSendChat;
    private JButton btnCreateRoom;
    private JButton btnJoinRoom;
    private JButton btnListRooms;
    private JButton btnQuickPlay;
    private JButton btnBot;
    private JButton btnForfeit;
    private JButton btnDraw;
    private JButton btnLeave;
    private JButton btnStatus;
    private JTextField roomIdField;
    private JPasswordField roomPasswordField;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private DefaultListModel<String> leaderboardModel;
    private JList<String> leaderboardList;
    private BoardPanel boardPanel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected;
    private boolean loggedIn;
    private boolean parsingProfileInfo;

    public GuiClient() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        setTitle("Game Caro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
        connectToServerAuto();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(createLoginCard(), "login");
        cards.add(createLobbyCard(), "lobby");
        add(cards);
        showLoginCard();
    }

    private JPanel createLoginCard() {
        JPanel loginCard = new JPanel(new BorderLayout(18, 18));
        loginCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        loginCard.setBackground(new Color(238, 246, 255));

        JLabel title = new JLabel("Game Caro", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(new Color(14, 76, 129));
        loginCard.add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Đăng nhập", createLoginPanel());
        
        JPanel registerPanel = createRegisterPanel();
        JScrollPane registerScroll = new JScrollPane(registerPanel);
        registerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addTab("Đăng ký", registerScroll);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(245, 250, 255));

        loginCard.add(tabbedPane, BorderLayout.CENTER);

        return loginCard;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(250, 252, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 215, 230), 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        loginUsernameField = new JTextField();
        loginPasswordField = new JPasswordField();
        loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(new Color(14, 76, 129));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(120, 40));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Tài khoản:"), gbc);
        gbc.gridy = 1;
        panel.add(loginUsernameField, gbc);
        gbc.gridy = 2;
        panel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridy = 3;
        panel.add(loginPasswordField, gbc);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(24, 12, 12, 12);
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> login());
        loginPasswordField.addActionListener(e -> login());

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(250, 252, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 215, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        registerUsernameField = new JTextField();
        registerPasswordField = new JPasswordField();
        registerNicknameField = new JTextField();
        registerAvatarField = new JTextField("default");
        registerButton = new JButton("Đăng ký");
        registerButton.setBackground(new Color(14, 76, 129));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(120, 40));
        
        JButton btnBrowseAvatar = new JButton("Chọn");
        btnBrowseAvatar.setPreferredSize(new Dimension(90, 30));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Tài khoản:"), gbc);
        gbc.gridy = 1;
        panel.add(registerUsernameField, gbc);
        gbc.gridy = 2;
        panel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridy = 3;
        panel.add(registerPasswordField, gbc);
        gbc.gridy = 4;
        panel.add(new JLabel("Nickname:"), gbc);
        gbc.gridy = 5;
        panel.add(registerNicknameField, gbc);
        gbc.gridy = 6;
        panel.add(new JLabel("Avatar:"), gbc);
        
        JPanel avatarPanel = new JPanel(new BorderLayout(6, 6));
        avatarPanel.setBackground(new Color(250, 252, 255));
        avatarPanel.add(registerAvatarField, BorderLayout.CENTER);
        avatarPanel.add(btnBrowseAvatar, BorderLayout.EAST);
        gbc.gridy = 7;
        panel.add(avatarPanel, gbc);
        
        gbc.gridy = 8;
        gbc.insets = new Insets(18, 10, 10, 10);
        panel.add(registerButton, gbc);

        registerButton.addActionListener(e -> register());
        btnBrowseAvatar.addActionListener(e -> browseAvatar());

        return panel;
    }

    private JPanel createLobbyCard() {
        JPanel lobby = new JPanel(new BorderLayout(16, 16));
        lobby.setBackground(new Color(240, 244, 252));
        lobby.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel pageTitle = new JLabel("Game Caro", SwingConstants.CENTER);
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        pageTitle.setForeground(new Color(14, 76, 129));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(240, 244, 252));
        titlePanel.add(pageTitle, BorderLayout.CENTER);
        lobby.add(titlePanel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout(14, 14));
        leftPanel.setBackground(new Color(240, 244, 252));
        leftPanel.setPreferredSize(new Dimension(340, 0));

        JPanel infoCards = new JPanel(new GridLayout(2, 1, 12, 12));
        infoCards.setBackground(new Color(240, 244, 252));
        infoCards.add(createPlayerCard());
        infoCards.add(createOpponentCard());

        leftPanel.add(infoCards, BorderLayout.NORTH);

        JPanel roomGroup = new JPanel(new BorderLayout(10, 10));
        roomGroup.setBackground(new Color(240, 244, 252));
        roomGroup.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 210, 225)), "Phòng"));

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roomList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane roomScroll = new JScrollPane(roomList);
        roomGroup.add(roomScroll, BorderLayout.CENTER);

        JPanel roomControlPanel = new JPanel(new GridBagLayout());
        roomControlPanel.setBackground(new Color(255, 255, 255));
        roomControlPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        roomIdField = new JTextField();
        roomPasswordField = new JPasswordField();
        btnCreateRoom = createStyledButton("Tạo phòng", new Color(14, 76, 129));
        btnJoinRoom = createStyledButton("Vào phòng", new Color(0, 123, 104));
        btnListRooms = createStyledButton("Danh sách", new Color(64, 89, 137));
        btnQuickPlay = createStyledButton("Chơi nhanh", new Color(40, 167, 69));
        btnBot = createStyledButton("Chơi BOT", new Color(255, 193, 7));

        gbc.gridx = 0;
        gbc.gridy = 0;
        roomControlPanel.add(new JLabel("ID phòng:"), gbc);
        gbc.gridx = 1;
        roomControlPanel.add(roomIdField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        roomControlPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1;
        roomControlPanel.add(roomPasswordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        roomControlPanel.add(btnCreateRoom, gbc);
        gbc.gridy = 3;
        roomControlPanel.add(btnJoinRoom, gbc);
        gbc.gridy = 4;
        roomControlPanel.add(btnListRooms, gbc);
        gbc.gridy = 5;
        roomControlPanel.add(btnQuickPlay, gbc);
        gbc.gridy = 6;
        roomControlPanel.add(btnBot, gbc);

        roomGroup.add(roomControlPanel, BorderLayout.SOUTH);
        leftPanel.add(roomGroup, BorderLayout.CENTER);

        lobby.add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(255, 255, 255));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        boardPanel = new BoardPanel();
        boardPanel.setBackground(new Color(247, 220, 111));
        boardPanel.setPreferredSize(new Dimension(520, 520));
        centerPanel.add(boardPanel, BorderLayout.CENTER);

        lblRoomInfo = new JLabel("Phòng: Chưa vào phòng", SwingConstants.CENTER);
        lblRoomInfo.setFont(lblRoomInfo.getFont().deriveFont(Font.BOLD, 14f));
        centerPanel.add(lblRoomInfo, BorderLayout.SOUTH);

        lobby.add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(12, 12));
        rightPanel.setBackground(new Color(240, 244, 252));
        rightPanel.setPreferredSize(new Dimension(320, 0));

        JPanel chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBackground(new Color(255, 255, 255));
        chatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 210, 225)), "Chat lobby"));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        chatArea.setBackground(new Color(250, 250, 250));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel chatInputPanel = new JPanel(new BorderLayout(10, 10));
        chatInputPanel.setBackground(new Color(255, 255, 255));
        chatField = new JTextField();
        btnSendChat = createStyledButton("Gửi", new Color(14, 76, 129));
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(btnSendChat, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        JPanel playControlPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        playControlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 210, 225)), "Chức năng trận đấu"));
        playControlPanel.setBackground(new Color(255, 255, 255));
        btnForfeit = createStyledButton("Xin thua", new Color(220, 53, 69));
        btnDraw = createStyledButton("Xin hòa", new Color(23, 162, 184));
        btnLeave = createStyledButton("Rời phòng", new Color(108, 117, 125));
        btnStatus = createStyledButton("Trạng thái", new Color(23, 162, 184));
        playControlPanel.add(btnForfeit);
        playControlPanel.add(btnDraw);
        playControlPanel.add(btnLeave);
        playControlPanel.add(btnStatus);

        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(playControlPanel, BorderLayout.SOUTH);

        lobby.add(rightPanel, BorderLayout.EAST);

        btnSendChat.addActionListener(e -> sendChat());
        chatField.addActionListener(e -> sendChat());
        btnCreateRoom.addActionListener(e -> sendCreateRoom());
        btnJoinRoom.addActionListener(e -> sendJoinRoom());
        btnListRooms.addActionListener(e -> sendCommand("/list-rooms"));
        btnQuickPlay.addActionListener(e -> sendCommand("/quick-play"));
        btnBot.addActionListener(e -> sendCommand("/bot"));
        btnForfeit.addActionListener(e -> sendCommand("/forfeit"));
        btnDraw.addActionListener(e -> sendCommand("/draw"));
        btnLeave.addActionListener(e -> sendCommand("/leave"));
        btnStatus.addActionListener(e -> sendCommand("/status"));

        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = roomList.getSelectedValue();
                    if (selected != null && selected.contains("[")) {
                        String roomId = selected.substring(selected.indexOf("[") + 1, selected.indexOf("]"));
                        roomIdField.setText(roomId);
                    }
                }
            }
        });

        boardPanel.setMoveListener((row, col) -> sendCommand("/move " + (row + 1) + " " + (col + 1)));

        return lobby;
    }

    private JButton createStyledButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createPlayerCard() {
        JPanel playerCard = new JPanel(new BorderLayout(12, 12));
        playerCard.setBackground(new Color(255, 255, 255));
        playerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("Bạn", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(14, 76, 129));

        lblAvatar = createAvatarLabel("Avatar");
        JPanel infoPanel = new JPanel(new GridLayout(6, 1, 4, 4));
        infoPanel.setBackground(new Color(255, 255, 255));
        lblNickname = createInfoLabel("Nickname: -");
        lblWins = createInfoLabel("Số ván thắng: 0");
        lblLosses = createInfoLabel("Số ván thua: 0");
        lblRate = createInfoLabel("Tỉ lệ thắng: 0%");
        lblPoints = createInfoLabel("Điểm: 0");
        lblRank = createInfoLabel("Thứ hạng: -");
        infoPanel.add(lblNickname);
        infoPanel.add(lblWins);
        infoPanel.add(lblLosses);
        infoPanel.add(lblRate);
        infoPanel.add(lblPoints);
        infoPanel.add(lblRank);

        playerCard.add(title, BorderLayout.NORTH);
        playerCard.add(lblAvatar, BorderLayout.WEST);
        playerCard.add(infoPanel, BorderLayout.CENTER);

        return playerCard;
    }

    private JPanel createOpponentCard() {
        JPanel opponentCard = new JPanel(new BorderLayout(12, 12));
        opponentCard.setBackground(new Color(255, 255, 255));
        opponentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("Đối thủ", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(14, 76, 129));

        lblOpponentAvatar = createAvatarLabel("Opponent");
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        infoPanel.setBackground(new Color(255, 255, 255));
        lblOpponent = createInfoLabel("Chưa có đối thủ");
        lblTimer = createInfoLabel("Thời gian: 00:00");
        lblOpponentRank = createInfoLabel("Thứ hạng: -");
        infoPanel.add(lblOpponent);
        infoPanel.add(lblTimer);
        infoPanel.add(lblOpponentRank);

        opponentCard.add(title, BorderLayout.NORTH);
        opponentCard.add(lblOpponentAvatar, BorderLayout.WEST);
        opponentCard.add(infoPanel, BorderLayout.CENTER);

        return opponentCard;
    }

    private JLabel createAvatarLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(90, 90));
        label.setOpaque(true);
        label.setBackground(new Color(235, 243, 255));
        label.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225)));
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return label;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8888);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("[CLIENT] Kết nối server thành công!");
            new Thread(this::readLoop).start();
        } catch (Exception ex) {
            System.out.println("[CLIENT] ✗ Không thể kết nối: " + ex.getMessage());
            ex.printStackTrace();
            appendChat("✗ Không thể kết nối: " + ex.getMessage());
            connected = false;
        }
    }

    private void connectToServerAuto() {
        connectToServer();
    }

    private void readLoop() {
        try {
            String line;
            while (socket != null && !socket.isClosed() && (line = in.readLine()) != null) {
                final String serverLine = line;
                System.out.println("[SERVER] " + serverLine);
                SwingUtilities.invokeLater(() -> {
                    appendChat(serverLine);
                    if (!loggedIn && serverLine.contains("Đăng nhập thành công")) {
                        System.out.println("[CLIENT] Đăng nhập thành công! Chuyển sang lobby.");
                        loggedIn = true;
                        showLobbyCard();
                    }
                    if (serverLine.startsWith("=== THÔNG TIN CÁ NHÂN ===")) {
                        parsingProfileInfo = true;
                    } else if (serverLine.startsWith("========================")) {
                        parsingProfileInfo = false;
                    } else if (parsingProfileInfo) {
                        updateProfileInfo(serverLine);
                    }
                });
            }
        } catch (IOException e) {
            System.out.println("[CLIENT] Kết nối bị ngắt: " + e.getMessage());
            appendChat("Kết nối bị ngắt: " + e.getMessage());
            connected = false;
            loggedIn = false;
        }
    }

    private void sendChat() {
        String text = chatField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        sendCommand("/chat " + text);
        chatField.setText("");
    }

    private void sendCreateRoom() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên phòng:", "Tạo phòng", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        String password = new String(roomPasswordField.getPassword()).trim();
        sendCommand("/create-room " + name.trim() + " " + password);
    }

    private void sendJoinRoom() {
        String id = roomIdField.getText().trim();
        if (id.isEmpty()) {
            appendChat("Nhập ID phòng trước khi vào phòng.");
            return;
        }
        String password = new String(roomPasswordField.getPassword()).trim();
        sendCommand("/join-room " + id + " " + password);
    }

    private void sendCommand(String command) {
        if (out == null) {
            appendChat("Chưa kết nối server.");
            return;
        }
        appendChat("=> " + command);
        out.println(command);
    }

    private void login() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();
        System.out.println("[CLIENT] Login button clicked. Username: " + username);
        if (username.isEmpty() || password.isEmpty()) {
            appendChat("Nhập username và password để đăng nhập.");
            return;
        }
        if (!connected) {
            System.out.println("[CLIENT] ✗ Not connected to server!");
            appendChat("✗ Chưa kết nối server!");
            return;
        }
        sendCommand("/login " + username + " " + password);
    }

    private void register() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword()).trim();
        String nickname = registerNicknameField.getText().trim();
        String avatar = registerAvatarField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || nickname.isEmpty() || avatar.isEmpty()) {
            appendChat("Nhập đầy đủ thông tin đăng ký.");
            return;
        }
        sendCommand("/register " + username + " " + password + " " + nickname + " " + avatar);
    }

    private void browseAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        
        // Set initial directory to Anh folder
        String projectPath = System.getProperty("user.dir");
        java.io.File anhDir = new java.io.File(projectPath, "src/game/Anh");
        if (anhDir.exists() && anhDir.isDirectory()) {
            fileChooser.setCurrentDirectory(anhDir);
        }
        
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getName();
            registerAvatarField.setText(filePath);
            appendChat("✓ Đã chọn avatar: " + filePath);
        }
    }

    private void showLoginCard() {
        cardLayout.show(cards, "login");
    }

    private void showLobbyCard() {
        cardLayout.show(cards, "lobby");
    }

    private void updateProfileInfo(String serverLine) {
        String regex = "^(.*) \\((.*)\\) - W:(\\d+) L:(\\d+) Rate:(\\d+)% Points:(\\d+)$";
        if (serverLine.matches(regex)) {
            String nickname = serverLine.replaceAll(regex, "$1");
            String username = serverLine.replaceAll(regex, "$2");
            String wins = serverLine.replaceAll(regex, "$3");
            String losses = serverLine.replaceAll(regex, "$4");
            String rate = serverLine.replaceAll(regex, "$5");
            String points = serverLine.replaceAll(regex, "$6");
            lblNickname.setText("Nickname: " + nickname + " (" + username + ")");
            lblWins.setText("Số trận thắng: " + wins);
            lblLosses.setText("Số trận thua: " + losses);
            lblRate.setText("Tỉ lệ thắng: " + rate + "%");
            lblPoints.setText("Điểm: " + points);
            lblRank.setText("Thứ hạng: -");
        }
    }

    private void appendChat(String text) {
        if (chatArea != null) {
            SwingUtilities.invokeLater(() -> {
                chatArea.append(text + "\n");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiClient client = new GuiClient();
            client.setVisible(true);
        });
    }
}
