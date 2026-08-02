package com.group1.shopease.model;

import java.math.BigDecimal;

public record CartItem(Long id, Long productId, String productName, BigDecimal unitPrice,
                       int quantity, int availableStock, BigDecimal lineTotal) { }
