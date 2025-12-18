package controllers;

import models.Users;
import java.sql.*;

public class UserController {

    private final String dbUrl;

    public UserController(String dbPath) {
        this.dbUrl = "jdbc:ucanaccess://" + dbPath;
    }

    // -----------------------------
    // LOGIN USER
    // -----------------------------
    public Users login(String username, String passwordHash) {
        String sql = "SELECT * FROM Users WHERE username=? AND passwordHash=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Users user = new Users();
                user.setUserId(rs.getInt("userId"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("passwordHash"));
                user.setFullName(rs.getString("fullName"));
                user.setRole(rs.getString("role"));
                user.setShift(rs.getString("shift"));
                user.setActive(rs.getBoolean("isActive"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // login failed
    }

    // -----------------------------
    // CREATE USER
    // -----------------------------
    public boolean createUser(Users user) {
        String sql = "INSERT INTO Users (username, passwordHash, fullName, role, shift, isActive) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getShift());
            ps.setBoolean(6, user.isActive());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error creating user:");
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------
    // UPDATE USER
    // -----------------------------
    public boolean updateUser(Users user) {
        String sql = "UPDATE Users SET username=?, passwordHash=?, fullName=?, role=?, shift=?, isActive=? WHERE userId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getShift());
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, user.getUserId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error updating user:");
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------
    // DELETE USER
    // -----------------------------
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE userId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error deleting user:");
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------
    // GET USER BY ID
    // -----------------------------
    public Users getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE userId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Users user = new Users();
                user.setUserId(rs.getInt("userId"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("passwordHash"));
                user.setFullName(rs.getString("fullName"));
                user.setRole(rs.getString("role"));
                user.setShift(rs.getString("shift"));
                user.setActive(rs.getBoolean("isActive"));
                return user;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user:");
            e.printStackTrace();
        }

        return null;
    }
}