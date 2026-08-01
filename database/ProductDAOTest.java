import java.math.BigDecimal;
import java.util.List;

/**
 * Quick manual test — NOT a proper unit test, just a way to
 * confirm your DAO actually talks to shopease correctly before
 * pushing to the repo. Run this, look at the console output.
 *
 * Once your backend teammate starts, he can call ProductDAOImpl
 * the exact same way you're calling it here.
 */
public class ProductDAOTest {
    public static void main(String[] args) {
        ProductDAO productDAO = new ProductDAOImpl();

        System.out.println("=== All Products ===");
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            System.out.println(p);
        }

        System.out.println("\n=== Product by ID (1) ===");
        Product single = productDAO.getProductById(1);
        System.out.println(single);

        System.out.println("\n=== Adding a new product ===");
        Product newProduct = new Product();
        newProduct.setName("Bluetooth Speaker");
        newProduct.setDescription("Portable speaker with 10hr battery");
        newProduct.setPrice(new BigDecimal("12000.00"));
        newProduct.setStockQty(20);
        newProduct.setCategoryId(1);
        boolean added = productDAO.addProduct(newProduct);
        System.out.println("Product added: " + added);

        System.out.println("\n=== All Products After Insert ===");
        for (Product p : productDAO.getAllProducts()) {
            System.out.println(p);
        }
    }
}
