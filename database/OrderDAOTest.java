import java.math.BigDecimal;
import java.util.List;

/**
 * Quick manual test for OrderDAO — the transactional checkout.
 *
 * Covers the paths that matter:
 *   1. Empty cart -> placeOrder returns null (nothing created).
 *   2. Happy path -> cart becomes an order; stock is decremented; cart is
 *      cleared; total and line snapshots are correct.
 *   3. getOrderById / getOrdersByUser return the order with its items.
 *   4. updateStatus flips the status.
 *   5. Insufficient stock -> placeOrder ROLLS BACK: no order, stock and cart
 *      unchanged (this is the whole point of the transaction).
 *
 * Self-cleaning: registers a throwaway user (unique email) and deletes it at
 * the end. Because orders.user_id is ON DELETE RESTRICT, we must delete this
 * user's orders (and their order_items, which cascade) BEFORE deleting the user.
 */
public class OrderDAOTest {
    public static void main(String[] args) {
        OrderDAO orderDAO = new OrderDAOImpl();
        CartDAO cartDAO = new CartDAOImpl();
        UserDAO userDAO = new UserDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();

        String email = "order+" + System.currentTimeMillis() + "@shopease.com";
        User owner = userDAO.register("Order Tester", email, "S3cret!pass", "customer");
        if (owner == null) {
            System.out.println("Could not create test user — aborting.");
            return;
        }
        int userId = owner.getId();
        System.out.println("Test user id: " + userId);

        // ---- 1. Empty cart -> null ----
        System.out.println("\n=== Place order with EMPTY cart (expect null) ===");
        Order none = orderDAO.placeOrder(userId);
        System.out.println("Result: " + none);

        // ---- Set up a cart: product 1 x2, product 2 x1 ----
        int stock1Before = productDAO.getProductById(1).getStockQty();
        int stock2Before = productDAO.getProductById(2).getStockQty();
        System.out.println("\nStock before: product1=" + stock1Before + ", product2=" + stock2Before);

        cartDAO.addToCart(userId, 1, 2);
        cartDAO.addToCart(userId, 2, 1);
        BigDecimal cartTotal = cartDAO.getCartTotal(userId);
        System.out.println("Cart total before checkout: " + cartTotal);

        // ---- 2. Happy path ----
        System.out.println("\n=== Place order (happy path) ===");
        Order order = orderDAO.placeOrder(userId);
        System.out.println("Placed: " + order);
        if (order == null) {
            System.out.println("Unexpected null — aborting.");
            cleanup(userDAO, orderDAO, userId);
            return;
        }
        for (OrderItem oi : order.getItems()) {
            System.out.println("  " + oi);
        }

        System.out.println("Order total matches cart total? " +
            (order.getTotalAmount().compareTo(cartTotal) == 0));

        int stock1After = productDAO.getProductById(1).getStockQty();
        int stock2After = productDAO.getProductById(2).getStockQty();
        System.out.println("Stock after: product1=" + stock1After + " (expect " + (stock1Before - 2) +
            "), product2=" + stock2After + " (expect " + (stock2Before - 1) + ")");
        System.out.println("Stock decremented correctly? " +
            (stock1After == stock1Before - 2 && stock2After == stock2Before - 1));

        System.out.println("Cart cleared after order? " + cartDAO.getCart(userId).isEmpty());

        // ---- 3. Read back ----
        System.out.println("\n=== getOrderById ===");
        Order fetched = orderDAO.getOrderById(order.getId());
        System.out.println("Fetched: " + fetched + " with " +
            (fetched != null ? fetched.getItems().size() : 0) + " items");

        System.out.println("\n=== getOrdersByUser ===");
        List<Order> userOrders = orderDAO.getOrdersByUser(userId);
        System.out.println("Order count for user: " + userOrders.size());

        // ---- 4. updateStatus ----
        System.out.println("\n=== updateStatus -> paid ===");
        System.out.println("updateStatus: " + orderDAO.updateStatus(order.getId(), "paid"));
        System.out.println("Status now: " + orderDAO.getOrderById(order.getId()).getStatus());

        // ---- 5. Insufficient stock -> rollback ----
        System.out.println("\n=== Insufficient stock (expect null + NO changes) ===");
        int stock3Before = productDAO.getProductById(3).getStockQty();
        cartDAO.addToCart(userId, 3, stock3Before + 100); // more than exists
        int ordersBeforeBad = orderDAO.getOrdersByUser(userId).size();

        Order bad = orderDAO.placeOrder(userId);
        System.out.println("Result (expect null): " + bad);

        int stock3After = productDAO.getProductById(3).getStockQty();
        int ordersAfterBad = orderDAO.getOrdersByUser(userId).size();
        System.out.println("Product 3 stock unchanged? " + (stock3After == stock3Before) +
            " (" + stock3Before + " -> " + stock3After + ")");
        System.out.println("No new order created? " + (ordersAfterBad == ordersBeforeBad));
        System.out.println("Cart NOT cleared (still has the bad line)? " +
            !cartDAO.getCart(userId).isEmpty());

        // ---- Cleanup ----
        System.out.println("\n=== Cleanup ===");
        cartDAO.clearCart(userId);
        cleanup(userDAO, orderDAO, userId);
    }

    /**
     * Delete the test user. orders.user_id is ON DELETE RESTRICT, so we must
     * remove the user's orders first (order_items cascade off orders). We do
     * this with a direct connection since OrderDAO has no delete method (there
     * is no business reason to delete a real order).
     */
    private static void cleanup(UserDAO userDAO, OrderDAO orderDAO, int userId) {
        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement stmt =
                 conn.prepareStatement("DELETE FROM orders WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            int deleted = stmt.executeUpdate();
            System.out.println("Deleted " + deleted + " order(s) for test user.");
        } catch (java.sql.SQLException e) {
            System.err.println("Cleanup (orders) failed: " + e.getMessage());
        }
        System.out.println("deleteUser: " + userDAO.deleteUser(userId));
    }
}
