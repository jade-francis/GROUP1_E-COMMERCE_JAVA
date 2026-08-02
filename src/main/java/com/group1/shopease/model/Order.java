package com.group1.shopease.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record Order(Long id, Long buyerId, BigDecimal totalAmount, String status, String paymentStatus, String shippingAddress, LocalDateTime createdAt) { }
