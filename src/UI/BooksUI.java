package UI;

import controllers.BookController;
import models.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BooksUI extends JFrame {

    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JTextField categoryField;
    private JTextField totalQtyField;
    private JTextField availableQtyField;
    private JTextField statusField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final BookController bookController;
    private int selectedBookId = -1;

    public BooksUI(String dbPath) {
        bookController = new BookController(dbPath);
        initializeUI();
        loadBooks();
    }

    // Backwards-compatible constructor
    public BooksUI() {
        this("./SLMS-DB.accdb");
    }

    private void initializeUI() {
        setTitle("Manage Books");
        setSize(1000, 400);
        setLocationRelativeTo(null);

        // ================= FORM =================
        JPanel formPanel = new JPanel(new GridLayout(2, 7, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Book Details"));

        titleField = new JTextField();
        authorField = new JTextField();
        isbnField = new JTextField();
        categoryField = new JTextField();
        totalQtyField = new JTextField();
        availableQtyField = new JTextField();
        statusField = new JTextField();

        formPanel.add(new JLabel("Title"));
        formPanel.add(new JLabel("Author"));
        formPanel.add(new JLabel("ISBN"));
        formPanel.add(new JLabel("Category"));
        formPanel.add(new JLabel("Total Qty"));
        formPanel.add(new JLabel("Available Qty"));
        formPanel.add(new JLabel("Status"));

        formPanel.add(titleField);
        formPanel.add(authorField);
        formPanel.add(isbnField);
        formPanel.add(categoryField);
        formPanel.add(totalQtyField);
        formPanel.add(availableQtyField);
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
                new String[]{
                        "ID", "Title", "Author", "ISBN",
                        "Category", "Total Qty", "Available Qty", "Status"
                }, 0
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
        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ================= LOGIC =================

    private void loadBooks() {
        tableModel.setRowCount(0);
        List<Book> books = bookController.getAllBooks();

        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getISBN(),
                    b.getCategory(),
                    b.getTotalQuantity(),
                    b.getAvailableQuantity(),
                    b.getStatus()
            });
        }
    }

    private void addBook() {
        Book b = buildBookFromForm();

        if (bookController.createBook(b)) {
            loadBooks();
            clearForm();
        } else {
            showError("Failed to add book");
        }
    }

    private void updateBook() {
        if (selectedBookId == -1) {
            showError("Select a book first");
            return;
        }

        Book b = buildBookFromForm();
        b.setBookId(selectedBookId);

        if (bookController.updateBook(b)) {
            loadBooks();
            clearForm();
        } else {
            showError("Failed to update book");
        }
    }

    private void deleteBook() {
        if (selectedBookId == -1) {
            showError("Select a book first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected book?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            bookController.deleteBook(selectedBookId);
            loadBooks();
            clearForm();
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedBookId = (int) tableModel.getValueAt(row, 0);
        titleField.setText(tableModel.getValueAt(row, 1).toString());
        authorField.setText(tableModel.getValueAt(row, 2).toString());
        isbnField.setText(tableModel.getValueAt(row, 3).toString());
        categoryField.setText(tableModel.getValueAt(row, 4).toString());
        totalQtyField.setText(tableModel.getValueAt(row, 5).toString());
        availableQtyField.setText(tableModel.getValueAt(row, 6).toString());
        statusField.setText(tableModel.getValueAt(row, 7).toString());
    }

    private Book buildBookFromForm() {
        Book b = new Book();
        b.setTitle(titleField.getText().trim());
        b.setAuthor(authorField.getText().trim());
        b.setISBN(isbnField.getText().trim());
        b.setCategory(categoryField.getText().trim());
        b.setTotalQuantity(Integer.parseInt(totalQtyField.getText().trim()));
        b.setAvailableQuantity(Integer.parseInt(availableQtyField.getText().trim()));
        b.setStatus(statusField.getText().trim());
        return b;
    }

    private void clearForm() {
        selectedBookId = -1;
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        categoryField.setText("");
        totalQtyField.setText("");
        availableQtyField.setText("");
        statusField.setText("");
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
