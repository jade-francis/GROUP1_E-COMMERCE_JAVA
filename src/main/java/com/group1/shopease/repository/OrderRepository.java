package com.group1.shopease.repository;
import com.group1.shopease.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;
    public OrderRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Order create(long buyerId, BigDecimal total, String address) {
        return jdbc.queryForObject("INSERT INTO orders(user_id,total_amount,status,payment_status,shipping_address) VALUES (?,?,'PENDING','PENDING',?) RETURNING id,user_id,total_amount,status,payment_status,shipping_address,created_at", mapper, buyerId,total,address);
    }
    public void addItem(long orderId, long productId, int quantity, BigDecimal price) { jdbc.update("INSERT INTO order_items(order_id,product_id,quantity,price_at_purchase) VALUES (?,?,?,?)",orderId,productId,quantity,price); }
    public List<Order> findByBuyer(long buyerId) { return jdbc.query("SELECT id,user_id,total_amount,status,payment_status,shipping_address,created_at FROM orders WHERE user_id=? ORDER BY created_at DESC",mapper,buyerId); }
    public List<Order> findBySeller(long sellerId) { return jdbc.query("SELECT DISTINCT o.id,o.user_id,o.total_amount,o.status,o.payment_status,o.shipping_address,o.created_at FROM orders o JOIN order_items oi ON oi.order_id=o.id JOIN products p ON p.id=oi.product_id WHERE p.seller_id=? ORDER BY o.created_at DESC",mapper,sellerId); }
    public boolean updateStatus(long orderId, long sellerId, String status) { return jdbc.update("UPDATE orders SET status=? WHERE id=? AND EXISTS (SELECT 1 FROM order_items oi JOIN products p ON p.id=oi.product_id WHERE oi.order_id=orders.id AND p.seller_id=?)",status,orderId,sellerId)>0; }
    public boolean reduceStock(long productId, int quantity) { return jdbc.update("UPDATE products SET stock_quantity=stock_quantity-? WHERE id=? AND stock_quantity>=?",quantity,productId,quantity)>0; }
    private final org.springframework.jdbc.core.RowMapper<Order> mapper = (rs,n) -> new Order(rs.getLong("id"),rs.getLong("user_id"),rs.getBigDecimal("total_amount"),rs.getString("status"),rs.getString("payment_status"),rs.getString("shipping_address"),rs.getTimestamp("created_at").toLocalDateTime());
}
