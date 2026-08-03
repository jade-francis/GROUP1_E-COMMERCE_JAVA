package com.group1.shopease.controller;

import com.group1.shopease.model.Product;
import com.group1.shopease.model.User;
import com.group1.shopease.repository.CategoryRepository;
import com.group1.shopease.repository.OrderRepository;
import com.group1.shopease.service.ProductService;
import com.group1.shopease.service.ImageStorageService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final ImageStorageService imageStorageService;

    public AdminViewController(UserService userService, ProductService productService,
                                CategoryRepository categoryRepository, OrderRepository orderRepository,
                                ImageStorageService imageStorageService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.imageStorageService = imageStorageService;
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
    public String createProduct(@ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes attributes) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        try {
            String uploadedImageUrl = imageStorageService.storeProductImage(imageFile);
            if (uploadedImageUrl != null) product.setImageUrl(uploadedImageUrl);
            productService.create(product);
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/new";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable long id, @ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes attributes) {
        String guard = requireAdmin();
        if (guard != null) return guard;

        try {
            String uploadedImageUrl = imageStorageService.storeProductImage(imageFile);
            if (uploadedImageUrl != null) product.setImageUrl(uploadedImageUrl);
            productService.update(id, product);
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
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

        List<User> pendingSellers = userService.pendingSellers();
        List<User> allSellers = userService.allSellers();
        
        model.addAttribute("pendingSellers", pendingSellers);
        model.addAttribute("allSellers", allSellers);
        return "admin/sellers";
    }

    @GetMapping("/users")
    public String users(Model model) {
        String guard = requireAdmin();
        if (guard != null) return guard;
        model.addAttribute("users", userService.allUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable long id, RedirectAttributes attributes) {
        String guard = requireAdmin();
        if (guard != null) return guard;
        boolean deleted = userService.deleteUser(id);
        attributes.addFlashAttribute(deleted ? "success" : "error",
                deleted ? "User removed." : "The user could not be removed.");
        return "redirect:/admin/users";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approve(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            userService.approveSeller(id);
            redirectAttributes.addFlashAttribute("success", "Seller approved successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/suspend")
    public String suspend(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            userService.suspendSeller(id);
            redirectAttributes.addFlashAttribute("success", "Seller suspended successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/revoke")
    public String revoke(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            if (userService.revokeSellerRequest(id)) {
                redirectAttributes.addFlashAttribute("success", "Seller request revoked. User is now a customer.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Cannot revoke: request not pending or user not a seller.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }
}
