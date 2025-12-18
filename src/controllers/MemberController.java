package controllers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Member;

public class MemberController {

    private final String dbUrl;

    public MemberController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }


    // ============================================================
    // CREATE MEMBER
    // ============================================================
    public boolean createMember(Member m) {
        String idSql = "SELECT MAX(memberId) AS maxId FROM Members";
        String insertSql = "INSERT INTO Members (memberId, studentId, firstName, lastName, email, department, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

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
                ps.setString(2, m.getStudentId());
                ps.setString(3, m.getFirstName());
                ps.setString(4, m.getLastName());
                ps.setString(5, m.getEmail());
                ps.setString(6, m.getDepartment());
                ps.setString(7, m.getStatus());

                return ps.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            System.out.println("Error creating member:");
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // UPDATE MEMBER
    // ============================================================
    public boolean updateMember(Member m) {
        String sql = "UPDATE Members SET studentId=?, firstName=?, lastName=?, email=?, department=?, status=? " +
                     "WHERE memberId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getStudentId());
            ps.setString(2, m.getFirstName());
            ps.setString(3, m.getLastName());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getDepartment());
            ps.setString(6, m.getStatus());
            ps.setInt(7, m.getMemberId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error updating member:");
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // DELETE MEMBER
    // ============================================================
    public boolean deleteMember(int memberId) {
        String sql = "DELETE FROM Members WHERE memberId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error deleting member:");
            e.printStackTrace();
            return false;
        }
    }


    // ============================================================
    // GET MEMBER BY ID
    // ============================================================
    public Member getMemberById(int memberId) {
        String sql = "SELECT * FROM Members WHERE memberId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildMember(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching member:");
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // GET MEMBER BY StudentID (for issue/return)
    // ============================================================
    public Member getMemberByStudentId(String studentId) {
        String sql = "SELECT * FROM Members WHERE studentId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildMember(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ============================================================
    // GET ALL MEMBERS
    // ============================================================
    public List<Member> getAllMembers() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM Members ORDER BY firstName ASC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(buildMember(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching all members:");
            e.printStackTrace();
        }

        return list;
    }


    // ============================================================
    // HELPER: BUILD MEMBER FROM ResultSet
    // ============================================================
    private Member buildMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberId(rs.getInt("memberId"));
        m.setStudentId(rs.getString("studentId"));
        m.setFirstName(rs.getString("firstName"));
        m.setLastName(rs.getString("lastName"));
        m.setEmail(rs.getString("email"));
        m.setDepartment(rs.getString("department"));
        m.setStatus(rs.getString("status"));
        return m;
    }
}