package controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Users;

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

    // -----------------------------
    // LIST USERS BY ROLE
    // -----------------------------
    public List<Users> getUsersByRole(String role) {
        List<Users> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role=? ORDER BY username ASC";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Users user = new Users();
                user.setUserId(rs.getInt("userId"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("passwordHash"));
                user.setFullName(rs.getString("fullName"));
                user.setRole(rs.getString("role"));
                user.setShift(rs.getString("shift"));
                user.setActive(rs.getBoolean("isActive"));
                list.add(user);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving users by role:");
            e.printStackTrace();
        }

        return list;
    }

    // -----------------------------
    // RESET PASSWORD (STORES HASH)
    // -----------------------------
    public boolean resetPassword(int userId, String newPasswordPlain) {
        String sql = "UPDATE Users SET passwordHash=? WHERE userId=?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashPassword(newPasswordPlain));
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error resetting password:");
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------
    // PASSWORD HASHING (SHA-256)
    // -----------------------------
    public static String hashPassword(String passwordPlain) {
        if (passwordPlain == null) passwordPlain = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(passwordPlain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Should never happen on standard JVMs
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}