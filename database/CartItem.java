import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CartItem model — mirrors one row of "cart_items", enriched with a few
 * joined fields from "products" (name, price) so the cart can be displayed
 * without a second query. subtotal() is a convenience: price * quantity.
 */
public class CartItem {
    private int id;
    private int userId;
    private int productId;
    private int quantity;
    private LocalDateTime addedAt;

    // Joined from products (populated by getCart) — may be null for a bare row
    private String productName;
    private BigDecimal price;

    public CartItem() {
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    /** Line-item subtotal: price * quantity. Null-safe. */
    public BigDecimal subtotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "CartItem{product='" + productName + "', qty=" + quantity +
               ", price=" + price + ", subtotal=" + subtotal() + "}";
    }
}
