package com.heal.doan_ltm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.net.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class MainClient extends JFrame {

    static {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) {}
    }

    class Friend {
        String username, name, ip; int port;
        public Friend(String u, String n, String i, int p) { username=u; name=n; ip=i; port=p; }
    }

    class BubblePanel extends JPanel {
        private Color bg;
        public BubblePanel(Color bg) {
            this.bg = bg; setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundedPanel extends JPanel {
        private int radius; private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius; this.bgColor = bgColor; setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class FlatButton extends JButton {
        private Color normalBg, hoverBg, pressedBg;
        private int radius;
        public FlatButton(String text, Color bg, Color fg, int radius) {
            super(text); this.normalBg = bg; this.radius = radius;
            if (bg.getAlpha() == 0) {
                this.hoverBg = new Color(0, 0, 0, 15);
                this.pressedBg = new Color(0, 0, 0, 30);
            } else {
                this.hoverBg = brighten(bg, 0.1f);
                this.pressedBg = darken(bg, 0.1f);
            }
            setForeground(fg); setFont(new Font("SansSerif", Font.BOLD, 14));
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { if(isEnabled()) repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { if(isEnabled()) repaint(); }
                public void mousePressed(java.awt.event.MouseEvent e) { if(isEnabled()) repaint(); }
                public void mouseReleased(java.awt.event.MouseEvent e) { if(isEnabled()) repaint(); }
            });
        }
        private Color brighten(Color c, float f) {
            if (c.equals(Color.WHITE)) return new Color(240, 240, 240);
            int r = Math.min(255, (int)(c.getRed() + (255 - c.getRed()) * f));
            int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * f));
            int b = Math.min(255, (int)(c.getBlue() + (255 - c.getBlue()) * f));
            return new Color(r, g, b);
        }
        private Color darken(Color c, float f) {
            int r = Math.max(0, (int)(c.getRed() * (1 - f)));
            int g = Math.max(0, (int)(c.getGreen() * (1 - f)));
            int b = Math.max(0, (int)(c.getBlue() * (1 - f)));
            return new Color(r, g, b);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) g2.setColor(new Color(230, 230, 230));
            else if (getModel().isPressed()) g2.setColor(pressedBg);
            else if (getModel().isRollover()) g2.setColor(hoverBg);
            else g2.setColor(normalBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundAvatar extends JLabel {
        private Image img;
        public RoundAvatar(String text) { super(text, SwingConstants.CENTER); setOpaque(false); }
        public void setImage(Image img) { this.img = img; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight());
            if (img != null) {
                g2.setClip(new Ellipse2D.Float(0, 0, d, d));
                int iw = img.getWidth(null), ih = img.getHeight(null);
                double scale = Math.max((double)d/iw, (double)d/ih);
                int sw = (int)(iw*scale), sh = (int)(ih*scale);
                int sx = (d-sw)/2, sy = (d-sh)/2;
                g2.drawImage(img, sx, sy, sw, sh, null);
            } else {
                g2.setColor(getBackground()); g2.fillOval(0, 0, d, d);
                super.paintComponent(g2);
            }
            g2.dispose();
        }
    }

    class FriendListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(12, 15, 12, 15));
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            if (isSelected) {
                label.setBackground(new Color(230, 240, 255));
                label.setForeground(new Color(0, 132, 255));
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(50, 50, 50));
            }
            return label;
        }
    }

    private String currentUser, currentFullname, currentAvatarBase64, currentTargetUser, currentCallUser;
    private RoundAvatar avatarLabel, lblCallAvatar;
    private JLabel nameLabel, lblTarget;
    private JTextField txtChat;
    private JPanel chatPanel;
    private FlatButton btnCall, btnSend, btnRecord, btnImg, btnFile, btnIcon;
    private DefaultListModel<String> friendModel, reqModel;
    private JList<String> listFriend, listReq;

    private CardLayout cardLayout;
    private JPanel rightPanel, chatContainer, callContainer;
    private JLabel lblCallName, lblCallStatus;
    private FlatButton btnAcceptCall, btnDeclineCall, btnEndCall;

    private DatagramSocket udpSocket;
    private TargetDataLine mic, recordMic;
    private SourceDataLine speaker;
    private volatile boolean isRunning = false;
    private volatile boolean isCalling = false;
    private volatile boolean isRecording = false;
    private ByteArrayOutputStream currentRecordStream;
    private Timer timer;
    private Map<String, Friend> friends = new HashMap<>();

    private Map<Long, byte[]> incomingData = new HashMap<>();
    private Map<Long, Integer> incomingDataReceived = new HashMap<>();

    public MainClient(String user, String fullname, String avatar) {
        this.currentUser = user; this.currentFullname = fullname.isEmpty() ? user : fullname; this.currentAvatarBase64 = avatar;
        setTitle("skype p2p - " + this.currentFullname);
        setSize(1100, 700); setDefaultCloseOperation(EXIT_ON_CLOSE); setLocationRelativeTo(null); setLayout(new BorderLayout());

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(320, 0)); sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        JPanel profilePanel = new JPanel(); profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(Color.WHITE); profilePanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        avatarLabel = new RoundAvatar(this.currentUser.substring(0, 1).toUpperCase());
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setPreferredSize(new Dimension(90, 90));
        avatarLabel.setMaximumSize(new Dimension(90, 90));
        avatarLabel.setFont(new Font("SansSerif", Font.BOLD, 35));
        avatarLabel.setBackground(new Color(0, 132, 255));
        avatarLabel.setForeground(Color.WHITE);
        updateAvatarDisplay();

        nameLabel = new JLabel(this.currentFullname); nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(new Color(30, 30, 30)); nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        FlatButton btnEditProfile = new FlatButton("đổi ảnh & tên", new Color(240, 242, 245), new Color(50, 50, 50), 15);
        btnEditProfile.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEditProfile.setPreferredSize(new Dimension(140, 35));
        btnEditProfile.setMaximumSize(new Dimension(140, 35));
        btnEditProfile.addActionListener(e -> showProfileDialog());

        profilePanel.add(avatarLabel); profilePanel.add(Box.createVerticalStrut(15));
        profilePanel.add(nameLabel); profilePanel.add(Box.createVerticalStrut(10)); profilePanel.add(btnEditProfile);

        JPanel midPanel = new JPanel(new BorderLayout()); midPanel.setBackground(Color.WHITE);
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 5)); pnlSearch.setBorder(new EmptyBorder(10, 20, 10, 20));
        pnlSearch.setBackground(Color.WHITE);

        JTextField txtPhone = new JTextField();
        txtPhone.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        txtPhone.setFont(new Font("SansSerif", Font.PLAIN, 14));

        FlatButton btnSearch = new FlatButton("tìm", new Color(0, 132, 255), Color.WHITE, 15);
        btnSearch.setPreferredSize(new Dimension(60, 35));
        pnlSearch.add(txtPhone, BorderLayout.CENTER); pnlSearch.add(btnSearch, BorderLayout.EAST);
        midPanel.add(pnlSearch, BorderLayout.NORTH);

        reqModel = new DefaultListModel<>(); listReq = new JList<>(reqModel);
        listReq.setCellRenderer(new FriendListRenderer());

        JPanel pnlReq = new JPanel(new BorderLayout()); pnlReq.setBackground(Color.WHITE);
        JLabel lblReq = new JLabel("  lời mời kết bạn:"); lblReq.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblReq.setForeground(new Color(150, 150, 150));
        lblReq.setBorder(new EmptyBorder(10, 0, 5, 0)); pnlReq.add(lblReq, BorderLayout.NORTH);

        JScrollPane scrollReq = new JScrollPane(listReq); scrollReq.setBorder(null);
        pnlReq.add(scrollReq, BorderLayout.CENTER);

        FlatButton btnAccept = new FlatButton("chấp nhận", new Color(76, 209, 55), Color.WHITE, 10);
        btnAccept.setPreferredSize(new Dimension(100, 35));
        JPanel pnlAccept = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlAccept.setBackground(Color.WHITE);
        pnlAccept.add(btnAccept); pnlReq.add(pnlAccept, BorderLayout.SOUTH);

        friendModel = new DefaultListModel<>(); listFriend = new JList<>(friendModel);
        listFriend.setCellRenderer(new FriendListRenderer());

        JPanel pnlFrList = new JPanel(new BorderLayout()); pnlFrList.setBackground(Color.WHITE);
        JLabel lblFr = new JLabel("  danh sách bạn bè:"); lblFr.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblFr.setForeground(new Color(150, 150, 150));
        lblFr.setBorder(new EmptyBorder(10, 0, 5, 0)); pnlFrList.add(lblFr, BorderLayout.NORTH);

        JScrollPane scrollFr = new JScrollPane(listFriend); scrollFr.setBorder(null);
        pnlFrList.add(scrollFr, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlReq, pnlFrList);
        split.setDividerLocation(180); split.setBorder(null); midPanel.add(split, BorderLayout.CENTER);

        sidebar.add(profilePanel, BorderLayout.NORTH); sidebar.add(midPanel, BorderLayout.CENTER);

        FlatButton btnLogout = new FlatButton("đăng xuất", new Color(255, 235, 235), new Color(232, 65, 24), 0);
        btnLogout.setPreferredSize(new Dimension(100, 45));
        btnLogout.addActionListener(e -> logout()); sidebar.add(btnLogout, BorderLayout.SOUTH);

        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout);

        chatContainer = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        headerPanel.setPreferredSize(new Dimension(0, 70));
        lblTarget = new JLabel("  chọn bạn bè để bắt đầu trò chuyện"); lblTarget.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(lblTarget, BorderLayout.WEST);

        JPanel callPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15)); callPanel.setBackground(Color.WHITE);
        btnCall = new FlatButton("📞", new Color(230, 245, 235), new Color(46, 204, 113), 20);
        btnCall.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnCall.setEnabled(false); btnCall.setPreferredSize(new Dimension(50, 40));
        callPanel.add(btnCall); headerPanel.add(callPanel, BorderLayout.EAST);

        chatPanel = new JPanel(); chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(244, 245, 247));
        JScrollPane chatScroll = new JScrollPane(chatPanel); chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(244, 245, 247));
        bottomPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        RoundedPanel inputBg = new RoundedPanel(25, Color.WHITE);
        inputBg.setLayout(new BorderLayout(5, 5));
        inputBg.setBorder(new EmptyBorder(5, 15, 5, 5));

        txtChat = new JTextField(); txtChat.setEnabled(false);
        txtChat.setOpaque(false); txtChat.setBorder(null);
        txtChat.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JPanel pnlChatActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlChatActions.setOpaque(false);

        btnIcon = new FlatButton("😀", new Color(0,0,0,0), new Color(120, 120, 120), 15);
        btnImg = new FlatButton("🖼", new Color(0,0,0,0), new Color(120, 120, 120), 15);
        btnFile = new FlatButton("📁", new Color(0,0,0,0), new Color(120, 120, 120), 15);
        btnRecord = new FlatButton("🎤", new Color(0,0,0,0), new Color(120, 120, 120), 15);
        btnSend = new FlatButton("✈", new Color(0, 132, 255), Color.WHITE, 20);

        btnIcon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnImg.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnFile.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnRecord.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnSend.setFont(new Font("SansSerif", Font.PLAIN, 18));

        btnIcon.setEnabled(false); btnImg.setEnabled(false); btnFile.setEnabled(false);
        btnRecord.setEnabled(false); btnSend.setEnabled(false);

        btnIcon.setPreferredSize(new Dimension(42, 42)); btnImg.setPreferredSize(new Dimension(42, 42));
        btnFile.setPreferredSize(new Dimension(42, 42)); btnRecord.setPreferredSize(new Dimension(42, 42));
        btnSend.setPreferredSize(new Dimension(42, 42));

        pnlChatActions.add(btnIcon); pnlChatActions.add(btnImg); pnlChatActions.add(btnFile);
        pnlChatActions.add(btnRecord); pnlChatActions.add(btnSend);

        JPopupMenu emojiMenu = new JPopupMenu();
        String[] emojis = {"😀", "😂", "😍", "😭", "👍", "❤", "🔥", "✨", "🎉", "😢", "👋"};
        for(String e : emojis) {
            JMenuItem item = new JMenuItem(e);
            item.setFont(new Font("SansSerif", Font.PLAIN, 22));
            item.addActionListener(ev -> txtChat.setText(txtChat.getText() + e));
            emojiMenu.add(item);
        }
        btnIcon.addActionListener(e -> emojiMenu.show(btnIcon, 0, -emojiMenu.getPreferredSize().height));

        inputBg.add(txtChat, BorderLayout.CENTER); inputBg.add(pnlChatActions, BorderLayout.EAST);
        bottomPanel.add(inputBg, BorderLayout.CENTER);

        chatContainer.add(headerPanel, BorderLayout.NORTH);
        chatContainer.add(chatScroll, BorderLayout.CENTER);
        chatContainer.add(bottomPanel, BorderLayout.SOUTH);

        callContainer = new JPanel(new GridBagLayout());
        callContainer.setBackground(new Color(33, 33, 33));
        GridBagConstraints g = new GridBagConstraints();
        g.gridwidth = GridBagConstraints.REMAINDER; g.anchor = GridBagConstraints.CENTER; g.insets = new Insets(10, 10, 10, 10);

        lblCallAvatar = new RoundAvatar("?"); lblCallAvatar.setFont(new Font("SansSerif", Font.BOLD, 60));
        lblCallAvatar.setBackground(new Color(0, 132, 255)); lblCallAvatar.setForeground(Color.WHITE);
        lblCallAvatar.setPreferredSize(new Dimension(150, 150)); lblCallAvatar.setMaximumSize(new Dimension(150, 150));

        lblCallName = new JLabel("tên người gọi"); lblCallName.setFont(new Font("SansSerif", Font.BOLD, 30)); lblCallName.setForeground(Color.WHITE);
        lblCallStatus = new JLabel("đang gọi..."); lblCallStatus.setFont(new Font("SansSerif", Font.PLAIN, 18)); lblCallStatus.setForeground(new Color(180, 180, 180));

        JPanel callActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20)); callActionPanel.setOpaque(false);
        btnAcceptCall = new FlatButton("nhấc máy", new Color(46, 204, 113), Color.WHITE, 25); btnAcceptCall.setPreferredSize(new Dimension(130, 50));
        btnDeclineCall = new FlatButton("từ chối", new Color(231, 76, 60), Color.WHITE, 25); btnDeclineCall.setPreferredSize(new Dimension(130, 50));
        btnEndCall = new FlatButton("kết thúc", new Color(231, 76, 60), Color.WHITE, 25); btnEndCall.setPreferredSize(new Dimension(130, 50));

        callActionPanel.add(btnAcceptCall); callActionPanel.add(btnDeclineCall); callActionPanel.add(btnEndCall);
        callContainer.add(lblCallAvatar, g); callContainer.add(lblCallName, g); callContainer.add(lblCallStatus, g); callContainer.add(callActionPanel, g);

        rightPanel.add(chatContainer, "CHAT");
        rightPanel.add(callContainer, "CALL");

        add(sidebar, BorderLayout.WEST); add(rightPanel, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            if(!txtPhone.getText().trim().isEmpty()) sendToServer("SEARCH;;;" + txtPhone.getText().trim());
        });
        btnAccept.addActionListener(e -> {
            String sel = listReq.getSelectedValue();
            if(sel != null) sendToServer("ACCEPT;;;" + sel.split(" - ")[0] + ";;;" + currentUser);
        });
        listFriend.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && listFriend.getSelectedValue() != null) {
                currentTargetUser = listFriend.getSelectedValue().split(" - ")[0];
                lblTarget.setText("  " + listFriend.getSelectedValue().split(" - ")[1]);
                txtChat.setEnabled(true); btnSend.setEnabled(true); btnCall.setEnabled(true);
                btnRecord.setEnabled(true); btnImg.setEnabled(true); btnFile.setEnabled(true); btnIcon.setEnabled(true);
            }
        });

        btnCall.addActionListener(e -> initiateCall());
        btnAcceptCall.addActionListener(e -> acceptCall());
        btnDeclineCall.addActionListener(e -> declineCall());
        btnEndCall.addActionListener(e -> endCall());

        btnImg.addActionListener(e -> sendFileOrImage(8));
        btnFile.addActionListener(e -> sendFileOrImage(9));
        btnRecord.addActionListener(e -> toggleRecord());
        btnSend.addActionListener(e -> sendText());
        txtChat.addActionListener(e -> sendText());

        startNetwork();
    }

    private void updateAvatarDisplay() {
        try {
            if (currentAvatarBase64 != null && !currentAvatarBase64.isEmpty()) {
                byte[] imgBytes = Base64.getDecoder().decode(currentAvatarBase64);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
                avatarLabel.setImage(img); avatarLabel.setText("");
            } else {
                avatarLabel.setImage(null); avatarLabel.setText(currentUser.substring(0, 1).toUpperCase());
            }
        } catch (Exception e) {}
    }

    private void showProfileDialog() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); JTextField nameField = new JTextField(currentFullname);
        JButton btnPickImg = new JButton("chọn ảnh"); final String[] newAvatar = {currentAvatarBase64};
        btnPickImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage img = ImageIO.read(fc.getSelectedFile()); Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    BufferedImage bimg = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = bimg.createGraphics(); g2d.drawImage(scaled, 0, 0, null); g2d.dispose();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(); ImageIO.write(bimg, "jpg", baos);
                    newAvatar[0] = Base64.getEncoder().encodeToString(baos.toByteArray()); JOptionPane.showMessageDialog(this, "đã nạp ảnh");
                } catch (Exception ex) {}
            }
        });
        p.add(new JLabel("tên hiển thị:"), BorderLayout.WEST); p.add(nameField, BorderLayout.CENTER); p.add(btnPickImg, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(this, p, "sửa hồ sơ", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            sendToServer("UPDATE;;;" + currentUser + ";;;" + nameField.getText() + ";;;" + newAvatar[0]);
            currentFullname = nameField.getText(); currentAvatarBase64 = newAvatar[0];
            nameLabel.setText(currentFullname); updateAvatarDisplay(); setTitle("skype p2p - " + currentFullname);
        }
    }

    private void startNetwork() {
        try {
            udpSocket = new DatagramSocket(); isRunning = true;
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
            speaker = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
            speaker.open(format); speaker.start();

            new Thread(this::listenData).start();
            timer = new Timer(3000, e -> {
                sendToServer("ONLINE;;;" + currentUser); sendToServer("GETDATA;;;" + currentUser);
            });
            timer.start();
        } catch (Exception e) {}
    }

    private void sendToServer(String msg) {
        try {
            byte[] data = msg.getBytes("UTF-8");
            udpSocket.send(new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), 8080));
        } catch (Exception e) {}
    }

    private void sendP2P(Friend f, byte[] data) {
        if(f == null || f.ip.isEmpty() || f.port == 0) return;
        try {
            udpSocket.send(new DatagramPacket(data, data.length, InetAddress.getByName(f.ip), f.port));
        } catch (Exception e) {}
    }

    private void listenData() {
        byte[] buffer = new byte[65507];
        while (isRunning) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                if (packet.getPort() == 8080) {
                    String s = new String(buffer, 0, packet.getLength(), "UTF-8");
                    SwingUtilities.invokeLater(() -> processServerMsg(s));
                } else {
                    int type = buffer[0];
                    Friend senderF = getFriendByIp(packet.getAddress().getHostAddress(), packet.getPort());

                    if (type == 0 && isCalling && speaker != null) {
                        speaker.write(buffer, 1, packet.getLength() - 1);
                    } else if (type == 1 && senderF != null) {
                        String text = new String(buffer, 1, packet.getLength() - 1, "UTF-8");
                        SwingUtilities.invokeLater(() -> appendMsg(senderF.name, text, false));
                    } else if ((type == 3 || type == 8 || type == 9) && senderF != null) {
                        ByteBuffer bb = ByteBuffer.wrap(buffer, 1, packet.getLength() - 1);
                        long id = bb.getLong(); int total = bb.getInt(); int offset = bb.getInt(); int dataLen = packet.getLength() - 17;
                        if (!incomingData.containsKey(id)) { incomingData.put(id, new byte[total]); incomingDataReceived.put(id, 0); }
                        byte[] fullData = incomingData.get(id); System.arraycopy(buffer, 17, fullData, offset, dataLen);
                        int received = incomingDataReceived.get(id) + dataLen; incomingDataReceived.put(id, received);
                        if (received >= total) {
                            byte[] finalData = fullData.clone();
                            if(type == 3) SwingUtilities.invokeLater(() -> appendAudioMsg(senderF.name, finalData, false));
                            else if(type == 8) SwingUtilities.invokeLater(() -> appendImageMsg(senderF.name, finalData, false));
                            else if(type == 9) SwingUtilities.invokeLater(() -> appendFileMsg(senderF.name, finalData, false));
                            incomingData.remove(id); incomingDataReceived.remove(id);
                        }
                    } else if (type == 4 && senderF != null) {
                        SwingUtilities.invokeLater(() -> handleIncomingCall(senderF.username));
                    } else if (type == 5) {
                        SwingUtilities.invokeLater(() -> handleCallAccepted());
                    } else if (type == 6) {
                        SwingUtilities.invokeLater(() -> handleCallDeclined());
                    } else if (type == 7) {
                        SwingUtilities.invokeLater(() -> handleCallEnded());
                    }
                }
            } catch (Exception e) {}
        }
    }

    private Friend getFriendByIp(String ip, int port) {
        for(Friend f : friends.values()) if(f.ip.equals(ip) && f.port == port) return f;
        return null;
    }

    private void processServerMsg(String s) {
        if (s.startsWith("DATA;;;")) {
            String[] parts = s.substring(7).split("###");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String sel = listFriend.getSelectedValue();
                friendModel.clear(); friends.clear();
                for(String f : parts[0].split("\\|")) {
                    if(f.isEmpty()) continue;
                    String[] d = f.split(":"); friends.put(d[0], new Friend(d[0], d[1], d[2], Integer.parseInt(d[3])));
                    friendModel.addElement(d[0] + " - " + d[1]);
                }
                if(sel != null && friendModel.contains(sel)) listFriend.setSelectedValue(sel, true);
            } else { friendModel.clear(); friends.clear(); }

            if (parts.length > 1 && !parts[1].isEmpty()) {
                reqModel.clear();
                for(String r : parts[1].split("\\|")) {
                    if(!r.isEmpty()) reqModel.addElement(r.split(":")[0] + " - " + r.split(":")[1]);
                }
            } else reqModel.clear();
        } else if (s.startsWith("SEARCHRES;;;")) {
            String[] p = s.split(";;;");
            if (p.length == 3 && JOptionPane.showConfirmDialog(this, "tìm thấy: " + p[2] + "\nbạn có muốn kết bạn?", "kết bạn", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                sendToServer("ADD;;;" + currentUser + ";;;" + p[1]);
            }
        } else if (s.equals("NOTFOUND")) JOptionPane.showMessageDialog(this, "không tìm thấy số điện thoại này");
    }

    private void initiateCall() {
        if (currentTargetUser == null) return;
        Friend f = friends.get(currentTargetUser);
        if (f == null || f.ip.isEmpty() || f.port == 0) { JOptionPane.showMessageDialog(this, "bạn bè đang ngoại tuyến"); return; }

        currentCallUser = currentTargetUser;
        sendP2P(f, new byte[]{4});

        lblCallAvatar.setText(f.name.substring(0, 1).toUpperCase());
        lblCallName.setText(f.name);
        lblCallStatus.setText("đang gọi...");

        btnAcceptCall.setVisible(false);
        btnDeclineCall.setVisible(false);
        btnEndCall.setVisible(true);
        cardLayout.show(rightPanel, "CALL");
    }

    private void handleIncomingCall(String callerUsername) {
        if (currentCallUser != null) {
            sendP2P(friends.get(callerUsername), new byte[]{6});
            return;
        }
        currentCallUser = callerUsername;
        Friend f = friends.get(currentCallUser);
        if (f == null) return;

        lblCallAvatar.setText(f.name.substring(0, 1).toUpperCase());
        lblCallName.setText(f.name);
        lblCallStatus.setText("cuộc gọi đến...");

        btnAcceptCall.setVisible(true);
        btnDeclineCall.setVisible(true);
        btnEndCall.setVisible(false);
        cardLayout.show(rightPanel, "CALL");
    }

    private void acceptCall() {
        if (currentCallUser == null) return;
        sendP2P(friends.get(currentCallUser), new byte[]{5});
        lblCallStatus.setText("đang trò chuyện");
        btnAcceptCall.setVisible(false);
        btnDeclineCall.setVisible(false);
        btnEndCall.setVisible(true);
        startAudioStreams();
    }

    private void handleCallAccepted() {
        lblCallStatus.setText("đang trò chuyện");
        startAudioStreams();
    }

    private void declineCall() {
        if (currentCallUser == null) return;
        sendP2P(friends.get(currentCallUser), new byte[]{6});
        closeCallUI();
    }

    private void handleCallDeclined() {
        JOptionPane.showMessageDialog(this, "người dùng đã từ chối cuộc gọi");
        closeCallUI();
    }

    private void endCall() {
        if (currentCallUser == null) return;
        sendP2P(friends.get(currentCallUser), new byte[]{7});
        closeCallUI();
    }

    private void handleCallEnded() {
        closeCallUI();
    }

    private void closeCallUI() {
        currentCallUser = null;
        stopAudioStreams();
        cardLayout.show(rightPanel, "CHAT");
    }

    private void startAudioStreams() {
        if (isCalling) return;
        try {
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
            mic = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
            mic.open(format); mic.start();
            isCalling = true;

            new Thread(() -> {
                try {
                    byte[] audioBuffer = new byte[1024];
                    byte[] packetData = new byte[1025]; packetData[0] = 0;
                    Friend f = friends.get(currentCallUser);
                    if (f == null) return;
                    InetAddress addr = InetAddress.getByName(f.ip);
                    int targetPort = f.port;

                    while (isCalling && currentCallUser != null) {
                        int read = mic.read(audioBuffer, 0, audioBuffer.length);
                        if (read > 0) {
                            System.arraycopy(audioBuffer, 0, packetData, 1, read);
                            udpSocket.send(new DatagramPacket(packetData, packetData.length, addr, targetPort));
                        }
                    }
                } catch (Exception e) {}
            }).start();
        } catch (Exception e) {}
    }

    private void stopAudioStreams() {
        isCalling = false;
        if (mic != null) { mic.close(); mic = null; }
    }

    private void sendFragmentedData(Friend f, byte type, byte[] fullData) {
        new Thread(() -> {
            try {
                InetAddress addr = InetAddress.getByName(f.ip);
                long msgId = System.currentTimeMillis(); int offset = 0;
                while (offset < fullData.length) {
                    int length = Math.min(50000, fullData.length - offset);
                    ByteBuffer bb = ByteBuffer.allocate(17 + length);
                    bb.put(type); bb.putLong(msgId); bb.putInt(fullData.length); bb.putInt(offset); bb.put(fullData, offset, length);
                    udpSocket.send(new DatagramPacket(bb.array(), bb.array().length, addr, f.port));
                    offset += length; Thread.sleep(15);
                }
            } catch (Exception e) {}
        }).start();
    }

    private void sendFileOrImage(int type) {
        if (currentTargetUser == null) return;
        Friend f = friends.get(currentTargetUser);
        if (f == null || f.ip.isEmpty() || f.port == 0) { JOptionPane.showMessageDialog(this, "bạn bè đang ngoại tuyến"); return; }

        JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                if(type == 8) {
                    sendFragmentedData(f, (byte)8, fileBytes);
                    appendImageMsg("", fileBytes, true);
                } else {
                    byte[] nameBytes = file.getName().getBytes("UTF-8");
                    ByteBuffer bb = ByteBuffer.allocate(4 + nameBytes.length + fileBytes.length);
                    bb.putInt(nameBytes.length); bb.put(nameBytes); bb.put(fileBytes);
                    sendFragmentedData(f, (byte)9, bb.array());
                    appendFileMsg("", bb.array(), true);
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "lỗi đọc file");
            }
        }
    }

    private void toggleRecord() {
        if (currentTargetUser == null) return;
        Friend f = friends.get(currentTargetUser);
        if (f == null || f.ip.isEmpty() || f.port == 0) { JOptionPane.showMessageDialog(this, "bạn bè đang ngoại tuyến"); return; }

        if (!isRecording) {
            try {
                AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
                recordMic = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
                recordMic.open(format); recordMic.start();
                isRecording = true; currentRecordStream = new ByteArrayOutputStream();
                btnRecord.setForeground(new Color(231, 76, 60));

                new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    while (isRecording) {
                        int read = recordMic.read(buffer, 0, buffer.length);
                        if (read > 0) currentRecordStream.write(buffer, 0, read);
                    }
                    recordMic.close();
                }).start();
            } catch (Exception e) {}
        } else {
            isRecording = false; btnRecord.setForeground(new Color(120, 120, 120));
            byte[] audioData = currentRecordStream.toByteArray();
            appendAudioMsg("", audioData, true);
            sendFragmentedData(f, (byte)3, audioData);
        }
    }

    private void sendText() {
        if(currentTargetUser == null || txtChat.getText().trim().isEmpty()) return;
        Friend f = friends.get(currentTargetUser);
        if(f != null && !f.ip.isEmpty() && f.port > 0) {
            try {
                byte[] textBytes = txtChat.getText().trim().getBytes("UTF-8"); byte[] packetData = new byte[textBytes.length + 1]; packetData[0] = 1;
                System.arraycopy(textBytes, 0, packetData, 1, textBytes.length);
                udpSocket.send(new DatagramPacket(packetData, packetData.length, InetAddress.getByName(f.ip), f.port));
                appendMsg("", txtChat.getText().trim(), true); txtChat.setText("");
            } catch (Exception e) {}
        } else JOptionPane.showMessageDialog(this, "bạn bè đang ngoại tuyến");
    }

    private void addChatBubble(JComponent comp, boolean isMe) {
        JPanel w = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT)); w.setOpaque(false);
        w.add(comp); chatPanel.add(w); chatPanel.revalidate(); chatPanel.repaint();
        JScrollBar vertical = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> vertical.setValue(vertical.getMaximum()));
    }

    private void appendMsg(String sender, String msg, boolean isMe) {
        boolean isEmoji = msg.length() <= 8 && !msg.matches(".*[a-zA-Z0-9].*");
        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);

        if(!isMe && !isEmoji && !sender.isEmpty()) {
            JLabel nameLbl = new JLabel(sender);
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            nameLbl.setForeground(new Color(150, 150, 150));
            nameLbl.setBorder(new EmptyBorder(0, 5, 2, 0));
            wrapper.add(nameLbl, BorderLayout.NORTH);
        }

        JLabel l = new JLabel(msg);

        if(!isEmoji) {
            l.setFont(new Font("SansSerif", Font.PLAIN, 15));
            BubblePanel bubble = new BubblePanel(isMe ? new Color(0, 132, 255) : Color.WHITE);
            l.setForeground(isMe ? Color.WHITE : new Color(30, 30, 30));
            bubble.add(l, BorderLayout.CENTER);
            wrapper.add(bubble, BorderLayout.CENTER);
        } else {
            l.setFont(new Font("SansSerif", Font.PLAIN, 45));
            wrapper.add(l, BorderLayout.CENTER);
        }
        addChatBubble(wrapper, isMe);
    }

    private void appendImageMsg(String sender, byte[] imgData, boolean isMe) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgData));
            int w = img.getWidth(), h = img.getHeight(), max = 220;
            if(w > max || h > max) { float ratio = Math.min((float)max/w, (float)max/h); w = Math.round(w*ratio); h = Math.round(h*ratio); }
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            JLabel l = new JLabel(new ImageIcon(scaled));

            JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
            if(!isMe && !sender.isEmpty()) {
                JLabel nameLbl = new JLabel(sender);
                nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                nameLbl.setForeground(new Color(150, 150, 150));
                nameLbl.setBorder(new EmptyBorder(0, 5, 2, 0));
                wrapper.add(nameLbl, BorderLayout.NORTH);
            }
            BubblePanel bubble = new BubblePanel(isMe ? new Color(0, 132, 255) : Color.WHITE);
            bubble.add(l, BorderLayout.CENTER);
            wrapper.add(bubble, BorderLayout.CENTER);
            addChatBubble(wrapper, isMe);
        } catch(Exception e) {}
    }

    private void appendFileMsg(String sender, byte[] payload, boolean isMe) {
        try {
            ByteBuffer bb = ByteBuffer.wrap(payload);
            int nameLen = bb.getInt(); byte[] nameBytes = new byte[nameLen]; bb.get(nameBytes);
            String fileName = new String(nameBytes, "UTF-8");
            byte[] fileData = new byte[payload.length - 4 - nameLen]; bb.get(fileData);

            FlatButton btn = new FlatButton("⬇ " + fileName, isMe ? new Color(0, 132, 255) : Color.WHITE, isMe ? Color.WHITE : new Color(30, 30, 30), 10);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
            btn.addActionListener(e -> {
                JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File(fileName));
                if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try { Files.write(fc.getSelectedFile().toPath(), fileData); JOptionPane.showMessageDialog(this, "lưu thành công"); } catch(Exception ex) {}
                }
            });

            JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
            if(!isMe && !sender.isEmpty()) {
                JLabel nameLbl = new JLabel(sender);
                nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                nameLbl.setForeground(new Color(150, 150, 150));
                nameLbl.setBorder(new EmptyBorder(0, 5, 2, 0));
                wrapper.add(nameLbl, BorderLayout.NORTH);
            }
            BubblePanel bubble = new BubblePanel(isMe ? new Color(0, 132, 255) : Color.WHITE);
            bubble.add(btn, BorderLayout.CENTER);
            wrapper.add(bubble, BorderLayout.CENTER);
            addChatBubble(wrapper, isMe);
        } catch(Exception e) {}
    }

    private void appendAudioMsg(String sender, byte[] audioData, boolean isMe) {
        FlatButton btnPlay = new FlatButton("► tin nhắn thoại", isMe ? new Color(0, 132, 255) : Color.WHITE, isMe ? Color.WHITE : new Color(30, 30, 30), 10);
        btnPlay.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnPlay.addActionListener(e -> {
            new Thread(() -> {
                try {
                    AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
                    SourceDataLine sdl = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
                    sdl.open(format); sdl.start(); sdl.write(audioData, 0, audioData.length); sdl.drain(); sdl.close();
                } catch(Exception ex) {}
            }).start();
        });

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
        if(!isMe && !sender.isEmpty()) {
            JLabel nameLbl = new JLabel(sender);
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            nameLbl.setForeground(new Color(150, 150, 150));
            nameLbl.setBorder(new EmptyBorder(0, 5, 2, 0));
            wrapper.add(nameLbl, BorderLayout.NORTH);
        }
        BubblePanel bubble = new BubblePanel(isMe ? new Color(0, 132, 255) : Color.WHITE);
        bubble.add(btnPlay, BorderLayout.CENTER);
        wrapper.add(bubble, BorderLayout.CENTER);
        addChatBubble(wrapper, isMe);
    }

    private void logout() {
        isRunning = false; closeCallUI(); isRecording = false;
        if(timer != null) timer.stop();
        if(udpSocket != null) udpSocket.close();
        if(recordMic != null) recordMic.close();
        if(speaker != null) speaker.close();
        new LoginClient().setVisible(true); this.dispose();
    }
}