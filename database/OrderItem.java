import java.math.BigDecimal;

/**
 * OrderItem model — mirrors one row of "order_items": a single product line
 * within a placed order.
 *
 * The important field is priceAtPurchase: it's a SNAPSHOT of the product's
 * price at the moment the order was placed. We never read the live price from
 * "products" for a historical order, because that price may have changed since.
 * productName is joined in from "products" for display convenience.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal priceAtPurchase;

    // Joined from products (populated on read) — may be null for a bare row
    private String productName;

    public OrderItem() {
    }

    public OrderItem(int productId, int quantity, BigDecimal priceAtPurchase) {
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(BigDecimal priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    /** Line-item subtotal: priceAtPurchase * quantity. Null-safe. */
    public BigDecimal subtotal() {
        if (priceAtPurchase == null) return BigDecimal.ZERO;
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "OrderItem{product='" + productName + "', qty=" + quantity +
               ", priceAtPurchase=" + priceAtPurchase + ", subtotal=" + subtotal() + "}";
    }
}
