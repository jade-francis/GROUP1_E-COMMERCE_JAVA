package com.group1.shopease.exception;
public class InsufficientStockException extends RuntimeException { public InsufficientStockException(String product) { super("Insufficient stock for " + product); } }
