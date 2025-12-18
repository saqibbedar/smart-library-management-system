package controllers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.BookCopy;

public class BookCopyController {

    private final String dbUrl;

    public BookCopyController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }


    // =========================================================================
    // CREATE A NEW BOOK COPY
    // =========================================================================
    public boolean createCopy(BookCopy copy) {
        String idSql = "SELECT MAX(copyId) AS maxId FROM BookCopies";
        String insertSql = "INSERT INTO BookCopies (copyId, bookId, copyNumber, barcode, location, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";

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
                ps.setInt(2, copy.getBookId());
                ps.setInt(3, copy.getCopyNumber());
                ps.setString(4, copy.getBarcode());
                ps.setString(5, copy.getLocation());
                ps.setString(6, copy.getStatus());

                return ps.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            System.out.println("Error creating book copy:");
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================================
    // UPDATE BOOK COPY
    // =========================================================================
    public boolean updateCopy(BookCopy copy) {
        String sql = "UPDATE BookCopies SET bookId=?, copyNumber=?, barcode=?, location=?, status=? " +
                     "WHERE copyId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, copy.getBookId());
            ps.setInt(2, copy.getCopyNumber());
            ps.setString(3, copy.getBarcode());
            ps.setString(4, copy.getLocation());
            ps.setString(5, copy.getStatus());
            ps.setInt(6, copy.getCopyId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error updating book copy:");
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================================
    // DELETE BOOK COPY
    // =========================================================================
    public boolean deleteCopy(int copyId) {
        String sql = "DELETE FROM BookCopies WHERE copyId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, copyId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error deleting book copy:");
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================================
    // GET COPY BY ID
    // =========================================================================
    public BookCopy getCopyById(int copyId) {
        String sql = "SELECT * FROM BookCopies WHERE copyId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, copyId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return buildCopy(rs);

        } catch (SQLException e) {
            System.out.println("Error fetching book copy:");
            e.printStackTrace();
        }

        return null;
    }


    // =========================================================================
    // GET COPY BY BARCODE (important for issue flow)
    // =========================================================================
    public BookCopy getCopyByBarcode(String barcode) {
        String sql = "SELECT * FROM BookCopies WHERE barcode=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return buildCopy(rs);

        } catch (SQLException e) {
            System.out.println("Error fetching copy by barcode:");
            e.printStackTrace();
        }

        return null;
    }


    // =========================================================================
    // GET ALL COPIES OF A BOOK
    // =========================================================================
    public List<BookCopy> getCopiesOfBook(int bookId) {
        List<BookCopy> list = new ArrayList<>();
        String sql = "SELECT * FROM BookCopies WHERE bookId=? ORDER BY copyNumber ASC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(buildCopy(rs));

        } catch (SQLException e) {
            System.out.println("Error fetching book copies:");
            e.printStackTrace();
        }

        return list;
    }


    // =========================================================================
    // CHECK IF COPY IS AVAILABLE
    // =========================================================================
    public boolean isCopyAvailable(int copyId) {
        String sql = "SELECT status FROM BookCopies WHERE copyId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, copyId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return rs.getString("status").equalsIgnoreCase("AVAILABLE");

        } catch (SQLException e) {
            System.out.println("Error checking copy availability:");
            e.printStackTrace();
        }

        return false;
    }


    // =========================================================================
    // GET ONLY AVAILABLE COPIES OF A BOOK
    // =========================================================================
    public List<BookCopy> getAvailableCopies(int bookId) {
        List<BookCopy> list = new ArrayList<>();
        String sql = "SELECT * FROM BookCopies WHERE bookId=? AND status='AVAILABLE'";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(buildCopy(rs));

        } catch (SQLException e) {
            System.out.println("Error fetching available copies:");
            e.printStackTrace();
        }

        return list;
    }


    // =========================================================================
    // BUILD COPY OBJECT FROM RESULTSET
    // =========================================================================
    private BookCopy buildCopy(ResultSet rs) throws SQLException {
        BookCopy c = new BookCopy();
        c.setCopyId(rs.getInt("copyId"));
        c.setBookId(rs.getInt("bookId"));
        c.setCopyNumber(rs.getInt("copyNumber"));
        c.setBarcode(rs.getString("barcode"));
        c.setLocation(rs.getString("location"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}