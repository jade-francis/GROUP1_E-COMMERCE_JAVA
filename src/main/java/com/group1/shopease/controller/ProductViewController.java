package com.group1.shopease.controller;

import com.group1.shopease.model.Product;
import com.group1.shopease.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductViewController {

    private final ProductService productService;

    public ProductViewController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long categoryId, Model model) {
        List<Product> products;
        if (categoryId != null) {
            products = productService.findAll().stream()
                .filter(p -> categoryId.equals(p.getCategoryId()))
                .toList();
        } else {
            products = productService.findAll();
        }
        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", categoryId);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findByIdOptional(id);
        if (product.isEmpty()) {
            return "redirect:/products";
        }
        model.addAttribute("product", product.get());
        return "products/details";
    }
}