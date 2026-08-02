package com.group1.shopease.controller;

import com.group1.shopease.model.Order;
import com.group1.shopease.model.User;
import com.group1.shopease.service.OrderService;
import com.group1.shopease.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;
    private final UserService userService;

    public SellerOrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    private User getCurrentSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public String listOrders(Model model) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }

        List<Order> orders = orderService.sellerOrders(seller.getEmail());
        model.addAttribute("orders", orders);
        return "seller/orders/list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }

        try {
            orderService.updateStatus(seller.getEmail(), id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated to " + status);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/orders";
    }
}