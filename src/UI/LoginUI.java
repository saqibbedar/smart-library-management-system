package UI;

import controllers.AuditLogController;
import controllers.UserController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Users;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final UserController userController;
    private final String dbPath;

    // --- Consistent Color Palette ---
    private final Color SIDEBAR_TOP = new Color(24, 28, 58);
    private final Color SIDEBAR_BOTTOM = new Color(44, 52, 107);
    private final Color ACCENT_BLUE = new Color(74, 144, 226);
    private final Color TEXT_MAIN = new Color(40, 44, 70);

    public LoginUI(String dbPath) {
        this.dbPath = dbPath;
        this.userController = new UserController(dbPath);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SLMS Premium | Secure Login");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Custom Gradient Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, SIDEBAR_TOP, 0, getHeight(), SIDEBAR_BOTTOM);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // ---------- LOGIN CARD ----------
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(350, 480));
        card.setBackground(Color.WHITE);
        // Using the helper class defined below
        card.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 30, 15));
        card.setBorder(new EmptyBorder(40, 30, 40, 30));

        // Header Section
        JLabel logoLabel = new JLabel("SLMS", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logoLabel.setForeground(ACCENT_BLUE);

        JLabel titleLabel = new JLabel("Library Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(120, 120, 120));

        // Username Field
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(TEXT_MAIN);
        usernameField = new JTextField();
        styleTextField(usernameField);

        // Password Field
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLbl.setForeground(TEXT_MAIN);
        passwordField = new JPasswordField();
        styleTextField(passwordField);

        // Login Button
        JButton loginButton = new JButton("LOGIN TO SYSTEM");
        styleLoginButton(loginButton);

        // Add components to card
        card.add(logoLabel);
        card.add(titleLabel);
        card.add(new JLabel(" ")); // Spacer
        card.add(userLbl);
        card.add(usernameField);
        card.add(passLbl);
        card.add(passwordField);
        card.add(new JLabel(" ")); // Spacer
        card.add(loginButton);

        backgroundPanel.add(card);

        loginButton.addActionListener(e -> handleLogin());
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(280, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(245, 247, 250));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleLoginButton(JButton btn) {
        btn.setPreferredSize(new Dimension(280, 45));
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_BLUE.brighter());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT_BLUE);
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Users user = userController.login(username, password);

        if (user != null && user.isActive()) {
            // Log successful login
            new AuditLogController(dbPath).logAction(user.getUserId(), "LOGIN", "AUTH");

            dispose();
            new DashboardUI(user, dbPath).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials or inactive account", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/**
 * HELPER CLASS: VerticalFlowLayout This allows us to stack the Login components
 * vertically inside the card.
 */
class VerticalFlowLayout extends FlowLayout {

    public static final int TOP = 0;

    public VerticalFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            int nmembers = target.getComponentCount();
            int width = 0, height = getVgap();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    width = Math.max(width, d.width);
                    height += d.height + getVgap();
                }
            }
            return new Dimension(width + getHgap() * 2, height + getVgap());
        }
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            int nmembers = target.getComponentCount();
            int x = getHgap(), y = getVgap();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    m.setBounds(x, y, target.getWidth() - x * 2, d.height);
                    y += d.height + getVgap();
                }
            }
        }
    }
}
