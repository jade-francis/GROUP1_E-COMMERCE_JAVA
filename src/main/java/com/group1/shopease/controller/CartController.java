package com.group1.shopease.controller;
import com.group1.shopease.model.CartItem;
import com.group1.shopease.service.CartService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
@RestController @RequestMapping("/api/cart")
public class CartController {
    private final CartService service;
    public CartController(CartService service) { this.service = service; }
    @GetMapping public List<CartItem> view(Principal p) { return service.view(p.getName()); }
    @GetMapping("/total") public BigDecimal total(Principal p) { return service.total(p.getName()); }
    @PostMapping("/{productId}") public void add(@PathVariable long productId, @RequestParam int quantity, Principal p) { service.add(p.getName(), productId, quantity); }
    @PutMapping("/{productId}") public void update(@PathVariable long productId, @RequestParam int quantity, Principal p) { service.update(p.getName(), productId, quantity); }
    @DeleteMapping("/{productId}") public void remove(@PathVariable long productId, Principal p) { service.remove(p.getName(), productId); }
}
