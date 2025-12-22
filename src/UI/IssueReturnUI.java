package UI;

import controllers.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
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
        setTitle("Issue / Return Books");
        setSize(500, 350);
        setLocationRelativeTo(null);

        // ================= INPUT PANEL =================
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Details"));

        studentIdField = new JTextField();
        barcodeField = new JTextField();

        inputPanel.add(new JLabel("Student ID"));
        inputPanel.add(studentIdField);
        inputPanel.add(new JLabel("Book Copy Barcode"));
        inputPanel.add(barcodeField);

        // ================= BUTTONS =================
        JPanel buttonPanel = new JPanel();

        JButton issueBtn = new JButton("Issue Book");
        JButton returnBtn = new JButton("Return Book");

        buttonPanel.add(issueBtn);
        buttonPanel.add(returnBtn);

        // ================= OUTPUT =================
        outputArea = new JTextArea(6, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // ================= LAYOUT =================
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // ================= EVENTS =================
        issueBtn.addActionListener(e -> issueBook());
        returnBtn.addActionListener(e -> returnBook());
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
