package com.group1.shopease.service;
import com.group1.shopease.exception.InsufficientStockException;
import com.group1.shopease.model.Order;
import com.group1.shopease.repository.CartRepository;
import com.group1.shopease.repository.OrderRepository;
import com.group1.shopease.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service public class OrderService {
    private final OrderRepository orders; private final CartRepository carts; private final UserRepository users;
    public OrderService(OrderRepository orders, CartRepository carts, UserRepository users){this.orders=orders;this.carts=carts;this.users=users;}
    @Transactional public Order checkout(String email,String address){ return checkout(email,address,"PAY_ON_DELIVERY"); }
    @Transactional public Order checkout(String email,String address,String paymentMethod){
        if(address==null||address.isBlank()) throw new IllegalArgumentException("Shipping address is required");
        if(!"PAY_ON_DELIVERY".equals(paymentMethod)) throw new IllegalArgumentException("Only pay on delivery is currently available");
        long buyer=users.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found")).getId(); var items=carts.findByUser(buyer);
        if(items.isEmpty()) throw new IllegalArgumentException("Cart is empty");
        var total=items.stream().map(com.group1.shopease.model.CartItem::lineTotal).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add); Order order=orders.create(buyer,total,address,paymentMethod);
        for(var item:items){if(!orders.reduceStock(item.productId(),item.quantity())) throw new InsufficientStockException(item.productName()); orders.addItem(order.id(),item.productId(),item.quantity(),item.unitPrice());} carts.clear(buyer); return order;
    }
    public List<Order> buyerOrders(String email){return orders.findByBuyer(users.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found")).getId());}
    public Order buyerOrder(String email, long orderId){
        long buyerId = users.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found")).getId();
        return orders.findByIdForBuyer(orderId, buyerId).orElseThrow(()->new IllegalArgumentException("Order not found"));
    }
    public List<com.group1.shopease.model.OrderItem> orderItems(long orderId){return orders.findItems(orderId);}
    public List<Order> sellerOrders(String email){return orders.findBySeller(users.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found")).getId());}
    public void updateStatus(String email,long orderId,String status){if(!List.of("SHIPPED","DELIVERED","CANCELLED").contains(status)) throw new IllegalArgumentException("Invalid order status"); if(!orders.updateStatus(orderId,users.findByEmail(email).orElseThrow().getId(),status)) throw new IllegalArgumentException("Order not found or not owned by seller");}
}
