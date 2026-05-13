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
    private JLabel lblOpponent;
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

    public GuiClient() {
        setTitle("Game Caro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);
        setResizable(false);
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
        JPanel loginCard = new JPanel(new BorderLayout(10, 10));
        loginCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        loginCard.setBackground(new Color(240, 240, 240));

        JLabel title = new JLabel("Game Caro", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 102, 204));
        loginCard.add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Đăng nhập", createLoginPanel());
        
        // Wrap register panel in scroll pane
        JPanel registerPanel = createRegisterPanel();
        JScrollPane registerScroll = new JScrollPane(registerPanel);
        registerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addTab("Đăng ký", registerScroll);

        loginCard.add(tabbedPane, BorderLayout.CENTER);

        return loginCard;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        loginUsernameField = new JTextField();
        loginPasswordField = new JPasswordField();
        loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginButton.setPreferredSize(new Dimension(100, 35));

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
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> login());
        loginPasswordField.addActionListener(e -> login());

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        registerUsernameField = new JTextField();
        registerPasswordField = new JPasswordField();
        registerNicknameField = new JTextField();
        registerAvatarField = new JTextField("default");
        registerButton = new JButton("Đăng ký");
        registerButton.setBackground(new Color(0, 153, 76));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerButton.setPreferredSize(new Dimension(100, 35));
        
        JButton btnBrowseAvatar = new JButton("Chọn");
        btnBrowseAvatar.setPreferredSize(new Dimension(80, 25));

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
        
        JPanel avatarPanel = new JPanel(new BorderLayout(5, 5));
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.add(registerAvatarField, BorderLayout.CENTER);
        avatarPanel.add(btnBrowseAvatar, BorderLayout.EAST);
        gbc.gridy = 7;
        panel.add(avatarPanel, gbc);
        
        gbc.gridy = 8;
        gbc.insets = new Insets(15, 10, 8, 10);
        panel.add(registerButton, gbc);

        registerButton.addActionListener(e -> register());
        btnBrowseAvatar.addActionListener(e -> browseAvatar());

        return panel;
    }

    private JPanel createLobbyCard() {
        JPanel lobby = new JPanel(new BorderLayout(12, 12));
        lobby.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel leftPanel = new JPanel(new BorderLayout(12, 12));
        leftPanel.setPreferredSize(new Dimension(340, 0));

        JPanel profilePanel = new JPanel(new GridLayout(6, 1, 6, 6));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Thông tin cá nhân"));
        lblNickname = createInfoLabel("Nickname: -");
        lblWins = createInfoLabel("Số trận thắng: 0");
        lblLosses = createInfoLabel("Số trận thua: 0");
        lblRate = createInfoLabel("Tỉ lệ thắng: 0%");
        lblPoints = createInfoLabel("Điểm: 0");
        lblRank = createInfoLabel("Thứ hạng: -");
        profilePanel.add(lblNickname);
        profilePanel.add(lblWins);
        profilePanel.add(lblLosses);
        profilePanel.add(lblRate);
        profilePanel.add(lblPoints);
        profilePanel.add(lblRank);

        JPanel opponentPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        opponentPanel.setBorder(BorderFactory.createTitledBorder("Đối thủ"));
        lblOpponent = createInfoLabel("Chưa có đối thủ");
        lblTimer = createInfoLabel("Thời gian: 00:00");
        opponentPanel.add(lblOpponent);
        opponentPanel.add(lblTimer);
        opponentPanel.add(new JLabel());

        JPanel infoGroup = new JPanel(new GridLayout(2, 1, 10, 10));
        infoGroup.add(profilePanel);
        infoGroup.add(opponentPanel);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setBorder(BorderFactory.createTitledBorder("Danh sách phòng"));
        roomList.setVisibleRowCount(8);
        JScrollPane roomScroll = new JScrollPane(roomList);

        leaderboardModel = new DefaultListModel<>();
        leaderboardModel.addElement("Xếp hạng server");
        leaderboardModel.addElement("1. Tester - 1000");
        leaderboardModel.addElement("2. Alice - 980");
        leaderboardModel.addElement("3. Bob - 950");
        leaderboardList = new JList<>(leaderboardModel);
        leaderboardList.setBorder(BorderFactory.createTitledBorder("Xếp hạng"));
        leaderboardList.setVisibleRowCount(5);
        JScrollPane leaderboardScroll = new JScrollPane(leaderboardList);

        JPanel listGroup = new JPanel(new GridLayout(2, 1, 10, 10));
        listGroup.add(roomScroll);
        listGroup.add(leaderboardScroll);

        JPanel roomControlPanel = new JPanel(new GridBagLayout());
        roomControlPanel.setBorder(BorderFactory.createTitledBorder("Phòng"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        roomIdField = new JTextField();
        roomPasswordField = new JPasswordField();
        btnCreateRoom = new JButton("Tạo phòng");
        btnJoinRoom = new JButton("Vào phòng");
        btnListRooms = new JButton("Danh sách phòng");
        btnQuickPlay = new JButton("Chơi nhanh");
        btnBot = new JButton("Chơi BOT");

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

        leftPanel.add(infoGroup, BorderLayout.NORTH);
        leftPanel.add(listGroup, BorderLayout.CENTER);
        leftPanel.add(roomControlPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Bàn cờ"));
        boardPanel = new BoardPanel();
        boardPanel.setBackground(new Color(247, 220, 111));
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        lblRoomInfo = new JLabel("Phòng: Chưa vào phòng", SwingConstants.CENTER);
        lblRoomInfo.setFont(lblRoomInfo.getFont().deriveFont(Font.BOLD, 14f));
        centerPanel.add(lblRoomInfo, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(340, 0));
        JPanel chatPanel = new JPanel(new BorderLayout(6, 6));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat lobby"));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel chatInputPanel = new JPanel(new BorderLayout(6, 6));
        chatField = new JTextField();
        btnSendChat = new JButton("Gửi");
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(btnSendChat, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        JPanel playControlPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        playControlPanel.setBorder(BorderFactory.createTitledBorder("Chức năng trận đấu"));
        btnForfeit = new JButton("Xin thua");
        btnDraw = new JButton("Xin hòa");
        btnLeave = new JButton("Rời phòng");
        btnStatus = new JButton("Trạng thái");
        playControlPanel.add(btnForfeit);
        playControlPanel.add(btnDraw);
        playControlPanel.add(btnLeave);
        playControlPanel.add(btnStatus);

        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(playControlPanel, BorderLayout.SOUTH);

        lobby.add(leftPanel, BorderLayout.WEST);
        lobby.add(centerPanel, BorderLayout.CENTER);
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
            new Thread(this::readLoop).start();
        } catch (Exception ex) {
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
                SwingUtilities.invokeLater(() -> {
                    appendChat(serverLine);
                    if (!loggedIn && serverLine.contains("Đăng nhập thành công")) {
                        loggedIn = true;
                        showLobbyCard();
                    }
                    if (serverLine.startsWith("=== THÔNG TIN CÁ NHÂN ===")) {
                        // ignore header
                    }
                });
            }
        } catch (IOException e) {
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
        if (username.isEmpty() || password.isEmpty()) {
            appendChat("Nhập username và password để đăng nhập.");
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
