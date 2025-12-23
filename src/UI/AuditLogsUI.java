package UI;

import controllers.AuditLogController;
import models.AuditLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.util.List;

public class AuditLogsUI extends JFrame {

    private JTextField userIdField;
    private JTextField actionField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final AuditLogController logController;

    // Corporate Palette (from temp UI theme)
    private final Color SIDEBAR_COLOR = new Color(245, 246, 250);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(210, 218, 226);

    public AuditLogsUI(String dbPath) {
        logController = new AuditLogController(dbPath);
        initializeUI();
        loadAllLogs();
    }

    // Backwards-compatible constructor
    public AuditLogsUI() {
        this("./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("SLMS | Audit Logs");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // ================= LEFT: FILTER PANEL =================
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(280, 600));
        leftPanel.setBackground(SIDEBAR_COLOR);
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 25, 10, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("Filter Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        leftPanel.add(title, gbc);

        gbc.insets = new Insets(20, 25, 5, 25);
        gbc.gridy = 1;
        leftPanel.add(new JLabel("Search User ID:"), gbc);
        userIdField = new JTextField();
        styleTextField(userIdField);
        gbc.gridy = 2;
        leftPanel.add(userIdField, gbc);

        gbc.insets = new Insets(15, 25, 5, 25);
        gbc.gridy = 3;
        leftPanel.add(new JLabel("Search Action:"), gbc);
        actionField = new JTextField();
        styleTextField(actionField);
        gbc.gridy = 4;
        leftPanel.add(actionField, gbc);

        JButton loadBtn = createStyledButton("Apply Filters", ACCENT_COLOR);
        gbc.insets = new Insets(25, 25, 10, 25);
        gbc.gridy = 5;
        leftPanel.add(loadBtn, gbc);

        JButton resetBtn = createStyledButton("Reset View", new Color(127, 140, 141));
        gbc.gridy = 6;
        leftPanel.add(resetBtn, gbc);

        // ================= RIGHT: DATA TABLE =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel tableLabel = new JLabel("System Activity History");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        rightPanel.add(tableLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"Log ID", "User ID", "Action", "Target", "Time"}, 0
        );
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel);

        // ================= EVENTS =================
        loadBtn.addActionListener(e -> applyFilters());
        resetBtn.addActionListener(e -> {
            userIdField.setText("");
            actionField.setText("");
            loadAllLogs();
        });
    }

    // ================= UI HELPERS (STYLE ONLY) =================
    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setGridColor(new Color(240, 240, 240));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(232, 241, 249));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 40));
    }

    // ================= LOGIC =================

    private void loadAllLogs() {
        tableModel.setRowCount(0);
        List<AuditLog> logs = logController.getAllLogs();

        for (AuditLog log : logs) {
            addRow(log);
        }
    }

    private void applyFilters() {
        tableModel.setRowCount(0);

        String userIdText = userIdField.getText().trim();
        String actionText = actionField.getText().trim();

        List<AuditLog> logs;

        if (!userIdText.isEmpty()) {
            logs = logController.getLogsByUser(Integer.parseInt(userIdText));
        } else if (!actionText.isEmpty()) {
            logs = logController.getLogsByAction(actionText);
        } else {
            logs = logController.getAllLogs();
        }

        for (AuditLog log : logs) {
            addRow(log);
        }
    }

    private void addRow(AuditLog log) {
        tableModel.addRow(new Object[]{
                log.getLogId(),
                log.getUserId(),
                log.getAction(),
                log.getTargetType(),
                log.getLogTime()
        });
    }
}
