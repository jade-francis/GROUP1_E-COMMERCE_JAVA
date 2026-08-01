import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CartDAOImpl — JDBC implementation of CartDAO.
 *
 * Key detail: addToCart uses PostgreSQL's "INSERT ... ON CONFLICT" (upsert).
 * Because cart_items has a UNIQUE(user_id, product_id) constraint, adding a
 * product that's already in the cart bumps its quantity instead of erroring
 * or creating a duplicate row.
 */
public class CartDAOImpl implements CartDAO {

    @Override
    public boolean addToCart(int userId, int productId, int quantity) {
        String sql =
            "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) " +
            "ON CONFLICT (user_id, product_id) " +
            "DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<CartItem> getCart(int userId) {
        List<CartItem> items = new ArrayList<>();
        // Join products so the cart carries name + price for display
        String sql =
            "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, ci.added_at, " +
            "       p.name AS product_name, p.price " +
            "FROM cart_items ci " +
            "JOIN products p ON p.id = ci.product_id " +
            "WHERE ci.user_id = ? " +
            "ORDER BY ci.id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getInt("id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    Timestamp ts = rs.getTimestamp("added_at");
                    if (ts != null) item.setAddedAt(ts.toLocalDateTime());
                    item.setProductName(rs.getString("product_name"));
                    item.setPrice(rs.getBigDecimal("price"));
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching cart: " + e.getMessage());
        }
        return items;
    }

    @Override
    public boolean updateQuantity(int userId, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            // A quantity of 0 or less means "remove it" (the CHECK constraint
            // would reject <= 0 anyway).
            return removeFromCart(userId, productId);
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating cart quantity: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeFromCart(int userId, int productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error removing from cart: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true; // clearing an already-empty cart is still "success"

        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            return false;
        }
    }

    @Override
    public BigDecimal getCartTotal(int userId) {
        String sql =
            "SELECT COALESCE(SUM(p.price * ci.quantity), 0) AS total " +
            "FROM cart_items ci JOIN products p ON p.id = ci.product_id " +
            "WHERE ci.user_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error computing cart total: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
