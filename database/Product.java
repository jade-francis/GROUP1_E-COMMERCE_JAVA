import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product model — a plain Java object (POJO) that mirrors
 * one row of the "products" table.
 *
 * This is the bridge between SQL rows and Java objects:
 * the DAO reads a ResultSet row and builds one of these,
 * so the rest of the backend never has to touch SQL directly.
 */
public class Product {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQty;
    private Integer categoryId; // Integer (not int) so it can be null
    private String imageUrl;
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(int id, String name, String description, BigDecimal price,
                    int stockQty, Integer categoryId, String imageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price +
               ", stockQty=" + stockQty + "}";
    }
}
