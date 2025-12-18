package controllers;

import models.IssueTransaction;

import java.sql.*;
import java.util.Date;

public class IssueController {

    private final String dbUrl;

    public IssueController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }

    // =========================================================================
    // ISSUE A BOOK COPY TO A MEMBER
    // =========================================================================
    public boolean issueBook(int memberId, int copyId, int bookId) {

        String insertIssueSQL =
                "INSERT INTO IssueTransactions (memberId, copyId, issueDate, dueDate, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        String updateCopySQL =
                "UPDATE BookCopies SET status='ISSUED' WHERE copyId=?";

        String updateBookQtySQL =
                "UPDATE Books SET availableQuantity = availableQuantity - 1 WHERE bookId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl)) {

            conn.setAutoCommit(false); // Transaction

            // 1) Insert Issue Record
            try (PreparedStatement ps = conn.prepareStatement(insertIssueSQL)) {
                Date today = new Date();
                java.sql.Date sqlIssue = new java.sql.Date(today.getTime());

                // 15 days due date
                java.sql.Date sqlDue =
                        new java.sql.Date(sqlIssue.toLocalDate().plusDays(15).toEpochDay() * 86400000);

                ps.setInt(1, memberId);
                ps.setInt(2, copyId);
                ps.setDate(3, sqlIssue);
                ps.setDate(4, sqlDue);
                ps.setString(5, "ISSUED");

                ps.executeUpdate();
            }

            // 2) Mark copy as issued
            try (PreparedStatement ps = conn.prepareStatement(updateCopySQL)) {
                ps.setInt(1, copyId);
                ps.executeUpdate();
            }

            // 3) Reduce available quantity
            try (PreparedStatement ps = conn.prepareStatement(updateBookQtySQL)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Error issuing book:");
            e.printStackTrace();
            return false;
        }
    }




    // =========================================================================
    // RETURN BOOK COPY
    // =========================================================================
    public boolean returnBook(int issueId, int copyId, int bookId) {

        String updateIssueSQL =
                "UPDATE IssueTransactions SET returnDate=?, status='RETURNED' WHERE issueId=?";

        String updateCopySQL =
                "UPDATE BookCopies SET status='AVAILABLE' WHERE copyId=?";

        String updateBookQtySQL =
                "UPDATE Books SET availableQuantity = availableQuantity + 1 WHERE bookId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl)) {

            conn.setAutoCommit(false);

            // 1) Update Transaction (add returnDate)
            try (PreparedStatement ps = conn.prepareStatement(updateIssueSQL)) {
                Date today = new Date();
                java.sql.Date sqlReturn = new java.sql.Date(today.getTime());

                ps.setDate(1, sqlReturn);
                ps.setInt(2, issueId);

                ps.executeUpdate();
            }

            // 2) Mark copy as available
            try (PreparedStatement ps = conn.prepareStatement(updateCopySQL)) {
                ps.setInt(1, copyId);
                ps.executeUpdate();
            }

            // 3) Increase available quantity
            try (PreparedStatement ps = conn.prepareStatement(updateBookQtySQL)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Error returning book:");
            e.printStackTrace();
            return false;
        }
    }




    // =========================================================================
    // GET ISSUE TRANSACTION BY ID
    // =========================================================================
    public IssueTransaction getIssueById(int issueId) {
        String sql = "SELECT * FROM IssueTransactions WHERE issueId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                IssueTransaction it = new IssueTransaction();
                it.setIssueId(rs.getInt("issueId"));
                it.setMemberId(rs.getInt("memberId"));
                it.setCopyId(rs.getInt("copyId"));
                it.setIssueDate(rs.getDate("issueDate"));
                it.setDueDate(rs.getDate("dueDate"));
                it.setReturnDate(rs.getDate("returnDate"));
                it.setStatus(rs.getString("status"));

                return it;
            }

        } catch (SQLException e) {
            System.out.println("Error fetching issue record:");
            e.printStackTrace();
        }

        return null;
    }



    // =========================================================================
    // GET ACTIVE (NOT YET RETURNED) ISSUE FOR A GIVEN COPY
    // =========================================================================
    public IssueTransaction getActiveIssueByCopyId(int copyId) {
        String sql = "SELECT TOP 1 * FROM IssueTransactions WHERE copyId=? AND status='ISSUED' ORDER BY issueDate DESC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, copyId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                IssueTransaction it = new IssueTransaction();
                it.setIssueId(rs.getInt("issueId"));
                it.setMemberId(rs.getInt("memberId"));
                it.setCopyId(rs.getInt("copyId"));
                it.setIssueDate(rs.getDate("issueDate"));
                it.setDueDate(rs.getDate("dueDate"));
                it.setReturnDate(rs.getDate("returnDate"));
                it.setStatus(rs.getString("status"));
                return it;
            }

        } catch (SQLException e) {
            System.out.println("Error fetching active issue by copyId:");
            e.printStackTrace();
        }

        return null;
    }




    // =========================================================================
    // CHECK IF A COPY IS AVAILABLE TO ISSUE
    // =========================================================================
    public boolean isCopyAvailable(int copyId) {
        String sql = "SELECT status FROM BookCopies WHERE copyId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, copyId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("status").equalsIgnoreCase("AVAILABLE");
            }

        } catch (SQLException e) {
            System.out.println("Error checking copy availability:");
            e.printStackTrace();
        }

        return false;
    }




    // =========================================================================
    // CHECK IF MEMBER ALREADY HAS AN ACTIVE ISSUE
    // =========================================================================
    public boolean memberHasIssuedBook(int memberId) {
        String sql = "SELECT COUNT(*) AS cnt FROM IssueTransactions " +
                     "WHERE memberId=? AND status='ISSUED'";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error checking active issues:");
            e.printStackTrace();
        }

        return false;
    }
}