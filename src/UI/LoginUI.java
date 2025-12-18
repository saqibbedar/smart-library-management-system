package UI;

import controllers.UserController;
import models.Users;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    private final UserController userController;
    private final String dbPath;

    public LoginUI(String dbPath) {
        this.dbPath = dbPath;
        this.userController = new UserController(dbPath);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SLMS - Login");
        setSize(350, 200);
        setLocationRelativeTo(null); // center screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ---------- Username ----------
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // ---------- Password ----------
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // ---------- Login Button ----------
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        add(panel);

        // ---------- Button Action ----------
        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter username and password",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // NOTE: password hashing can be added later
        Users user = userController.login(username, password);

        if (user != null && user.isActive()) {
            JOptionPane.showMessageDialog(this,
                    "Welcome " + user.getFullName(),
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // Close login
            dispose();

            // TEMP: Open dashboard placeholder
            new DashboardUI(user, dbPath).setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid credentials or inactive account",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
