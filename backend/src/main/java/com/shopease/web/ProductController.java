package com.shopease.web;

import com.shopease.db.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController — the HTTP surface for products.
 *
 * This is the WORKED TEMPLATE for the rest of the backend. To add another
 * resource (users, categories, cart, orders), copy this three-file shape:
 *   1. DaoConfig already exposes the DAO bean.
 *   2. Write a <Thing>Service that injects the DAO (business layer).
 *   3. Write a <Thing>Controller like this one that injects the service.
 *
 * Jackson auto-converts the returned Java objects to/from JSON, so the getters
 * on Product/etc. become JSON fields with no extra work.
 *
 * Endpoints:
 *   GET  /api/products               -> all products
 *   GET  /api/products/{id}          -> one product (404 if missing)
 *   GET  /api/products?categoryId=1  -> products in a category
 *   POST /api/products               -> create a product (201 / 400)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     * GET /api/products?categoryId=1
     * The optional categoryId query param switches to the by-category lookup.
     */
    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) Integer categoryId) {
        if (categoryId != null) {
            return productService.getProductsByCategory(categoryId);
        }
        return productService.getAllProducts();
    }

    /** GET /api/products/{id} — 200 with the product, or 404 if not found. */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable int id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * POST /api/products — create a product from a JSON body.
     * Returns 201 Created on success, 400 Bad Request if the insert failed.
     */
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        boolean created = productService.addProduct(product);
        if (created) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Product created");
        }
        return ResponseEntity.badRequest().body("Could not create product");
    }
}
