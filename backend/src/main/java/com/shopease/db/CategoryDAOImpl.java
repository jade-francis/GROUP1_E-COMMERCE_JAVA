package com.shopease.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAOImpl — JDBC implementation of CategoryDAO.
 * Same pattern as ProductDAOImpl: PreparedStatements everywhere,
 * try-with-resources for auto-closing, ResultSet -> POJO mapping.
 */
public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all categories: " + e.getMessage());
        }
        return categories;
    }

    @Override
    public Category getCategoryById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCategory(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching category by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Category getCategoryByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCategory(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching category by name: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Category addCategory(String name, String description) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?) RETURNING id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("id"), name, description);
                }
            }

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                System.err.println("Add category failed: name '" + name + "' already exists.");
            } else {
                System.err.println("Error adding category: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean updateCategory(int id, String name, String description) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteCategory(int id) {
        // Note: products.category_id has ON DELETE SET NULL, so deleting a
        // category leaves its products intact but uncategorized.
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    private Category mapRowToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}
