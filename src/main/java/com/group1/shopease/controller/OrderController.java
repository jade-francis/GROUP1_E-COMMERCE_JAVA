package com.group1.shopease.controller;
import com.group1.shopease.model.Order;
import com.group1.shopease.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
@RestController @RequestMapping("/api/orders") public class OrderController {
 private final OrderService service; public OrderController(OrderService service){this.service=service;}
 @PostMapping("/checkout") public Order checkout(@RequestParam String shippingAddress, Principal p){return service.checkout(p.getName(),shippingAddress);}
 @GetMapping public List<Order> mine(Principal p){return service.buyerOrders(p.getName());}
 @GetMapping("/seller") public List<Order> seller(Principal p){return service.sellerOrders(p.getName());}
 @PutMapping("/{id}/status") public void status(@PathVariable long id,@RequestParam String value,Principal p){service.updateStatus(p.getName(),id,value);}
}
