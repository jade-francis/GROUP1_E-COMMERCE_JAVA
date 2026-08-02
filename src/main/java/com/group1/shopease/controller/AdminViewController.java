package com.group1.shopease.controller;

import com.group1.shopease.model.Product;
import com.group1.shopease.model.User;
import com.group1.shopease.repository.CategoryRepository;
import com.group1.shopease.repository.OrderRepository;
import com.group1.shopease.service.ProductService;
import com.group1.shopease.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    public AdminViewController(UserService userService, ProductService productService,
                                CategoryRepository categoryRepository, OrderRepository orderRepository) {
        this.userService = userService;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        model.addAttribute("totalProducts", productService.findAll().size());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("pendingSellers", userService.pendingSellers().size());
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(Model model) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        model.addAttribute("products", productService.findAll());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable long id, Model model) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        productService.create(product);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable long id, @ModelAttribute Product product) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        productService.update(id, product);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable long id) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        productService.delete(id);
        return "redirect:/admin/products";
    }

    private String requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? null : "redirect:/";
    }

    @GetMapping("/sellers")
    public String sellers(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        
        List<User> sellers = userService.pendingSellers();
        model.addAttribute("sellers", sellers);
        return "admin/sellers";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approve(@PathVariable long id) {
        userService.approveSeller(id);
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/suspend")
    public String suspend(@PathVariable long id) {
        userService.suspendSeller(id);
        return "redirect:/admin/sellers";
    }
}