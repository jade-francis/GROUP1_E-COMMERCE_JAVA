package com.group1.shopease.controller;

import com.group1.shopease.model.CartItem;
import com.group1.shopease.service.OrderService;
import com.group1.shopease.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutViewController {

    private final OrderService orderService;
    private final ProductService productService;

    public CheckoutViewController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    public String showCheckout(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean requiresLogin = auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal());
        model.addAttribute("requiresLogin", requiresLogin);
        
        if (!requiresLogin) {
            // Build cart items for the template
            List<CartItem> cartItems = getSessionCart(session);
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
                                   HttpSession session,
                                   Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
            // Re-add cart items for error display
            List<CartItem> cartItems = getSessionCart(session);
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

    private List<CartItem> getSessionCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<SessionCartItem> cart = (List<SessionCartItem>) session.getAttribute("cart");
        if (cart == null) return List.of();

        List<CartItem> result = new ArrayList<>();
        for (SessionCartItem item : cart) {
            Optional<com.group1.shopease.model.Product> product = productService.findByIdOptional(item.productId());
            if (product.isPresent()) {
                com.group1.shopease.model.Product p = product.get();
                result.add(new CartItem(
                    item.id(),
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    item.quantity(),
                    p.getStockQuantity(),
                    p.getPrice().multiply(BigDecimal.valueOf(item.quantity())),
                    p.getImageUrl()
                ));
            }
        }
        return result;
    }

    private record SessionCartItem(long id, Long productId, int quantity) {}
}