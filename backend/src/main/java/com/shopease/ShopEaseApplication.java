package com.shopease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ShopEase backend entry point.
 *
 * Run with:  mvn spring-boot:run
 * Then hit:  http://localhost:8080/api/products
 *
 * This wraps the existing, already-tested JDBC DAO layer (com.shopease.db)
 * behind a Spring Boot web server. The DAOs are unchanged apart from being
 * moved into a named package; see DaoConfig for how they become Spring beans.
 */
@SpringBootApplication
public class ShopEaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopEaseApplication.class, args);
    }
}
