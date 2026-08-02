package com.group1.shopease.repository;

import com.group1.shopease.model.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public class CartRepository {
    private final JdbcTemplate jdbc;
    public CartRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<CartItem> findByUser(long userId) {
        return jdbc.query("""
                SELECT c.id, c.product_id, p.name, p.price, c.quantity, p.stock_quantity, p.image_url
                FROM cart_items c JOIN products p ON p.id = c.product_id
                WHERE c.user_id = ? ORDER BY c.id
                """, (rs, n) -> {
            BigDecimal price = rs.getBigDecimal("price"); int qty = rs.getInt("quantity");
            return new CartItem(rs.getLong("id"), rs.getLong("product_id"), rs.getString("name"), price,
                    qty, rs.getInt("stock_quantity"), price.multiply(BigDecimal.valueOf(qty)), rs.getString("image_url"));
        }, userId);
    }
    public void addOrUpdate(long userId, long productId, int quantity) {
        jdbc.update("""
                INSERT INTO cart_items(user_id, product_id, quantity) VALUES (?, ?, ?)
                ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity
                """, userId, productId, quantity);
    }
    public boolean update(long userId, long productId, int quantity) {
        return jdbc.update("UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?", quantity, userId, productId) > 0;
    }
    public boolean remove(long userId, long productId) { return jdbc.update("DELETE FROM cart_items WHERE user_id = ? AND product_id = ?", userId, productId) > 0; }
    public void clear(long userId) { jdbc.update("DELETE FROM cart_items WHERE user_id = ?", userId); }
}
