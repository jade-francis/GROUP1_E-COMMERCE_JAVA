package com.shopease.db;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAOImpl — JDBC implementation of UserDAO with BCrypt password hashing.
 *
 * SECURITY NOTES:
 * - Passwords are NEVER stored in plain text. register() hashes the plain
 *   password with BCrypt before inserting; only the hash touches the database.
 * - login() fetches the stored hash and uses BCrypt.checkpw() to verify the
 *   plain password against it. The plain password never appears in a query.
 * - BCrypt is intentionally slow (adaptive hashing) to resist brute-force attacks.
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public User register(String name, String email, String plainPassword, String role) {
        // Hash the password with BCrypt (default work factor: 10 rounds)
        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?) RETURNING id, created_at";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.setString(4, role);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Build and return the newly created User
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(name);
                    user.setEmail(email);
                    user.setPasswordHash(passwordHash);
                    user.setRole(role);
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        user.setCreatedAt(ts.toLocalDateTime());
                    }
                    return user;
                }
            }

        } catch (SQLException e) {
            // Most common cause: email already exists (UNIQUE constraint violation)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // integrity constraint
                System.err.println("Registration failed: email already in use.");
            } else {
                System.err.println("Error registering user: " + e.getMessage());
            }
        }
        return null; // registration failed
    }

    @Override
    public User login(String email, String plainPassword) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    // Verify the plain password against the stored BCrypt hash
                    if (BCrypt.checkpw(plainPassword, storedHash)) {
                        return mapRowToUser(rs);
                    } else {
                        System.err.println("Login failed: incorrect password.");
                    }
                } else {
                    System.err.println("Login failed: no account found for " + email);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null; // login failed
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean updateRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating role: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps one row of a ResultSet to a User object.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}
