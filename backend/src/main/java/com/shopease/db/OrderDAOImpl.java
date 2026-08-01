package com.shopease.db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAOImpl — JDBC implementation of OrderDAO.
 *
 * placeOrder is the reason this class exists and the first place in the
 * project that uses an explicit TRANSACTION. Turning a cart into an order
 * touches four tables (orders, order_items, products, cart_items) and every
 * one of those writes must succeed together or not at all — otherwise you'd
 * get corrupt states like "stock was decremented but no order exists" or
 * "order placed but cart never cleared."
 *
 * The transaction recipe:
 *   conn.setAutoCommit(false);   // stop each statement auto-committing
 *   ... do all the work on THIS one connection ...
 *   conn.commit();               // make it all permanent at once
 *   // on any failure -> conn.rollback(); (undo everything)
 *   // finally -> conn.setAutoCommit(true); conn.close();
 *
 * Everything inside placeOrder deliberately uses the SAME connection so the
 * reads and writes share one transaction. We do NOT call CartDAO here,
 * because that would open a separate connection outside this transaction.
 */
public class OrderDAOImpl implements OrderDAO {

    private final OrderItemDAO orderItemDAO = new OrderItemDAOImpl();

    @Override
    public Order placeOrder(int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // ---- begin transaction ----

            // 1. Read the cart, locking the referenced product rows (FOR UPDATE)
            //    so a concurrent checkout can't oversell the same stock.
            List<CartItem> cart = readCartForUpdate(conn, userId);
            if (cart.isEmpty()) {
                System.err.println("placeOrder: cart is empty for user " + userId);
                conn.rollback();
                return null;
            }

            // 2. Verify stock for every line and compute the total up front.
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem line : cart) {
                int available = getStock(conn, line.getProductId());
                if (available < line.getQuantity()) {
                    System.err.println("placeOrder: insufficient stock for product " +
                        line.getProductId() + " (need " + line.getQuantity() +
                        ", have " + available + ")");
                    conn.rollback();
                    return null;
                }
                total = total.add(line.subtotal());
            }

            // 3. Create the orders row and grab its generated id.
            int orderId = insertOrder(conn, userId, total);

            // 4. Insert one order_items row per cart line (price snapshotted),
            //    and 5. decrement the product's stock.
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem line : cart) {
                insertOrderItem(conn, orderId, line);
                decrementStock(conn, line.getProductId(), line.getQuantity());

                OrderItem oi = new OrderItem(line.getProductId(), line.getQuantity(), line.getPrice());
                oi.setOrderId(orderId);
                oi.setProductName(line.getProductName());
                orderItems.add(oi);
            }

            // 6. Clear the cart.
            clearCart(conn, userId);

            conn.commit(); // ---- everything succeeded: make it permanent ----

            // Build the Order object to hand back to the caller.
            Order order = new Order();
            order.setId(orderId);
            order.setUserId(userId);
            order.setTotalAmount(total);
            order.setStatus("pending");
            order.setItems(orderItems);
            return order;

        } catch (SQLException e) {
            System.err.println("Error placing order (rolling back): " + e.getMessage());
            rollbackQuietly(conn);
            return null;
        } finally {
            closeQuietly(conn);
        }
    }

    // ---- transaction helper steps (all operate on the passed-in conn) ----

    /**
     * Read the user's cart on the transaction connection, locking each
     * referenced product row with FOR UPDATE. Mirrors CartDAOImpl.getCart's
     * join but stays inside our transaction.
     */
    private List<CartItem> readCartForUpdate(Connection conn, int userId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        String sql =
            "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, ci.added_at, " +
            "       p.name AS product_name, p.price " +
            "FROM cart_items ci " +
            "JOIN products p ON p.id = ci.product_id " +
            "WHERE ci.user_id = ? " +
            "ORDER BY ci.id " +
            "FOR UPDATE OF p"; // lock the product rows, not the cart rows

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        }
        return items;
    }

    private int getStock(Connection conn, int productId) throws SQLException {
        String sql = "SELECT stock_qty FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("stock_qty");
            }
        }
        return 0;
    }

    private int insertOrder(Connection conn, int userId, BigDecimal total) throws SQLException {
        String sql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, 'pending')";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setBigDecimal(2, total);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to obtain generated order id");
    }

    private void insertOrderItem(Connection conn, int orderId, CartItem line) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, line.getProductId());
            stmt.setInt(3, line.getQuantity());
            stmt.setBigDecimal(4, line.getPrice()); // snapshot the current price
            stmt.executeUpdate();
        }
    }

    private void decrementStock(Connection conn, int productId, int qty) throws SQLException {
        // The CHECK (stock_qty >= 0) constraint is a backstop; we already
        // verified availability above under FOR UPDATE.
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qty);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    private void clearCart(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // ---- read methods (plain, own-connection, no transaction) ----

    @Override
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapRowToOrder(rs);
                    order.setItems(orderItemDAO.getItemsByOrderId(orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders by user: " + e.getMessage());
            return orders;
        }
        // Populate items per order (separate step so the ResultSet above is closed first).
        for (Order order : orders) {
            order.setItems(orderItemDAO.getItemsByOrderId(order.getId()));
        }
        return orders;
    }

    @Override
    public boolean updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) order.setCreatedAt(ts.toLocalDateTime());
        return order;
    }

    // ---- connection cleanup helpers ----

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true); // restore default before closing
                conn.close();
            } catch (SQLException ex) {
                System.err.println("Error closing connection: " + ex.getMessage());
            }
        }
    }
}
