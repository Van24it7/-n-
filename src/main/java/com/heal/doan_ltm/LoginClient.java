package com.heal.doan_ltm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.*;

public class LoginClient extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin, btnRegister;

    public LoginClient() {
        setTitle("skype p2p - đăng nhập");
        setSize(400, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel lblLogo = new JLabel("đăng nhập", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(new Color(0, 175, 240));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUser = new JTextField(); styleTextField(txtUser);
        txtPass = new JPasswordField(); styleTextField(txtPass);
        btnLogin = new JButton("đăng nhập"); styleButton(btnLogin, new Color(0, 175, 240));
        btnRegister = new JButton("tạo tài khoản mới"); styleButton(btnRegister, new Color(46, 204, 113));

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> showRegisterDialog());

        mainPanel.add(Box.createVerticalStrut(20)); mainPanel.add(lblLogo);
        mainPanel.add(Box.createVerticalStrut(40)); mainPanel.add(new JLabel("tài khoản:")); mainPanel.add(txtUser);
        mainPanel.add(Box.createVerticalStrut(15)); mainPanel.add(new JLabel("mật khẩu:")); mainPanel.add(txtPass);
        mainPanel.add(Box.createVerticalStrut(30)); mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(10)); mainPanel.add(btnRegister);

        add(mainPanel);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleButton(JButton btn, Color c) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(c); btn.setForeground(Color.WHITE); btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 0, 10, 0)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private void login() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String request = "LOGIN;;;" + txtUser.getText() + ";;;" + new String(txtPass.getPassword());
            byte[] sendData = request.getBytes("UTF-8");
            socket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), 8080));
            byte[] receiveData = new byte[65507];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.setSoTimeout(3000); socket.receive(receivePacket);
            String[] parts = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").split(";;;");
            if ("SUCCESS".equals(parts[0])) {
                String fn = parts.length > 1 ? parts[1] : "";
                String av = parts.length > 2 ? parts[2] : "";
                new MainClient(txtUser.getText(), fn, av).setVisible(true);
                this.dispose();
            } else JOptionPane.showMessageDialog(this, "sai tài khoản hoặc mật khẩu");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "lỗi máy chủ"); }
    }

    private void showRegisterDialog() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField user = new JTextField(); JPasswordField pass = new JPasswordField();
        JTextField name = new JTextField(); JTextField email = new JTextField();
        JTextField phone = new JTextField(); JComboBox<String> gender = new JComboBox<>(new String[]{"nam", "nữ", "khác"});
        p.add(new JLabel("tài khoản:")); p.add(user); p.add(new JLabel("mật khẩu:")); p.add(pass);
        p.add(new JLabel("tên hiển thị:")); p.add(name); p.add(new JLabel("email:")); p.add(email);
        p.add(new JLabel("số điện thoại:")); p.add(phone); p.add(new JLabel("giới tính:")); p.add(gender);
        if (JOptionPane.showConfirmDialog(this, p, "đăng ký", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (DatagramSocket socket = new DatagramSocket()) {
                String req = "REGISTER;;;" + user.getText() + ";;;" + new String(pass.getPassword()) + ";;;" + name.getText() + ";;;" + email.getText() + ";;;" + phone.getText() + ";;;" + gender.getSelectedItem();
                byte[] sendData = req.getBytes("UTF-8");
                socket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), 8080));
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.setSoTimeout(3000); socket.receive(receivePacket);
                if ("SUCCESS".equals(new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8"))) JOptionPane.showMessageDialog(this, "đăng ký thành công");
                else JOptionPane.showMessageDialog(this, "tài khoản đã tồn tại hoặc lỗi dữ liệu");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "lỗi kết nối"); }
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new LoginClient().setVisible(true)); }
}
