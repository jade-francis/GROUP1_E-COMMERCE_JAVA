package com.group1.shopease.service;

import com.group1.shopease.exception.ProductNotFoundException;
import com.group1.shopease.model.Product;
import com.group1.shopease.repository.ProductRepository;
import com.group1.shopease.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ProductService(ProductRepository productRepository) {
        this(productRepository, null);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAllRandomized() {
        List<Product> products = new ArrayList<>(productRepository.findAll());
        Collections.shuffle(products);
        return products;
    }
    public List<Product> search(String query, Long categoryId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination");
        return productRepository.search(query, categoryId, page, size);
    }

    public Product findById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public java.util.Optional<Product> findByIdOptional(long id) {
        return productRepository.findById(id);
    }

    public Product create(Product product, String sellerEmail) {
        var seller = approvedSeller(sellerEmail);
        product.setId(null);
        product.setSellerId(seller.getId());
        return productRepository.save(product);
    }

    /** Compatibility overload for service-level callers; API writes require an approved seller. */
    public Product create(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    public Product update(long id, Product product, String sellerEmail) {
        var seller = approvedSeller(sellerEmail);
        product.setId(id);
        product.setSellerId(seller.getId());
        if (!productRepository.update(product)) {
            throw new IllegalArgumentException("Product does not exist or belongs to another seller");
        }
        return product;
    }

    public void delete(long id, String sellerEmail) {
        var seller = approvedSeller(sellerEmail);
        if (!productRepository.deleteByIdAndSellerId(id, seller.getId())) {
            throw new IllegalArgumentException("Product does not exist or belongs to another seller");
        }
    }

    public void delete(long id) {
        if (!productRepository.deleteById(id)) throw new ProductNotFoundException(id);
    }

    /** Admin-scoped update: no seller-ownership check. */
    public Product update(long id, Product product) {
        product.setId(id);
        if (!productRepository.updateAny(product)) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public List<Product> findBySellerId(long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    private com.group1.shopease.model.User approvedSeller(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Seller account was not found"));
        if (!"SELLER".equals(user.getRole()) || !"APPROVED".equals(user.getSellerStatus())) {
            throw new IllegalArgumentException("Seller account is not approved");
        }
        return user;
    }
}
