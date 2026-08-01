package com.shopease.db;

import java.util.List;

/**
 * OrderItemDAO — the CONTRACT for reading the line items of an order.
 *
 * Note there is no standalone "add item" here: order items are only ever
 * created as part of placing an order, which happens atomically inside
 * OrderDAO.placeOrder (all in one transaction). This interface is the
 * read side — fetching the items that belong to an already-placed order.
 */
public interface OrderItemDAO {
    /**
     * Return all line items for a given order, enriched with product name.
     */
    List<OrderItem> getItemsByOrderId(int orderId);
}
