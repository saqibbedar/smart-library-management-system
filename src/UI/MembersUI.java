package UI;

import controllers.MemberController;
import models.Member;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MembersUI extends JFrame {

    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField statusField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final MemberController memberController;
    private int selectedMemberId = -1;

    public MembersUI(String dbPath) {
        memberController = new MemberController(dbPath);
        initializeUI();
        loadMembers();
    }

    // Backwards-compatible constructor
    public MembersUI() {
        this("./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("Manage Members");
        setSize(900, 400);
        setLocationRelativeTo(null);

        // ================= FORM PANEL =================
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Member Details"));

        studentIdField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        departmentField = new JTextField();
        statusField = new JTextField();

        formPanel.add(new JLabel("Student ID"));
        formPanel.add(new JLabel("First Name"));
        formPanel.add(new JLabel("Last Name"));
        formPanel.add(new JLabel("Email"));
        formPanel.add(new JLabel("Department"));
        formPanel.add(new JLabel("Status"));

        formPanel.add(studentIdField);
        formPanel.add(firstNameField);
        formPanel.add(lastNameField);
        formPanel.add(emailField);
        formPanel.add(departmentField);
        formPanel.add(statusField);

        // ================= BUTTON PANEL =================
        JPanel buttonPanel = new JPanel();

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        // ================= TABLE =================
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Student ID", "First Name", "Last Name", "Email", "Department", "Status"}, 0
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // ================= LAYOUT =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ================= EVENTS =================

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ================= LOGIC METHODS =================

    private void loadMembers() {
        tableModel.setRowCount(0);
        List<Member> members = memberController.getAllMembers();

        for (Member m : members) {
            tableModel.addRow(new Object[]{
                    m.getMemberId(),
                    m.getStudentId(),
                    m.getFirstName(),
                    m.getLastName(),
                    m.getEmail(),
                    m.getDepartment(),
                    m.getStatus()
            });
        }
    }

    private void addMember() {
        Member m = buildMemberFromForm();

        if (memberController.createMember(m)) {
            loadMembers();
            clearForm();
        } else {
            showError("Failed to add member");
        }
    }

    private void updateMember() {
        if (selectedMemberId == -1) {
            showError("Select a member first");
            return;
        }

        Member m = buildMemberFromForm();
        m.setMemberId(selectedMemberId);

        if (memberController.updateMember(m)) {
            loadMembers();
            clearForm();
        } else {
            showError("Failed to update member");
        }
    }

    private void deleteMember() {
        if (selectedMemberId == -1) {
            showError("Select a member first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected member?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            memberController.deleteMember(selectedMemberId);
            loadMembers();
            clearForm();
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedMemberId = (int) tableModel.getValueAt(row, 0);
        studentIdField.setText(tableModel.getValueAt(row, 1).toString());
        firstNameField.setText(tableModel.getValueAt(row, 2).toString());
        lastNameField.setText(tableModel.getValueAt(row, 3).toString());
        emailField.setText(tableModel.getValueAt(row, 4).toString());
        departmentField.setText(tableModel.getValueAt(row, 5).toString());
        statusField.setText(tableModel.getValueAt(row, 6).toString());
    }

    private Member buildMemberFromForm() {
        Member m = new Member();
        m.setStudentId(studentIdField.getText().trim());
        m.setFirstName(firstNameField.getText().trim());
        m.setLastName(lastNameField.getText().trim());
        m.setEmail(emailField.getText().trim());
        m.setDepartment(departmentField.getText().trim());
        m.setStatus(statusField.getText().trim());
        return m;
    }

    private void clearForm() {
        selectedMemberId = -1;
        studentIdField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        departmentField.setText("");
        statusField.setText("");
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
