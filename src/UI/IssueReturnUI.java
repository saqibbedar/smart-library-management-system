package UI;

import controllers.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import models.*;

public class IssueReturnUI extends JFrame {

    private JTextField studentIdField;
    private JTextField barcodeField;
    private JTextArea outputArea;

    private final MemberController memberController;
    private final BookController bookController;
    private final BookCopyController copyController;
    private final IssueController issueController;
    private final FineController fineController;
    private final AuditLogController auditLogController;
    private final Users loggedInUser;

    // Corporate Palette (from temp UI theme)
    private final Color SIDEBAR_COLOR = new Color(245, 246, 250);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(210, 218, 226);

    public IssueReturnUI(Users user, String dbPath) {
        this.loggedInUser = user;
        memberController = new MemberController(dbPath);
        bookController = new BookController(dbPath);
        copyController = new BookCopyController(dbPath);
        issueController = new IssueController(dbPath);
        fineController = new FineController(dbPath);
        auditLogController = new AuditLogController(dbPath);

        initializeUI();
    }

    public IssueReturnUI(String dbPath) {
        this(null, dbPath);
    }

    // Backwards-compatible constructor
    public IssueReturnUI() {
        this(null, "./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("SLMS | Circulation Desk");
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // ================= LEFT: TRANSACTION FORM =================
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(380, 500));
        leftPanel.setBackground(SIDEBAR_COLOR);
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 25, 8, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("Transaction Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_COLOR);
        leftPanel.add(title, gbc);

        studentIdField = createStyledTextField();
        barcodeField = createStyledTextField();

        gbc.gridy = 1;
        leftPanel.add(new JLabel("Member Student ID"), gbc);
        gbc.gridy = 2;
        leftPanel.add(studentIdField, gbc);
        gbc.gridy = 3;
        leftPanel.add(new JLabel("Book Copy Barcode"), gbc);
        gbc.gridy = 4;
        leftPanel.add(barcodeField, gbc);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);

        JButton issueBtn = createPrimaryButton("Issue Book", new Color(39, 174, 96));
        JButton returnBtn = createPrimaryButton("Return Book", ACCENT_COLOR);
        btnPanel.add(issueBtn);
        btnPanel.add(returnBtn);

        gbc.gridy = 5;
        gbc.insets = new Insets(30, 25, 10, 25);
        leftPanel.add(btnPanel, gbc);

        // ================= RIGHT: LOG AREA =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel logHeader = new JLabel("System Logs & Details");
        logHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(252, 252, 252));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR));

        rightPanel.add(logHeader, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel);

        // ================= EVENTS =================
        issueBtn.addActionListener(e -> issueBook());
        returnBtn.addActionListener(e -> returnBook());
    }

    // ================= UI HELPERS (STYLE ONLY) =================
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(280, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 0, 10, 0));
        return btn;
    }

    // ================= LOGIC =================

    private void issueBook() {
        outputArea.setText("");

        Member member = memberController.getMemberByStudentId(studentIdField.getText().trim());
        if (member == null) {
            log("Member not found");
            return;
        }

        BookCopy copy = copyController.getCopyByBarcode(barcodeField.getText().trim());
        if (copy == null) {
            log("Book copy not found");
            return;
        }

        if (!copyController.isCopyAvailable(copy.getCopyId())) {
            log("Book copy is not available");
            return;
        }

        if (issueController.memberHasIssuedBook(member.getMemberId())) {
            log("Member already has an issued book");
            return;
        }

        boolean success = issueController.issueBook(
                member.getMemberId(),
                copy.getCopyId(),
                copy.getBookId()
        );

        if (success) {
            log("Book issued successfully");

            // --- Additional details (no change to existing issue logic) ---
            int activeIssueId = getActiveIssueIdForCopy(copy.getCopyId());
            IssueTransaction activeIssue = activeIssueId != -1 ? issueController.getIssueById(activeIssueId) : null;
            Book book = bookController.getBookById(copy.getBookId());
            appendIssueDetails(member, book, activeIssue != null ? activeIssue.getDueDate() : null);

            if (loggedInUser != null) {
                auditLogController.logAction(loggedInUser.getUserId(), "ISSUE", "COPY:" + copy.getCopyId());
            }
        } else {
            log("Failed to issue book");
        }
    }

    private void returnBook() {
        outputArea.setText("");

        Member member = memberController.getMemberByStudentId(studentIdField.getText().trim());
        if (member == null) {
            log("Member not found");
            return;
        }

        BookCopy copy = copyController.getCopyByBarcode(barcodeField.getText().trim());
        if (copy == null) {
            log("Book copy not found");
            return;
        }

        // Must be currently issued
        IssueTransaction activeIssue = issueController.getActiveIssueByCopyId(copy.getCopyId());
        if (activeIssue == null) {
            log("This book copy is not currently issued");
            return;
        }

        // Must be issued to the same member attempting the return
        if (activeIssue.getMemberId() != member.getMemberId()) {
            log("Return denied: this copy is issued to another member (memberId=" + activeIssue.getMemberId() + ")");
            return;
        }

        IssueTransaction issue = issueController.getIssueById(activeIssue.getIssueId());
        if (issue == null) {
            log("Active issue record could not be loaded");
            return;
        }

        boolean success = issueController.returnBook(
                issue.getIssueId(),
                copy.getCopyId(),
                copy.getBookId()
        );

        if (!success) {
            log("Failed to return book");
            return;
        }

        // ---------- Fine handling ----------
        Fine fine = fineController.calculateFine(issue.getIssueId(), 10.0);

        if (fine != null && fine.getAmount() > 0) {
            fineController.createFine(fine);
            log("Book returned with fine: PKR " + fine.getAmount());
            if (loggedInUser != null) {
                auditLogController.logAction(loggedInUser.getUserId(), "FINE_CREATED", "ISSUE:" + issue.getIssueId());
            }
        } else {
            log("Book returned successfully. No fine.");
        }

        // --- Additional details (no change to existing return logic) ---
        IssueTransaction returnedIssue = issueController.getIssueById(issue.getIssueId());
        Book book = bookController.getBookById(copy.getBookId());
        appendReturnDetails(
                member,
                book,
                returnedIssue != null ? returnedIssue.getDueDate() : issue.getDueDate(),
                returnedIssue != null ? returnedIssue.getReturnDate() : null,
                (fine != null ? fine.getAmount() : 0.0)
        );

        if (loggedInUser != null) {
            auditLogController.logAction(loggedInUser.getUserId(), "RETURN", "COPY:" + copy.getCopyId());
        }
    }

    // ================= HELPERS =================

    private int getActiveIssueIdForCopy(int copyId) {
        // Delegate to controller: active issue = status ISSUED and matching copyId
        IssueTransaction it = issueController.getActiveIssueByCopyId(copyId);
        return it != null ? it.getIssueId() : -1;
    }

    private void log(String msg) {
        outputArea.append(msg + "\n");
    }

    private void appendIssueDetails(Member member, Book book, Date dueDate) {
        log("----------------------------------------");
        log("Issue Details");
        log("Member Name     : " + formatFullName(member));
        log("Department      : " + safe(member != null ? member.getDepartment() : null));
        log("Student/Reg ID  : " + safe(member != null ? member.getStudentId() : null));
        log("Book Title      : " + safe(book != null ? book.getTitle() : null));
        log("Due Date        : " + formatDate(dueDate));
        log("----------------------------------------");
    }

    private void appendReturnDetails(Member member, Book book, Date dueDate, Date returnDate, double fineAmount) {
        log("----------------------------------------");
        log("Return Details");
        log("Member Name     : " + formatFullName(member));
        log("Department      : " + safe(member != null ? member.getDepartment() : null));
        log("Student/Reg ID  : " + safe(member != null ? member.getStudentId() : null));
        log("Book Title      : " + safe(book != null ? book.getTitle() : null));
        log("Due Date        : " + formatDate(dueDate));
        log("Return Date     : " + formatDate(returnDate));
        if (fineAmount > 0) {
            log("Fine Amount     : PKR " + fineAmount);
        }
        log("----------------------------------------");
    }

    private String formatFullName(Member member) {
        String first = member != null ? member.getFirstName() : null;
        String last = member != null ? member.getLastName() : null;
        String full = (safe(first) + " " + safe(last)).trim();
        return full.isEmpty() ? "N/A" : full;
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "N/A" : s.trim();
    }
}
