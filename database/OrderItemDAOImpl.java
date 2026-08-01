import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderItemDAOImpl — JDBC read side for order line items.
 *
 * These are plain reads (no transaction needed), so each uses its own
 * connection via try-with-resources, exactly like ProductDAOImpl.
 */
public class OrderItemDAOImpl implements OrderItemDAO {

    @Override
    public List<OrderItem> getItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        // Join products for the display name. Use the snapshotted
        // price_at_purchase from order_items, NOT the live products.price.
        String sql =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price_at_purchase, " +
            "       p.name AS product_name " +
            "FROM order_items oi " +
            "JOIN products p ON p.id = oi.product_id " +
            "WHERE oi.order_id = ? " +
            "ORDER BY oi.id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToOrderItem(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching order items: " + e.getMessage());
        }
        return items;
    }

    /** Maps one row of a ResultSet to an OrderItem. */
    static OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPriceAtPurchase(rs.getBigDecimal("price_at_purchase"));
        item.setProductName(rs.getString("product_name"));
        return item;
    }
}
