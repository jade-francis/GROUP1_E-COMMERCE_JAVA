import java.util.List;

/**
 * ProductDAO — the CONTRACT for how the rest of the backend
 * is allowed to interact with product data.
 *
 * This is the file you hand to your backend teammate.
 * He doesn't need to know a single line of SQL — he just
 * calls these methods and gets back Java objects.
 */
public interface ProductDAO {
    List<Product> getAllProducts();
    Product getProductById(int id);
    List<Product> getProductsByCategory(int categoryId);
    boolean addProduct(Product product);
    boolean updateStock(int productId, int newQty);
    boolean deleteProduct(int id);
}
