package UI;

import controllers.MemberController;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import models.Member;

public class MembersUI extends JFrame {

    private JTextField studentIdField, firstNameField, lastNameField, emailField, departmentField, statusField;
    private JTable table;
    private DefaultTableModel tableModel;
    private final MemberController memberController;
    private int selectedMemberId = -1;

    // --- Consistent Palette ---
    private final Color ACCENT_BLUE = new Color(74, 144, 226);
    private final Color SIDEBAR_TOP = new Color(24, 28, 58);
    private final Color BG_SOFT = new Color(240, 242, 245);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color DANGER_RED = new Color(231, 76, 60);

    public MembersUI(String dbPath) {
        memberController = new MemberController(dbPath);
        initializeUI();
        loadMembers();
    }

    public MembersUI() {
        this("./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("SLMS Premium | Member Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_SOFT);
        setLayout(new BorderLayout());

        // LEFT PANEL: REGISTRATION FORM 
        JPanel leftPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 20, 15));
        leftPanel.setPreferredSize(new Dimension(350, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel titleLbl = new JLabel("Member Details");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(SIDEBAR_TOP);
        leftPanel.add(titleLbl);

        // Styling Inputs
        studentIdField = createStyledTextField("Student ID");
        firstNameField = createStyledTextField("First Name");
        lastNameField = createStyledTextField("Last Name");
        emailField = createStyledTextField("Email Address");
        departmentField = createStyledTextField("Department");
        statusField = createStyledTextField("Status (e.g. Active)");
//Addition
        leftPanel.add(new JLabel("Student ID"));
        leftPanel.add(studentIdField);
        leftPanel.add(new JLabel("First Name"));
        leftPanel.add(firstNameField);
        leftPanel.add(new JLabel("Last Name"));
        leftPanel.add(lastNameField);
        leftPanel.add(new JLabel("Email"));
        leftPanel.add(emailField);
        leftPanel.add(new JLabel("Department"));
        leftPanel.add(departmentField);
        leftPanel.add(new JLabel("Status"));
        leftPanel.add(statusField);

        // Action Buttons
        JButton addBtn = createActionBtn("Add Member", SUCCESS_GREEN);
        JButton updateBtn = createActionBtn("Update Info", ACCENT_BLUE);
        JButton deleteBtn = createActionBtn("Delete Member", DANGER_RED);
        JButton clearBtn = createActionBtn("Clear Form", new Color(149, 165, 166));

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.add(addBtn);
        btnGrid.add(updateBtn);
        btnGrid.add(deleteBtn);
        btnGrid.add(clearBtn);
        leftPanel.add(new JLabel(" ")); // Spacer
        leftPanel.add(btnGrid);

        add(leftPanel, BorderLayout.WEST);

        //  RIGHT PANEL: TABLE DATABASE 
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Student ID", "First Name", "Last Name", "Email", "Department", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        rightPanel.add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        //  EVENTS 
        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // UI STYLING HELPERS 
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(232, 241, 252));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(SIDEBAR_TOP);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 45));
    }

    //LOGIC METHODS  
    private void loadMembers() {
        tableModel.setRowCount(0);
        List<Member> members = memberController.getAllMembers();
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                m.getMemberId(), m.getStudentId(), m.getFirstName(),
                m.getLastName(), m.getEmail(), m.getDepartment(), m.getStatus()
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
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected member?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            memberController.deleteMember(selectedMemberId);
            loadMembers();
            clearForm();
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
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
