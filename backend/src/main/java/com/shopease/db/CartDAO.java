package com.shopease.db;

import java.math.BigDecimal;
import java.util.List;

/**
 * CartDAO — the CONTRACT for managing a user's shopping cart.
 */
public interface CartDAO {
    /**
     * Add a product to the user's cart. If the product is already in the
     * cart, its quantity is increased by the given amount (upsert).
     */
    boolean addToCart(int userId, int productId, int quantity);

    /**
     * Return all items in the user's cart, enriched with product name/price.
     */
    List<CartItem> getCart(int userId);

    /**
     * Set an exact quantity for a product already in the cart.
     */
    boolean updateQuantity(int userId, int productId, int newQuantity);

    /**
     * Remove a single product from the user's cart.
     */
    boolean removeFromCart(int userId, int productId);

    /**
     * Empty the user's entire cart.
     */
    boolean clearCart(int userId);

    /**
     * Total value of everything in the cart (sum of price * quantity).
     */
    BigDecimal getCartTotal(int userId);
}
