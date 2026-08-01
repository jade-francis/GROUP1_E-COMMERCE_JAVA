import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAOImpl — the actual JDBC implementation of ProductDAO.
 *
 * Every method follows the same pattern:
 *   1. Open a connection (try-with-resources auto-closes it)
 *   2. Use a PreparedStatement (never raw string-concat SQL — SQL injection risk)
 *   3. Execute the query
 *   4. Map ResultSet rows -> Product objects
 *   5. Return plain Java objects to the caller
 */
public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
        }
        return products;
    }

    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching product by id: " + e.getMessage());
        }
        return null; // caller should check for null (product not found)
    }

    @Override
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching products by category: " + e.getMessage());
        }
        return products;
    }

    @Override
    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (name, description, price, stock_qty, category_id, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName());
            stmt.setString(2, p.getDescription());
            stmt.setBigDecimal(3, p.getPrice());
            stmt.setInt(4, p.getStockQty());
            if (p.getCategoryId() != null) {
                stmt.setInt(5, p.getCategoryId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, p.getImageUrl());

            return stmt.executeUpdate() > 0; // true if a row was inserted

        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStock(int productId, int newQty) {
        String sql = "UPDATE products SET stock_qty = ? WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQty);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps one row of a ResultSet to a Product object.
     * Every DAO ends up needing a helper like this —
     * it's the "translation" step between SQL and Java.
     */
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));

        int catId = rs.getInt("category_id");
        p.setCategoryId(rs.wasNull() ? null : catId);

        p.setImageUrl(rs.getString("image_url"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
