package UI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Users;

public class DashboardUI extends JFrame {

    private final Users loggedInUser;
    private final String dbPath; // Added to fix the NoSuchMethodError

    // Professional Color Palette
    private final Color SIDEBAR_COLOR = new Color(44, 62, 80);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HOVER_COLOR = new Color(41, 128, 185);

    // Fixed Constructor to match LoginUI's call
    public DashboardUI(Users user, String dbPath) {
        this.loggedInUser = user;
        this.dbPath = dbPath;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SLMS | Library Management System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- LEFT SIDEBAR ----------
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BorderLayout());

        // Profile Section
        JPanel profileSection = new JPanel(new GridLayout(2, 1));
        profileSection.setOpaque(false);
        profileSection.setBorder(new EmptyBorder(50, 25, 50, 25));

        JLabel roleLabel = new JLabel(loggedInUser.getRole().toUpperCase());
        roleLabel.setForeground(ACCENT_COLOR);
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel nameLabel = new JLabel(loggedInUser.getFullName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        profileSection.add(roleLabel);
        profileSection.add(nameLabel);
        sidebar.add(profileSection, BorderLayout.NORTH);

        // Logout Button at Bottom
        JButton logoutBtn = createSidebarButton("Logout", new Color(231, 76, 60));
        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        logoutWrapper.add(logoutBtn);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);

        // ---------- MAIN CONTENT AREA ----------
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcomeText = new JLabel("System Dashboard");
        welcomeText.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeText.setForeground(SIDEBAR_COLOR);
        mainPanel.add(welcomeText, BorderLayout.NORTH);

        // Button Grid
        JPanel grid = new JPanel(new GridLayout(3, 2, 25, 25));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(30, 0, 0, 0));

        JButton membersBtn = createGridButton("Manage Members", "User icons...");
        JButton booksBtn = createGridButton("Manage Books", "Library inventory...");
        JButton copiesBtn = createGridButton("Book Copies", "Track physical units...");
        JButton issueBtn = createGridButton("Issue / Return", "Circulation desk...");
        JButton finesBtn = createGridButton("Fines", "Financial records...");
        JButton logsBtn = createGridButton("Audit Logs", "System history...");

        grid.add(membersBtn);
        grid.add(booksBtn);
        grid.add(copiesBtn);
        grid.add(issueBtn);
        grid.add(finesBtn);

        if (loggedInUser.getRole().equalsIgnoreCase("ADMIN")) {
            grid.add(logsBtn);
        }

        mainPanel.add(grid, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // ---------- BUTTON FUNCTIONALITY ----------
        membersBtn.addActionListener(e -> new MembersUI().setVisible(true));
        booksBtn.addActionListener(e -> new BooksUI().setVisible(true));
        copiesBtn.addActionListener(e -> new BookCopiesUI().setVisible(true));
        issueBtn.addActionListener(e -> new IssueReturnUI().setVisible(true));
        finesBtn.addActionListener(e -> new FinesUI().setVisible(true));
        logsBtn.addActionListener(e -> new AuditLogsUI().setVisible(true));

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginUI(dbPath).setVisible(true); // Using the passed dbPath
        });
    }

    private JButton createGridButton(String title, String subtitle) {
        JButton btn = new JButton("<html><div style='text-align: left; padding-left:10px;'>"
                + "<b style='font-size:13px;'>" + title + "</b><br>"
                + "<i style='font-size:10px; color:#7f8c8d;'>" + subtitle + "</i></div></html>");

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(Color.WHITE);
        btn.setForeground(SIDEBAR_COLOR);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(250, 252, 255));
                btn.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230), 1));
            }
        });
        return btn;
    }

    private JButton createSidebarButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
