package com.group1.shopease.controller;

import com.group1.shopease.model.CartItem;
import com.group1.shopease.model.Product;
import com.group1.shopease.service.CartService;
import com.group1.shopease.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartViewController {

    private final CartService cartService;
    private final ProductService productService;

    public CartViewController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public String view(Principal principal, HttpSession session, Model model) {
        List<CartItem> items;
        if (principal != null) {
            items = cartService.view(principal.getName());
        } else {
            items = getSessionCart(session);
        }
        BigDecimal total = items.stream()
            .map(CartItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart/view";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      Principal principal,
                      HttpSession session) {
        if (principal != null) {
            cartService.add(principal.getName(), productId, quantity);
        } else {
            addToSessionCart(session, productId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long productId,
                         @RequestParam int quantity,
                         Principal principal,
                         HttpSession session) {
        if (principal != null) {
            cartService.update(principal.getName(), productId, quantity);
        } else {
            updateSessionCart(session, productId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId,
                         Principal principal,
                         HttpSession session) {
        if (principal != null) {
            cartService.remove(principal.getName(), productId);
        } else {
            removeFromSessionCart(session, productId);
        }
        return "redirect:/cart";
    }

    private List<CartItem> getSessionCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<SessionCartItem> cart = (List<SessionCartItem>) session.getAttribute("cart");
        if (cart == null) return List.of();

        List<CartItem> result = new ArrayList<>();
        for (SessionCartItem item : cart) {
            Optional<Product> product = productService.findByIdOptional(item.productId());
            if (product.isPresent()) {
                Product p = product.get();
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

    private void addToSessionCart(HttpSession session, Long productId, int quantity) {
        @SuppressWarnings("unchecked")
        List<SessionCartItem> cart = (List<SessionCartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        // Check if product already in cart
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).productId().equals(productId)) {
                int newQty = cart.get(i).quantity() + quantity;
                cart.set(i, new SessionCartItem(cart.get(i).id(), productId, newQty));
                return;
            }
        }

        // New item - use a temporary ID (negative for session items)
        long tempId = -System.currentTimeMillis();
        cart.add(new SessionCartItem(tempId, productId, quantity));
    }

    private void updateSessionCart(HttpSession session, Long productId, int quantity) {
        @SuppressWarnings("unchecked")
        List<SessionCartItem> cart = (List<SessionCartItem>) session.getAttribute("cart");
        if (cart == null) return;

        cart.removeIf(item -> item.productId().equals(productId));
        if (quantity > 0) {
            cart.add(new SessionCartItem(-System.currentTimeMillis(), productId, quantity));
        }
    }

    private void removeFromSessionCart(HttpSession session, Long productId) {
        @SuppressWarnings("unchecked")
        List<SessionCartItem> cart = (List<SessionCartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.productId().equals(productId));
        }
    }

    // Internal record for session storage
    private record SessionCartItem(long id, Long productId, int quantity) {}
}