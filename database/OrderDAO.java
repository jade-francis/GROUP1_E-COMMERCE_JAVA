import java.util.List;

/**
 * OrderDAO — the CONTRACT for placing and reading orders.
 *
 * The headline method is placeOrder: it turns a user's current cart into a
 * permanent order ATOMICALLY (all-or-nothing). See OrderDAOImpl for the
 * transaction details.
 */
public interface OrderDAO {
    /**
     * Place an order from the user's current cart. This is a single atomic
     * transaction that:
     *   1. reads the cart (fails if empty),
     *   2. verifies every line has enough stock,
     *   3. creates the orders row (total locked in),
     *   4. inserts an order_items row per cart line (price snapshotted),
     *   5. decrements products.stock_qty,
     *   6. clears the cart.
     * If ANY step fails, the whole thing rolls back and nothing changes.
     *
     * Returns the fully-populated Order (with id and items) on success,
     * or null if the cart was empty, stock was insufficient, or a DB
     * error forced a rollback.
     */
    Order placeOrder(int userId);

    /**
     * Fetch a single order by id, with its line items populated.
     */
    Order getOrderById(int orderId);

    /**
     * Fetch all orders for a user (newest first), each with items populated.
     */
    List<Order> getOrdersByUser(int userId);

    /**
     * Update an order's status (pending, paid, shipped, delivered, cancelled).
     */
    boolean updateStatus(int orderId, String newStatus);
}
