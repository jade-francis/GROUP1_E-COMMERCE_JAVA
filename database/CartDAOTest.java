import java.math.BigDecimal;
import java.util.List;

/**
 * Quick manual test for CartDAO — walks a single user's cart through its
 * whole lifecycle (add, upsert-bump, update, total, remove, clear) and
 * prints the state at each step so you can eyeball it in the console.
 *
 * It registers a throwaway user first (unique timestamp email) so the
 * cart has a real user_id to hang off of, then uses the seeded products
 * (ids 1, 2, 3 from schema.sql). Cleans up its own user at the end so
 * repeated runs don't pile up test rows.
 */
public class CartDAOTest {
    public static void main(String[] args) {
        CartDAO cartDAO = new CartDAOImpl();
        UserDAO userDAO = new UserDAOImpl();

        // --- Set up a throwaway user to own the cart ---
        String email = "cart+" + System.currentTimeMillis() + "@shopease.com";
        User owner = userDAO.register("Cart Tester", email, "S3cret!pass", "customer");
        if (owner == null) {
            System.out.println("Could not create test user — aborting.");
            return;
        }
        int userId = owner.getId();
        System.out.println("Test user id: " + userId);

        // --- Add two different products ---
        System.out.println("\n=== Add product 1 (qty 2) and product 2 (qty 1) ===");
        System.out.println("addToCart(1, 2): " + cartDAO.addToCart(userId, 1, 2));
        System.out.println("addToCart(2, 1): " + cartDAO.addToCart(userId, 2, 1));
        printCart(cartDAO, userId);

        // --- Upsert: adding product 1 again should BUMP quantity, not duplicate ---
        System.out.println("\n=== Add product 1 again (qty 3) — should bump 2 -> 5, no new row ===");
        System.out.println("addToCart(1, 3): " + cartDAO.addToCart(userId, 1, 3));
        printCart(cartDAO, userId);

        // --- Update to an exact quantity ---
        System.out.println("\n=== Set product 1 quantity to exactly 4 ===");
        System.out.println("updateQuantity(1, 4): " + cartDAO.updateQuantity(userId, 1, 4));
        printCart(cartDAO, userId);

        // --- updateQuantity to 0 should remove the line ---
        System.out.println("\n=== updateQuantity(2, 0) — should remove product 2 ===");
        System.out.println("updateQuantity(2, 0): " + cartDAO.updateQuantity(userId, 2, 0));
        printCart(cartDAO, userId);

        // --- Cart total ---
        System.out.println("\n=== Cart total ===");
        System.out.println("getCartTotal: " + cartDAO.getCartTotal(userId));

        // --- Remove and clear ---
        System.out.println("\n=== removeFromCart(1) ===");
        System.out.println("removeFromCart(1): " + cartDAO.removeFromCart(userId, 1));
        printCart(cartDAO, userId);

        System.out.println("\n=== Re-add then clearCart ===");
        cartDAO.addToCart(userId, 3, 1);
        System.out.println("clearCart: " + cartDAO.clearCart(userId));
        printCart(cartDAO, userId);
        System.out.println("Total after clear (expect 0): " + cartDAO.getCartTotal(userId));

        // --- Clean up the throwaway user (cart_items cascade on user delete) ---
        System.out.println("\n=== Cleanup ===");
        System.out.println("deleteUser: " + userDAO.deleteUser(userId));
    }

    /** Print every line in the cart plus the running total. */
    private static void printCart(CartDAO cartDAO, int userId) {
        List<CartItem> items = cartDAO.getCart(userId);
        if (items.isEmpty()) {
            System.out.println("  (cart is empty)");
            return;
        }
        for (CartItem item : items) {
            System.out.println("  " + item);
        }
        System.out.println("  --> total: " + cartDAO.getCartTotal(userId));
    }
}
