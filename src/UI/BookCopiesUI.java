package UI;

import controllers.BookController;
import controllers.BookCopyController;
import models.Book;
import models.BookCopy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookCopiesUI extends JFrame {

    private JComboBox<Book> bookCombo;
    private JTextField copyNumberField;
    private JTextField barcodeField;
    private JTextField locationField;
    private JTextField statusField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final BookController bookController;
    private final BookCopyController copyController;

    private int selectedCopyId = -1;

    public BookCopiesUI(String dbPath) {
        bookController = new BookController(dbPath);
        copyController = new BookCopyController(dbPath);

        initializeUI();
        loadBooks();
    }

    // Backwards-compatible constructor
    public BookCopiesUI() {
        this("./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("Book Copies");
        setSize(900, 400);
        setLocationRelativeTo(null);

        // ================= FORM =================
        JPanel formPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Copy Details"));

        bookCombo = new JComboBox<>();
        copyNumberField = new JTextField();
        barcodeField = new JTextField();
        locationField = new JTextField();
        statusField = new JTextField();

        formPanel.add(new JLabel("Book"));
        formPanel.add(new JLabel("Copy No"));
        formPanel.add(new JLabel("Barcode"));
        formPanel.add(new JLabel("Location"));
        formPanel.add(new JLabel("Status"));

        formPanel.add(bookCombo);
        formPanel.add(copyNumberField);
        formPanel.add(barcodeField);
        formPanel.add(locationField);
        formPanel.add(statusField);

        // ================= BUTTONS =================
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
                new String[]{"ID", "Copy No", "Barcode", "Location", "Status"}, 0
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
        addBtn.addActionListener(e -> addCopy());
        updateBtn.addActionListener(e -> updateCopy());
        deleteBtn.addActionListener(e -> deleteCopy());
        clearBtn.addActionListener(e -> clearForm());

        bookCombo.addActionListener(e -> loadCopiesForSelectedBook());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ================= LOGIC =================

    private void loadBooks() {
        bookCombo.removeAllItems();
        List<Book> books = bookController.getAllBooks();

        for (Book b : books) {
            bookCombo.addItem(b);
        }

        if (!books.isEmpty()) {
            bookCombo.setSelectedIndex(0);
            loadCopiesForSelectedBook();
        }
    }

    private void loadCopiesForSelectedBook() {
        tableModel.setRowCount(0);

        Book selectedBook = (Book) bookCombo.getSelectedItem();
        if (selectedBook == null) return;

        List<BookCopy> copies = copyController.getCopiesOfBook(selectedBook.getBookId());

        for (BookCopy c : copies) {
            tableModel.addRow(new Object[]{
                    c.getCopyId(),
                    c.getCopyNumber(),
                    c.getBarcode(),
                    c.getLocation(),
                    c.getStatus()
            });
        }
    }

    private void addCopy() {
        Book book = (Book) bookCombo.getSelectedItem();
        if (book == null) return;

        BookCopy c = buildCopyFromForm();
        c.setBookId(book.getBookId());

        if (copyController.createCopy(c)) {
            loadCopiesForSelectedBook();
            clearForm();
        } else {
            showError("Failed to add copy");
        }
    }

    private void updateCopy() {
        if (selectedCopyId == -1) {
            showError("Select a copy first");
            return;
        }

        Book book = (Book) bookCombo.getSelectedItem();
        BookCopy c = buildCopyFromForm();
        c.setCopyId(selectedCopyId);
        c.setBookId(book.getBookId());

        if (copyController.updateCopy(c)) {
            loadCopiesForSelectedBook();
            clearForm();
        } else {
            showError("Failed to update copy");
        }
    }

    private void deleteCopy() {
        if (selectedCopyId == -1) {
            showError("Select a copy first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected copy?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            copyController.deleteCopy(selectedCopyId);
            loadCopiesForSelectedBook();
            clearForm();
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedCopyId = (int) tableModel.getValueAt(row, 0);
        copyNumberField.setText(tableModel.getValueAt(row, 1).toString());
        barcodeField.setText(tableModel.getValueAt(row, 2).toString());
        locationField.setText(tableModel.getValueAt(row, 3).toString());
        statusField.setText(tableModel.getValueAt(row, 4).toString());
    }

    private BookCopy buildCopyFromForm() {
        BookCopy c = new BookCopy();
        c.setCopyNumber(Integer.parseInt(copyNumberField.getText().trim()));
        c.setBarcode(barcodeField.getText().trim());
        c.setLocation(locationField.getText().trim());
        c.setStatus(statusField.getText().trim());
        return c;
    }

    private void clearForm() {
        selectedCopyId = -1;
        copyNumberField.setText("");
        barcodeField.setText("");
        locationField.setText("");
        statusField.setText("");
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
