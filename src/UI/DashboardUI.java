package UI;

import models.Users;

import javax.swing.*;
import java.awt.*;

public class DashboardUI extends JFrame {

    private final Users loggedInUser;
        private final String dbPath;

        public DashboardUI(Users user, String dbPath) {
                this.loggedInUser = user;
                this.dbPath = dbPath;
        initializeUI();
    }

        // Backwards-compatible constructor (defaults to local DB next to app)
        public DashboardUI(Users user) {
                this(user, "./SLMS-DB.accdb");
        }

    private void initializeUI() {
        setTitle("SLMS Dashboard");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ---------- Top Info ----------
        JLabel userInfo = new JLabel(
                "Logged in as: " + loggedInUser.getFullName() +
                " (" + loggedInUser.getRole() + ")",
                SwingConstants.CENTER
        );
        userInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(userInfo, BorderLayout.NORTH);

        // ---------- Center Buttons ----------
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton membersBtn = new JButton("Manage Members");
        JButton booksBtn = new JButton("Manage Books");
        JButton copiesBtn = new JButton("Book Copies");
        JButton issueBtn = new JButton("Issue / Return");
        JButton finesBtn = new JButton("Fines");
        JButton logsBtn = new JButton("Audit Logs");

        buttonPanel.add(membersBtn);
        buttonPanel.add(booksBtn);
        buttonPanel.add(copiesBtn);
        buttonPanel.add(issueBtn);
        buttonPanel.add(finesBtn);

        // Show logs only for Admin
        if (loggedInUser.getRole().equalsIgnoreCase("ADMIN")) {
            buttonPanel.add(logsBtn);
        }

        add(buttonPanel, BorderLayout.CENTER);

        // ---------- Logout ----------
        JButton logoutBtn = new JButton("Logout");
        add(logoutBtn, BorderLayout.SOUTH);

        // ---------- Button Actions ----------
        membersBtn.addActionListener(e ->
                new MembersUI(dbPath).setVisible(true));

        booksBtn.addActionListener(e ->
                new BooksUI(dbPath).setVisible(true));

        copiesBtn.addActionListener(e ->
                new BookCopiesUI(dbPath).setVisible(true));

        issueBtn.addActionListener(e ->
                new IssueReturnUI(dbPath).setVisible(true));

        finesBtn.addActionListener(e ->
                new FinesUI(dbPath).setVisible(true));

        logsBtn.addActionListener(e ->
                new AuditLogsUI(dbPath).setVisible(true));

        logoutBtn.addActionListener(e -> {
            dispose();
                        new LoginUI(dbPath).setVisible(true);
        });
    }
}
