package UI;

import controllers.AuditLogController;
import controllers.FineController;
import controllers.MemberController;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        setTitle("Fines Management");
        setSize(700, 400);
        setLocationRelativeTo(null);

        // ================= TOP =================
        JPanel topPanel = new JPanel();

        studentIdField = new JTextField(15);
        JButton loadBtn = new JButton("Load Fines");

        topPanel.add(new JLabel("Student ID"));
        topPanel.add(studentIdField);
        topPanel.add(loadBtn);

        // ================= TABLE =================
        tableModel = new DefaultTableModel(
                new String[]{"Fine ID", "Issue ID", "Amount", "Days", "Status"}, 0
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // ================= BUTTONS =================
        JPanel buttonPanel = new JPanel();

        JButton payBtn = new JButton("Mark Paid");
        JButton waiveBtn = new JButton("Waive");

        buttonPanel.add(payBtn);
        buttonPanel.add(waiveBtn);

        // ================= LAYOUT =================
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // ================= EVENTS =================
        loadBtn.addActionListener(e -> loadFines());
        payBtn.addActionListener(e -> markPaid());
        waiveBtn.addActionListener(e -> waiveFine());

        table.getSelectionModel().addListSelectionListener(e -> selectFine());
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
