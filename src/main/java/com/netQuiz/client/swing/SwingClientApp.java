package com.netQuiz.client.swing;

public class SwingClientApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginSwing login = new LoginSwing();
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }
}
