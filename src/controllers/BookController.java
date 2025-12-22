package controllers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Book;

public class BookController {

    private final String dbUrl;

    public BookController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }

    // ============================================================
    // CREATE BOOK
    // ============================================================
    public boolean createBook(Book b) {
        String idSql = "SELECT MAX(bookId) AS maxId FROM Books";
        String insertSql = "INSERT INTO Books (bookId, title, author, ISBN, category, totalQuantity, availableQuantity, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement idStmt = conn.prepareStatement(idSql);
             ResultSet rs = idStmt.executeQuery()) {

            int nextId = 1;
            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (!rs.wasNull()) {
                    nextId = maxId + 1;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, nextId);
                ps.setString(2, b.getTitle());
                ps.setString(3, b.getAuthor());
                ps.setString(4, b.getISBN());
                ps.setString(5, b.getCategory());
                ps.setInt(6, b.getTotalQuantity());
                ps.setInt(7, b.getAvailableQuantity());
                ps.setString(8, b.getStatus());

                return ps.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            System.out.println("Error creating book:");
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // UPDATE BOOK
    // ============================================================
    public boolean updateBook(Book b) {
        String sql = "UPDATE Books SET title=?, author=?, ISBN=?, category=?, totalQuantity=?, availableQuantity=?, status=? " +
                     "WHERE bookId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, b.getISBN());
            ps.setString(4, b.getCategory());
            ps.setInt(5, b.getTotalQuantity());
            ps.setInt(6, b.getAvailableQuantity());
            ps.setString(7, b.getStatus());
            ps.setInt(8, b.getBookId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error updating book:");
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // DELETE BOOK
    // ============================================================
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM Books WHERE bookId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error deleting book:");
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // GET BOOK BY ID
    // ============================================================
    public Book getBookById(int bookId) {
        String sql = "SELECT * FROM Books WHERE bookId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildBook(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving book:");
            e.printStackTrace();
        }

        return null;
    }

    // ============================================================
    // GET BOOK BY ISBN (for issue flow)
    // ============================================================
    public Book getBookByISBN(String isbn) {
        String sql = "SELECT * FROM Books WHERE ISBN=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildBook(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching book by ISBN:");
            e.printStackTrace();
        }

        return null;
    }

    // ============================================================
    // SEARCH BOOKS BY TITLE
    // ============================================================
    public List<Book> searchBooksByTitle(String title) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM Books WHERE title LIKE ?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + title + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(buildBook(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error searching books:");
            e.printStackTrace();
        }

        return list;
    }

    // ============================================================
    // SEARCH BOOKS BY TITLE / AUTHOR / ISBN
    // ============================================================
    public List<Book> searchBooks(String query) {
        List<Book> list = new ArrayList<>();

        if (query == null) {
            query = "";
        }

        String sql = "SELECT * FROM Books WHERE title LIKE ? OR author LIKE ? OR ISBN LIKE ? ORDER BY title ASC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + query + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildBook(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching books:");
            e.printStackTrace();
        }

        return list;
    }

    // ============================================================
    // GET ALL BOOKS
    // ============================================================
    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM Books ORDER BY title ASC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(buildBook(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading all books:");
            e.printStackTrace();
        }

        return list;
    }

    // ============================================================
    // HELPER: Convert ResultSet → Book object
    // ============================================================
    private Book buildBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("bookId"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setISBN(rs.getString("ISBN"));
        b.setCategory(rs.getString("category"));
        b.setTotalQuantity(rs.getInt("totalQuantity"));
        b.setAvailableQuantity(rs.getInt("availableQuantity"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}