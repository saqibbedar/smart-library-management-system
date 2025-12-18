package UI;

import controllers.*;
import models.*;

import javax.swing.*;
import java.awt.*;

public class IssueReturnUI extends JFrame {

    private JTextField studentIdField;
    private JTextField barcodeField;
    private JTextArea outputArea;

    private final MemberController memberController;
    private final BookCopyController copyController;
    private final IssueController issueController;
    private final FineController fineController;

    public IssueReturnUI(String dbPath) {
        memberController = new MemberController(dbPath);
        copyController = new BookCopyController(dbPath);
        issueController = new IssueController(dbPath);
        fineController = new FineController(dbPath);

        initializeUI();
    }

    // Backwards-compatible constructor
    public IssueReturnUI() {
        this("./SLMS-DB.accdb");
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
        } else {
            log("Failed to issue book");
        }
    }

    private void returnBook() {
        outputArea.setText("");

        BookCopy copy = copyController.getCopyByBarcode(barcodeField.getText().trim());
        if (copy == null) {
            log("Book copy not found");
            return;
        }

        IssueTransaction issue = issueController.getIssueById(
                getActiveIssueIdForCopy(copy.getCopyId())
        );

        if (issue == null) {
            log("No active issue found for this copy");
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
        } else {
            log("Book returned successfully. No fine.");
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
}
