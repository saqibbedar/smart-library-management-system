package UI;

import controllers.UserController;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import models.Users;

public class LibrariansUI extends JFrame {

    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField shiftField;
    private JCheckBox activeCheck;
    private JPasswordField passwordField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final UserController userController;
    private final Users loggedInUser;
    private int selectedUserId = -1;

    // --- Match MembersUI theme/palette ---
    private final Color ACCENT_BLUE = new Color(74, 144, 226);
    private final Color SIDEBAR_TOP = new Color(24, 28, 58);
    private final Color BG_SOFT = new Color(240, 242, 245);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);

    public LibrariansUI(Users user, String dbPath) {
        this.loggedInUser = user;
        this.userController = new UserController(dbPath);

        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            JOptionPane.showMessageDialog(null, "Access denied: Admins only", "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }

        initializeUI();
        loadLibrarians();
    }

    public LibrariansUI(String dbPath) {
        this(null, dbPath);
    }

    // Backwards-compatible constructor
    public LibrariansUI() {
        this(null, "./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("SLMS Premium | Librarian User Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_SOFT);
        setLayout(new BorderLayout());

        // LEFT PANEL: USER FORM
        JPanel leftPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 20, 15));
        leftPanel.setPreferredSize(new Dimension(350, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel titleLbl = new JLabel("Librarian User Details");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(SIDEBAR_TOP);
        leftPanel.add(titleLbl);

        usernameField = createStyledTextField();
        fullNameField = createStyledTextField();
        shiftField = createStyledTextField();
        passwordField = new JPasswordField();
        stylePasswordField(passwordField);
        activeCheck = new JCheckBox("Active");
        activeCheck.setOpaque(false);
        activeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        leftPanel.add(new JLabel("Username"));
        leftPanel.add(usernameField);
        leftPanel.add(new JLabel("Full Name"));
        leftPanel.add(fullNameField);
        leftPanel.add(new JLabel("Shift"));
        leftPanel.add(shiftField);
        leftPanel.add(new JLabel("Password (for new user / reset)"));
        leftPanel.add(passwordField);
        leftPanel.add(activeCheck);

        JButton addBtn = createActionBtn("Add Librarian", SUCCESS_GREEN);
        JButton resetBtn = createActionBtn("Reset Password", ACCENT_BLUE);
        JButton clearBtn = createActionBtn("Clear Form", new Color(149, 165, 166));

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.add(addBtn);
        btnGrid.add(resetBtn);
        btnGrid.add(clearBtn);
        btnGrid.add(new JLabel(""));

        leftPanel.add(new JLabel(" "));
        leftPanel.add(btnGrid);

        add(leftPanel, BorderLayout.WEST);

        // RIGHT PANEL: TABLE
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Username", "Full Name", "Role", "Shift", "Active"}, 0
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

        // EVENTS
        addBtn.addActionListener(e -> addLibrarian());
        resetBtn.addActionListener(e -> resetPassword());
        clearBtn.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    private void loadLibrarians() {
        tableModel.setRowCount(0);
        List<Users> librarians = userController.getUsersByRole("LIBRARIAN");
        for (Users u : librarians) {
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    u.getShift(),
                    u.isActive()
            });
        }
    }

    private void addLibrarian() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String shift = shiftField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || fullName.isEmpty() || shift.isEmpty() || password.isEmpty()) {
            showError("Please fill username, full name, shift, and password");
            return;
        }

        Users u = new Users();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole("LIBRARIAN");
        u.setShift(shift);
        u.setActive(activeCheck.isSelected());
        u.setPasswordHash(UserController.hashPassword(password));

        if (userController.createUser(u)) {
            loadLibrarians();
            clearForm();
        } else {
            showError("Failed to add librarian user");
        }
    }

    private void resetPassword() {
        if (selectedUserId == -1) {
            showError("Select a librarian first");
            return;
        }

        String password = new String(passwordField.getPassword()).trim();
        if (password.isEmpty()) {
            showError("Enter a new password to reset");
            return;
        }

        boolean ok = userController.resetPassword(selectedUserId, password);
        if (ok) {
            showInfo("Password reset successfully");
            passwordField.setText("");
        } else {
            showError("Failed to reset password");
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedUserId = (int) tableModel.getValueAt(row, 0);
        usernameField.setText(tableModel.getValueAt(row, 1).toString());
        fullNameField.setText(tableModel.getValueAt(row, 2).toString());
        shiftField.setText(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
        Object active = tableModel.getValueAt(row, 5);
        activeCheck.setSelected(active instanceof Boolean ? (Boolean) active : Boolean.parseBoolean(String.valueOf(active)));
        passwordField.setText("");
    }

    private void clearForm() {
        selectedUserId = -1;
        usernameField.setText("");
        fullNameField.setText("");
        shiftField.setText("");
        passwordField.setText("");
        activeCheck.setSelected(true);
        table.clearSelection();
    }

    // UI STYLING HELPERS (match MembersUI)
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setPreferredSize(new Dimension(0, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
