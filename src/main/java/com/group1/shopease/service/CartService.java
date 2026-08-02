package com.group1.shopease.service;

import com.group1.shopease.exception.InsufficientStockException;
import com.group1.shopease.model.CartItem;
import com.group1.shopease.repository.CartRepository;
import com.group1.shopease.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartRepository carts; private final UserRepository users;
    public CartService(CartRepository carts, UserRepository users) { this.carts = carts; this.users = users; }
    public List<CartItem> view(String email) { return carts.findByUser(userId(email)); }
    public void add(String email, long productId, int quantity) { if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive"); carts.addOrUpdate(userId(email), productId, quantity); validateStock(email); }
    public void update(String email, long productId, int quantity) { if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive"); if (!carts.update(userId(email), productId, quantity)) throw new IllegalArgumentException("Cart item was not found"); validateStock(email); }
    public void remove(String email, long productId) { carts.remove(userId(email), productId); }
    public BigDecimal total(String email) { return view(email).stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private void validateStock(String email) { for (CartItem item : view(email)) if (item.quantity() > item.availableStock()) throw new InsufficientStockException(item.productName()); }
    private long userId(String email) { return users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found")).getId(); }
}
