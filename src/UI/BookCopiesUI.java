package UI;

import controllers.BookController;
import controllers.BookCopyController;
import models.Book;
import models.BookCopy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

    // --- Modern Palette (from temp UI theme) ---
    private final Color ACCENT_BLUE = new Color(74, 144, 226);
    private final Color SIDEBAR_TOP = new Color(24, 28, 58);
    private final Color BG_SOFT = new Color(240, 242, 245);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color DANGER_RED = new Color(231, 76, 60);

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
        setTitle("SLMS | Book Copies Management");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_SOFT);
        setLayout(new BorderLayout());

        // ================= LEFT PANEL: FORM =================
        JPanel leftPanel = new JPanel(new CopiesLayout(CopiesLayout.TOP, 15, 10));
        leftPanel.setPreferredSize(new Dimension(350, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel headerLbl = new JLabel("Copy Management");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLbl.setForeground(SIDEBAR_TOP);
        leftPanel.add(headerLbl);

        bookCombo = new JComboBox<>();
        styleComboBox(bookCombo);
        copyNumberField = createStyledTextField();
        barcodeField = createStyledTextField();
        locationField = createStyledTextField();
        statusField = createStyledTextField();

        leftPanel.add(new JLabel("Select Main Book"));
        leftPanel.add(bookCombo);
        leftPanel.add(new JLabel("Copy Number"));
        leftPanel.add(copyNumberField);
        leftPanel.add(new JLabel("Barcode ID"));
        leftPanel.add(barcodeField);
        leftPanel.add(new JLabel("Shelf Location"));
        leftPanel.add(locationField);
        leftPanel.add(new JLabel("Condition Status"));
        leftPanel.add(statusField);

        JButton addBtn = createActionBtn("Add", SUCCESS_GREEN);
        JButton updateBtn = createActionBtn("Update", ACCENT_BLUE);
        JButton deleteBtn = createActionBtn("Delete", DANGER_RED);
        JButton clearBtn = createActionBtn("Clear", new Color(149, 165, 166));

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.add(addBtn);
        btnGrid.add(updateBtn);
        btnGrid.add(deleteBtn);
        btnGrid.add(clearBtn);
        leftPanel.add(new JLabel(" "));
        leftPanel.add(btnGrid);

        add(leftPanel, BorderLayout.WEST);

        // ================= RIGHT PANEL: TABLE =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        tableModel = new DefaultTableModel(
            new String[]{"ID", "Copy No", "Barcode", "Location", "Status"}, 0
        );
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // ================= EVENTS =================
        addBtn.addActionListener(e -> addCopy());
        updateBtn.addActionListener(e -> updateCopy());
        deleteBtn.addActionListener(e -> deleteCopy());
        clearBtn.addActionListener(e -> clearForm());

        bookCombo.addActionListener(e -> loadCopiesForSelectedBook());
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ================= UI HELPERS (STYLE ONLY) =================
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(0, 35));
        combo.setBackground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        table.setShowVerticalLines(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(SIDEBAR_TOP);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 45));
    }


// ================= LAYOUT HELPER (FROM TEMP THEME) =================
class CopiesLayout extends FlowLayout {

    public static final int TOP = 0;

    public CopiesLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension tarsiz = new Dimension(0, 0);
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    tarsiz.width = Math.max(tarsiz.width, d.width);
                    if (i > 0) {
                        tarsiz.height += getVgap();
                    }
                    tarsiz.height += d.height;
                }
            }
            tarsiz.width += getHgap() * 2;
            tarsiz.height += getVgap() * 2;
            return tarsiz;
        }
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            int x = getHgap(), y = getVgap();
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    m.setBounds(x, y, target.getWidth() - 2 * x, d.height);
                    y += d.height + getVgap();
                }
            }
        }
    }
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
