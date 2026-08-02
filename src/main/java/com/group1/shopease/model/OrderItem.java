package com.group1.shopease.model;

import java.math.BigDecimal;

public record OrderItem(Long productId, String productName, String imageUrl,
                         int quantity, BigDecimal priceAtPurchase, BigDecimal lineTotal) { }
