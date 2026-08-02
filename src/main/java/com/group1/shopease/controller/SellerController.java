package com.group1.shopease.controller;

import com.group1.shopease.model.Order;
import com.group1.shopease.model.Product;
import com.group1.shopease.model.User;
import com.group1.shopease.service.OrderService;
import com.group1.shopease.service.ProductService;
import com.group1.shopease.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/seller")
public class SellerController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public SellerController(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/request")
    public String showRequestForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Redirect based on current status
        if ("SELLER".equals(user.getRole())) {
            if ("PENDING".equals(user.getSellerStatus())) {
                return "redirect:/profile?status=pending";
            } else if ("APPROVED".equals(user.getSellerStatus())) {
                return "redirect:/seller/dashboard";
            } else if ("SUSPENDED".equals(user.getSellerStatus())) {
                return "redirect:/profile?status=suspended";
            }
        }
        
        model.addAttribute("user", user);
        return "seller/request";
    }

    @PostMapping("/request")
    public String submitRequest(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        try {
            User updated = userService.requestSeller(auth.getName());
            redirectAttributes.addFlashAttribute("success", "Seller request submitted! Your account is now pending admin approval.");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/request";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!"SELLER".equals(user.getRole()) || !"APPROVED".equals(user.getSellerStatus())) {
            return "redirect:/profile";
        }

        List<Product> products = productService.findBySellerId(user.getId());
        List<Order> orders = orderService.sellerOrders(email);

        long pendingOrders = orders.stream().filter(o -> "PENDING".equals(o.status())).count();
        BigDecimal totalSales = orders.stream().map(Order::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        BigDecimal revenue30Days = orders.stream()
                .filter(o -> o.createdAt() != null && o.createdAt().isAfter(cutoff))
                .map(Order::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("productCount", products.size());
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("revenue30Days", revenue30Days);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        model.addAttribute("recentProducts", products.stream().limit(5).toList());
        return "seller/dashboard";
    }
}