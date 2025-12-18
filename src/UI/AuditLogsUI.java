package UI;

import controllers.AuditLogController;
import models.AuditLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditLogsUI extends JFrame {

    private JTextField userIdField;
    private JTextField actionField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final AuditLogController logController;

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
        setTitle("Audit Logs");
        setSize(800, 400);
        setLocationRelativeTo(null);

        // ================= FILTER PANEL =================
        JPanel filterPanel = new JPanel();

        userIdField = new JTextField(5);
        actionField = new JTextField(10);
        JButton loadBtn = new JButton("Load");

        filterPanel.add(new JLabel("User ID"));
        filterPanel.add(userIdField);
        filterPanel.add(new JLabel("Action"));
        filterPanel.add(actionField);
        filterPanel.add(loadBtn);

        // ================= TABLE =================
        tableModel = new DefaultTableModel(
                new String[]{"Log ID", "User ID", "Action", "Target", "Time"}, 0
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // ================= LAYOUT =================
        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ================= EVENTS =================
        loadBtn.addActionListener(e -> applyFilters());
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
