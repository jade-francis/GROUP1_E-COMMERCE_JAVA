package com.group1.shopease.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(long id) {
        super("Product with ID " + id + " was not found");
    }
}
