package com.group1.shopease.repository;
import com.group1.shopease.model.Order;
import com.group1.shopease.model.OrderItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;
    public OrderRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Order> findByIdForBuyer(long orderId, long buyerId) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                "SELECT id,user_id,total_amount,status,payment_status,payment_method,shipping_address,created_at FROM orders WHERE id=? AND user_id=?",
                mapper, orderId, buyerId));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<OrderItem> findItems(long orderId) {
        return jdbc.query(
            "SELECT p.id AS product_id, p.name AS product_name, p.image_url, oi.quantity, oi.price_at_purchase " +
            "FROM order_items oi JOIN products p ON p.id = oi.product_id WHERE oi.order_id = ?",
            (rs, n) -> new OrderItem(
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getString("image_url"),
                rs.getInt("quantity"),
                rs.getBigDecimal("price_at_purchase"),
                rs.getBigDecimal("price_at_purchase").multiply(BigDecimal.valueOf(rs.getInt("quantity")))
            ),
            orderId
        );
    }

    public long count() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        return total == null ? 0 : total;
    }
    public Order create(long buyerId, BigDecimal total, String address, String paymentMethod) {
        return jdbc.queryForObject("INSERT INTO orders(user_id,total_amount,status,payment_status,payment_method,shipping_address) VALUES (?,?,'PENDING','PENDING',?,?) RETURNING id,user_id,total_amount,status,payment_status,payment_method,shipping_address,created_at", mapper, buyerId,total,paymentMethod,address);
    }
    public void addItem(long orderId, long productId, int quantity, BigDecimal price) { jdbc.update("INSERT INTO order_items(order_id,product_id,quantity,price_at_purchase) VALUES (?,?,?,?)",orderId,productId,quantity,price); }
    public List<Order> findByBuyer(long buyerId) { return jdbc.query("SELECT id,user_id,total_amount,status,payment_status,payment_method,shipping_address,created_at FROM orders WHERE user_id=? ORDER BY created_at DESC",mapper,buyerId); }
    public List<Order> findBySeller(long sellerId) { return jdbc.query("SELECT DISTINCT o.id,o.user_id,o.total_amount,o.status,o.payment_status,o.payment_method,o.shipping_address,o.created_at FROM orders o JOIN order_items oi ON oi.order_id=o.id JOIN products p ON p.id=oi.product_id WHERE p.seller_id=? ORDER BY o.created_at DESC",mapper,sellerId); }
    public boolean updateStatus(long orderId, long sellerId, String status) { return jdbc.update("UPDATE orders SET status=? WHERE id=? AND EXISTS (SELECT 1 FROM order_items oi JOIN products p ON p.id=oi.product_id WHERE oi.order_id=orders.id AND p.seller_id=?)",status,orderId,sellerId)>0; }
    public boolean reduceStock(long productId, int quantity) { return jdbc.update("UPDATE products SET stock_quantity=stock_quantity-? WHERE id=? AND stock_quantity>=?",quantity,productId,quantity)>0; }
    private final org.springframework.jdbc.core.RowMapper<Order> mapper = (rs,n) -> new Order(rs.getLong("id"),rs.getLong("user_id"),rs.getBigDecimal("total_amount"),rs.getString("status"),rs.getString("payment_status"),rs.getString("payment_method"),rs.getString("shipping_address"),rs.getTimestamp("created_at").toLocalDateTime());
}
