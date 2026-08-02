package com.group1.shopease.controller;

import com.group1.shopease.model.Order;
import com.group1.shopease.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderViewController {

    private final OrderService orderService;

    public OrderViewController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        List<Order> orders = orderService.buyerOrders(principal.getName());
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        // Could add order detail view later
        return "redirect:/orders";
    }
}