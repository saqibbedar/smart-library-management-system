package UI;

import controllers.BookController;
import models.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.List;

public class BooksUI extends JFrame {

    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JTextField categoryField;
    private JTextField totalQtyField;
    private JTextField availableQtyField;
    private JTextField statusField;

    private JTextField searchField;

    private JTable table;
    private DefaultTableModel tableModel;

    private final BookController bookController;
    private int selectedBookId = -1;

    // --- Consistent Palette (from temp UI theme) ---
    private final Color ACCENT_BLUE = new Color(74, 144, 226);
    private final Color SIDEBAR_TOP = new Color(24, 28, 58);
    private final Color BG_SOFT = new Color(240, 242, 245);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color DANGER_RED = new Color(231, 76, 60);

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
        setTitle("SLMS | Books Management");
        setSize(1250, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_SOFT);
        setLayout(new BorderLayout());

        // ================= LEFT PANEL: BOOK ENTRY FORM =================
        JPanel leftPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 15, 10));
        leftPanel.setPreferredSize(new Dimension(350, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel titleLbl = new JLabel("Book Details");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(SIDEBAR_TOP);
        leftPanel.add(titleLbl);
        leftPanel.add(new JLabel(" "));

        titleField = createStyledField();
        authorField = createStyledField();
        isbnField = createStyledField();
        categoryField = createStyledField();
        totalQtyField = createStyledField();
        availableQtyField = createStyledField();
        statusField = createStyledField();

        leftPanel.add(new JLabel("Book Title"));
        leftPanel.add(titleField);
        leftPanel.add(new JLabel("Author"));
        leftPanel.add(authorField);
        leftPanel.add(new JLabel("ISBN"));
        leftPanel.add(isbnField);
        leftPanel.add(new JLabel("Category"));
        leftPanel.add(categoryField);

        JPanel qtyPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        qtyPanel.setOpaque(false);
        JPanel tq = new JPanel(new BorderLayout());
        tq.setOpaque(false);
        tq.add(new JLabel("Total Qty"), BorderLayout.NORTH);
        tq.add(totalQtyField, BorderLayout.CENTER);
        JPanel aq = new JPanel(new BorderLayout());
        aq.setOpaque(false);
        aq.add(new JLabel("Available"), BorderLayout.NORTH);
        aq.add(availableQtyField, BorderLayout.CENTER);
        qtyPanel.add(tq);
        qtyPanel.add(aq);
        leftPanel.add(qtyPanel);

        leftPanel.add(new JLabel("Status"));
        leftPanel.add(statusField);

        JButton addBtn = createBtn("Add", SUCCESS_GREEN);
        JButton updateBtn = createBtn("Update", ACCENT_BLUE);
        JButton deleteBtn = createBtn("Delete", DANGER_RED);
        JButton clearBtn = createBtn("Clear", new Color(149, 165, 166));

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnGrid.add(addBtn);
        btnGrid.add(updateBtn);
        btnGrid.add(deleteBtn);
        btnGrid.add(clearBtn);
        leftPanel.add(btnGrid);

        add(leftPanel, BorderLayout.WEST);

        // ================= RIGHT PANEL: SEARCH + TABLE =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(SIDEBAR_TOP);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 210, 210), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));

        JPanel searchBtns = new JPanel(new GridLayout(1, 2, 10, 0));
        searchBtns.setOpaque(false);
        JButton searchBtn = createBtn("Search", ACCENT_BLUE);
        JButton showAllBtn = createBtn("Show All", new Color(127, 140, 141));
        searchBtns.add(searchBtn);
        searchBtns.add(showAllBtn);

        JPanel searchLeft = new JPanel(new BorderLayout(8, 0));
        searchLeft.setOpaque(false);
        searchLeft.add(searchLabel, BorderLayout.WEST);
        searchLeft.add(searchField, BorderLayout.CENTER);

        searchPanel.add(searchLeft, BorderLayout.CENTER);
        searchPanel.add(searchBtns, BorderLayout.EAST);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{
                "ID", "Title", "Author", "ISBN",
                "Category", "Total Qty", "Available Qty", "Status"
            }, 0
        );
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // ================= EVENTS =================
        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn.addActionListener(e -> clearForm());

        searchBtn.addActionListener(e -> searchBooks());
        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            selectedBookId = -1;
            table.clearSelection();
            loadBooks();
        });

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ================= UI HELPERS (STYLE ONLY) =================
    private JTextField createStyledField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 35));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(232, 241, 252));
        t.setShowVerticalLines(false);
        JTableHeader h = t.getTableHeader();
        h.setBackground(SIDEBAR_TOP);
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 45));
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

    private void searchBooks() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            selectedBookId = -1;
            table.clearSelection();
            loadBooks();
            return;
        }

        tableModel.setRowCount(0);
        selectedBookId = -1;
        table.clearSelection();

        List<Book> books = bookController.searchBooks(query);
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
