package com.shopease.web;

import com.shopease.db.Product;
import com.shopease.db.ProductDAO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductService — the business layer between the web controller and the DAO.
 *
 * Right now it's a thin pass-through, and that's fine: the point of the layer
 * is that ANY business rules (validation, permission checks, combining multiple
 * DAO calls) live here, NOT in the controller and NOT in the DAO. The controller
 * stays about HTTP; the DAO stays about SQL; this is where "what the app does"
 * goes.
 *
 * Spring injects the ProductDAO bean (declared in DaoConfig) via the constructor.
 */
@Service
public class ProductService {

    private final ProductDAO productDAO;

    // Constructor injection — Spring passes in the ProductDAO bean automatically.
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public Product getProductById(int id) {
        return productDAO.getProductById(id);
    }

    public List<Product> getProductsByCategory(int categoryId) {
        return productDAO.getProductsByCategory(categoryId);
    }

    public boolean addProduct(Product product) {
        return productDAO.addProduct(product);
    }
}
