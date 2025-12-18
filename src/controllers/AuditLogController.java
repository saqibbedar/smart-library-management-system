package controllers;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.AuditLog;

public class AuditLogController {

    private final String dbUrl;

    public AuditLogController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }


    // =========================================================================
    // INSERT A LOG ENTRY
    // =========================================================================
    public boolean logAction(int userId, String action, String targetType) {
        String idSql = "SELECT MAX(logId) AS maxId FROM AuditLog";
        String insertSql = "INSERT INTO AuditLog (logId, userId, action, targetType, logTime) VALUES (?, ?, ?, ?, ?)";

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
                java.sql.Timestamp now = new java.sql.Timestamp(new Date().getTime());

                ps.setInt(1, nextId);
                ps.setInt(2, userId);
                ps.setString(3, action);
                ps.setString(4, targetType);
                ps.setTimestamp(5, now);

                return ps.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            System.out.println("Error logging action:");
            e.printStackTrace();
            return false;
        }
    }


    // =========================================================================
    // GET ALL LOGS
    // =========================================================================
    public List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog ORDER BY logTime DESC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(buildLog(rs));

        } catch (SQLException e) {
            System.out.println("Error fetching logs:");
            e.printStackTrace();
        }

        return list;
    }


    // =========================================================================
    // GET LOGS FOR A SPECIFIC USER
    // =========================================================================
    public List<AuditLog> getLogsByUser(int userId) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog WHERE userId=? ORDER BY logTime DESC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(buildLog(rs));

        } catch (SQLException e) {
            System.out.println("Error fetching logs for user:");
            e.printStackTrace();
        }

        return list;
    }


    // =========================================================================
    // GET LOGS BY ACTION TYPE (login, logout, issue, return, fine, etc.)
    // =========================================================================
    public List<AuditLog> getLogsByAction(String actionType) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog WHERE action=? ORDER BY logTime DESC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, actionType);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(buildLog(rs));

        } catch (SQLException e) {
            System.out.println("Error filtering logs by action:");
            e.printStackTrace();
        }

        return list;
    }


    // =========================================================================
    // GET LOGS IN A DATE RANGE (for reporting)
    // =========================================================================
    public List<AuditLog> getLogsBetween(Date start, Date end) {
        List<AuditLog> list = new ArrayList<>();

        String sql = "SELECT * FROM AuditLog WHERE logTime BETWEEN ? AND ? ORDER BY logTime DESC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(end.getTime()));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(buildLog(rs));

        } catch (SQLException e) {
            System.out.println("Error filtering logs by date:");
            e.printStackTrace();
        }

        return list;
    }


    // =========================================================================
    // BUILD LOG OBJECT FROM RESULTSET
    // =========================================================================
    private AuditLog buildLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("logId"));
        log.setUserId(rs.getInt("userId"));
        log.setAction(rs.getString("action"));
        log.setTargetType(rs.getString("targetType"));
        log.setLogTime(rs.getTimestamp("logTime"));
        return log;
    }
}