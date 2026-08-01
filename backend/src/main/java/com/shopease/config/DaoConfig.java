package com.shopease.config;

import com.shopease.db.CartDAO;
import com.shopease.db.CartDAOImpl;
import com.shopease.db.CategoryDAO;
import com.shopease.db.CategoryDAOImpl;
import com.shopease.db.OrderDAO;
import com.shopease.db.OrderDAOImpl;
import com.shopease.db.OrderItemDAO;
import com.shopease.db.OrderItemDAOImpl;
import com.shopease.db.ProductDAO;
import com.shopease.db.ProductDAOImpl;
import com.shopease.db.UserDAO;
import com.shopease.db.UserDAOImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DaoConfig — registers the existing JDBC DAO implementations as Spring beans.
 *
 * The DAOs (com.shopease.db.*) were written and tested BEFORE Spring existed in
 * this project. Rather than annotate each impl with @Repository (which would
 * mean editing the tested files), we declare them here as @Bean factory methods.
 * The tested code stays byte-for-byte identical apart from its package line.
 *
 * Each DAO manages its own connections through DBConnection, which reads
 * db.properties from the classpath (src/main/resources/db.properties). That's
 * why we don't need a Spring DataSource for the current design — the DAO layer
 * is self-contained. If the teammate later wants Spring-managed pooling, swap
 * DBConnection for an injected javax.sql.DataSource; the interfaces won't change.
 */
@Configuration
public class DaoConfig {

    @Bean
    public ProductDAO productDAO() {
        return new ProductDAOImpl();
    }

    @Bean
    public UserDAO userDAO() {
        return new UserDAOImpl();
    }

    @Bean
    public CategoryDAO categoryDAO() {
        return new CategoryDAOImpl();
    }

    @Bean
    public CartDAO cartDAO() {
        return new CartDAOImpl();
    }

    @Bean
    public OrderDAO orderDAO() {
        return new OrderDAOImpl();
    }

    @Bean
    public OrderItemDAO orderItemDAO() {
        return new OrderItemDAOImpl();
    }
}
