package UI;

import controllers.AuditLogController;
import controllers.FineController;
import controllers.MemberController;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import models.Fine;
import models.Member;
import models.Users;

public class FinesUI extends JFrame {

    private JTextField studentIdField;
    private JTable table;
    private DefaultTableModel tableModel;

    private final MemberController memberController;
    private final FineController fineController;
    private final AuditLogController auditLogController;
    private final Users loggedInUser;

    private int selectedFineId = -1;

    // Corporate Palette (from temp UI theme)
    private final Color SIDEBAR_COLOR = new Color(245, 246, 250);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(210, 218, 226);
    private final Color SUCCESS_GREEN = new Color(39, 174, 96);

    public FinesUI(Users user, String dbPath) {
        this.loggedInUser = user;
        memberController = new MemberController(dbPath);
        fineController = new FineController(dbPath);
        auditLogController = new AuditLogController(dbPath);

        initializeUI();
    }

    public FinesUI(String dbPath) {
        this(null, dbPath);
    }

    // Backwards-compatible constructor
    public FinesUI() {
        this(null, "./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("SLMS | Fines Management");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // ================= LEFT: ACTION PANEL =================
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(300, 500));
        leftPanel.setBackground(SIDEBAR_COLOR);
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 25, 10, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("Fine Actions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        leftPanel.add(title, gbc);

        gbc.insets = new Insets(20, 25, 5, 25);
        gbc.gridy = 1;
        leftPanel.add(new JLabel("Member Student ID:"), gbc);

        studentIdField = new JTextField();
        styleTextField(studentIdField);
        gbc.gridy = 2;
        leftPanel.add(studentIdField, gbc);

        JButton loadBtn = createStyledButton("Load Fines", ACCENT_COLOR);
        gbc.gridy = 3;
        leftPanel.add(loadBtn, gbc);

        JSeparator sep = new JSeparator();
        gbc.insets = new Insets(30, 25, 10, 25);
        gbc.gridy = 4;
        leftPanel.add(sep, gbc);

        JButton payBtn = createStyledButton("Mark Paid", SUCCESS_GREEN);
        gbc.gridy = 5;
        leftPanel.add(payBtn, gbc);

        JButton waiveBtn = createStyledButton("Waive", new Color(127, 140, 141));
        gbc.gridy = 6;
        leftPanel.add(waiveBtn, gbc);

        // ================= RIGHT: TABLE PANEL =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel tableLabel = new JLabel("Outstanding & Paid Fines");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        rightPanel.add(tableLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Fine ID", "Issue ID", "Amount", "Days", "Status"}, 0
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
        loadBtn.addActionListener(e -> loadFines());
        payBtn.addActionListener(e -> markPaid());
        waiveBtn.addActionListener(e -> waiveFine());

        table.getSelectionModel().addListSelectionListener(e -> selectFine());
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
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(232, 241, 249));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 35));
    }

    // ================= LOGIC =================

    private void loadFines() {
        tableModel.setRowCount(0);
        selectedFineId = -1;

        Member member = memberController.getMemberByStudentId(studentIdField.getText().trim());
        if (member == null) {
            showError("Member not found");
            return;
        }

        List<Fine> fines = fineController.getFinesForMember(member.getMemberId());

        for (Fine f : fines) {
            tableModel.addRow(new Object[]{
                    f.getFineId(),
                    f.getIssueId(),
                    f.getAmount(),
                    f.getOverdueDays(),
                    f.getStatus()
            });
        }
    }

    private void selectFine() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedFineId = (int) tableModel.getValueAt(row, 0);
    }

    private void markPaid() {
        if (selectedFineId == -1) {
            showError("Select a fine first");
            return;
        }

        if (fineController.markFineAsPaid(selectedFineId)) {
            loadFines();
            if (loggedInUser != null) {
                auditLogController.logAction(loggedInUser.getUserId(), "FINE_PAID", "FINE:" + selectedFineId);
            }
        } else {
            showError("Failed to mark fine as paid");
        }
    }

    private void waiveFine() {
        if (selectedFineId == -1) {
            showError("Select a fine first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Waive selected fine?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            fineController.waiveFine(selectedFineId);
            loadFines();
            if (loggedInUser != null) {
                auditLogController.logAction(loggedInUser.getUserId(), "FINE_WAIVED", "FINE:" + selectedFineId);
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
