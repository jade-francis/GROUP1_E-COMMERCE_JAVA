package com.group1.shopease.controller;

import com.group1.shopease.model.CartItem;
import com.group1.shopease.service.CartService;
import com.group1.shopease.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutViewController {

    private final OrderService orderService;
    private final CartService cartService;

    public CheckoutViewController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping
    public String showCheckout(Authentication auth, Model model) {
        boolean requiresLogin = auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal());
        model.addAttribute("requiresLogin", requiresLogin);

        if (!requiresLogin) {
            List<CartItem> cartItems = cartService.view(auth.getName());
            BigDecimal total = cartItems.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
        }
        return "orders/checkout";
    }

    @PostMapping
    public String processCheckout(@RequestParam String shippingAddress,
                                   Authentication auth,
                                   Model model) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            model.addAttribute("error", "Please log in to checkout");
            return "orders/checkout";
        }
        try {
            String email = auth.getName();
            var order = orderService.checkout(email, shippingAddress);
            return "redirect:/checkout/success?id=" + order.id();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            List<CartItem> cartItems = cartService.view(auth.getName());
            BigDecimal total = cartItems.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
            return "orders/checkout";
        } catch (Exception e) {
            model.addAttribute("error", "Checkout failed: " + e.getMessage());
            return "orders/checkout";
        }
    }

    @GetMapping("/success")
    public String checkoutSuccess(@RequestParam Long id, Model model) {
        model.addAttribute("orderId", id);
        return "orders/success";
    }
}