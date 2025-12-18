package controllers;

import models.Fine;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FineController {

    private final String dbUrl;

    public FineController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }


    // =========================================================================
    // CALCULATE OVERDUE DAYS + FINE AMOUNT (DAILY RATE)
    // =========================================================================
    public Fine calculateFine(int issueId, double dailyRate) {

        String sql = "SELECT issueDate, dueDate, returnDate, memberId FROM IssueTransactions WHERE issueId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            Date dueDate = rs.getDate("dueDate");
            Date returnDate = rs.getDate("returnDate");
            int memberId = rs.getInt("memberId");

            // If not returned yet → cannot calculate fine
            if (returnDate == null) return null;

            long diffMs = returnDate.getTime() - dueDate.getTime();
            long daysLate = diffMs / (1000 * 60 * 60 * 24);

            if (daysLate < 0) daysLate = 0;

            double fineAmount = daysLate * dailyRate;

            Fine f = new Fine();
            f.setIssueId(issueId);
            f.setMemberId(memberId);
            f.setDailyRate(dailyRate);
            f.setOverdueDays((int) daysLate);
            f.setAmount(fineAmount);
            f.setStatus("UNPAID");

            return f;

        } catch (SQLException e) {
            System.out.println("Error calculating fine:");
            e.printStackTrace();
            return null;
        }
    }



    // =========================================================================
    // INSERT FINE INTO DATABASE
    // =========================================================================
    public boolean createFine(Fine f) {
        String sql = "INSERT INTO Fines (issueId, memberId, amount, dailyRate, overdueDays, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, f.getIssueId());
            ps.setInt(2, f.getMemberId());
            ps.setDouble(3, f.getAmount());
            ps.setDouble(4, f.getDailyRate());
            ps.setInt(5, f.getOverdueDays());
            ps.setString(6, f.getStatus());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error creating fine:");
            e.printStackTrace();
            return false;
        }
    }



    // =========================================================================
    // GET FINE BY ISSUE ID
    // =========================================================================
    public Fine getFineByIssue(int issueId) {

        String sql = "SELECT * FROM Fines WHERE issueId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildFine(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching fine:");
            e.printStackTrace();
        }

        return null;
    }



    // =========================================================================
    // GET ALL FINES FOR A MEMBER
    // =========================================================================
    public List<Fine> getFinesForMember(int memberId) {
        List<Fine> list = new ArrayList<>();

        String sql = "SELECT * FROM Fines WHERE memberId=? ORDER BY status ASC, fineId DESC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(buildFine(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching member fines:");
            e.printStackTrace();
        }

        return list;
    }



    // =========================================================================
    // MARK FINE AS PAID
    // =========================================================================
    public boolean markFineAsPaid(int fineId) {

        String sql = "UPDATE Fines SET status='PAID' WHERE fineId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fineId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error marking fine as paid:");
            e.printStackTrace();
            return false;
        }
    }



    // =========================================================================
    // WAIVE FINE
    // =========================================================================
    public boolean waiveFine(int fineId) {

        String sql = "UPDATE Fines SET status='WAIVED' WHERE fineId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fineId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error waiving fine:");
            e.printStackTrace();
            return false;
        }
    }



    // =========================================================================
    // BUILD FINE OBJECT FROM RESULTSET
    // =========================================================================
    private Fine buildFine(ResultSet rs) throws SQLException {
        Fine f = new Fine();
        f.setFineId(rs.getInt("fineId"));
        f.setIssueId(rs.getInt("issueId"));
        f.setMemberId(rs.getInt("memberId"));
        f.setAmount(rs.getDouble("amount"));
        f.setDailyRate(rs.getDouble("dailyRate"));
        f.setOverdueDays(rs.getInt("overdueDays"));
        f.setStatus(rs.getString("status"));
        return f;
    }
}