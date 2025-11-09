package com.netQuiz.client.swing;

import com.netQuiz.client.service.ClientServiceManager;
import javax.swing.*;
import java.awt.*;

public class LoginSwing extends JFrame {
    private JTextField usernameField;
    private JButton loginButton;
    private JLabel statusLabel;
    private final ClientServiceManager serviceManager = ClientServiceManager.getInstance();

    public LoginSwing() {
        setTitle("NetQuiz - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLayout(new BorderLayout());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("NetQuiz");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);

        center.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel subtitle = new JLabel("Multi-User Quiz & Chat Platform");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(Color.DARK_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(subtitle);

        center.add(Box.createRigidArea(new Dimension(0, 20)));
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(new JLabel("Username:"));
        usernameField = new JTextField();
        form.add(usernameField);

        center.add(form);

        center.add(Box.createRigidArea(new Dimension(0, 12)));
        loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());
        center.add(loginButton);

        center.add(Box.createRigidArea(new Dimension(0, 12)));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(statusLabel);

        add(center, BorderLayout.CENTER);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Enter username");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Connecting...");

        new Thread(() -> {
            try {
                serviceManager.setUsername(username);
                
                // Create main window first (but don't show it yet)
                MainSwing main = new MainSwing();
                
                // Login with user list update handler
                boolean success = serviceManager.getUserService().login(
                    username, 
                    "password", 
                    users -> main.updateUserList(users),  // User list handler
                    msg -> System.out.println("[System] " + msg)  // Message handler
                );

                if (success) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        main.setLocationRelativeTo(null);
                        main.setVisible(true);
                        dispose();
                    });
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Login failed");
                        loginButton.setEnabled(true);
                    });
                }
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection error: " + ex.getMessage());
                    loginButton.setEnabled(true);
                });
            }
        }).start();
    }
}
