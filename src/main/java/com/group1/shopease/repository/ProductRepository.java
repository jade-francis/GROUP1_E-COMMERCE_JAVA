package com.group1.shopease.repository;

import com.group1.shopease.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAll() {
        String sql = """
                SELECT id, name, description, price,
                         stock_quantity, category_id, image_url, seller_id
                FROM products
                ORDER BY id DESC
                """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    public Optional<Product> findById(long id) {
        String sql = """
            SELECT id, name, description, price,
                         stock_quantity, category_id, image_url, seller_id
            FROM products
            WHERE id = ?
            """;

        return jdbcTemplate.query(sql, this::mapRow, id)
            .stream()
            .findFirst();
    }

    public List<Product> search(String query, Long categoryId, int page, int size) {
        int offset = page * size;
        String text = StringUtils.hasText(query) ? "%" + query.trim().toLowerCase() + "%" : "%";
        if (categoryId == null) {
            String sql = """
                    SELECT id, name, description, price, stock_quantity, category_id, image_url, seller_id
                    FROM products
                    WHERE (lower(name) LIKE ? OR lower(coalesce(description, '')) LIKE ?)
                    ORDER BY id DESC LIMIT ? OFFSET ?
                    """;
            return jdbcTemplate.query(sql, this::mapRow, text, text, size, offset);
        } else {
            String sql = """
                    SELECT id, name, description, price, stock_quantity, category_id, image_url, seller_id
                    FROM products
                    WHERE (lower(name) LIKE ? OR lower(coalesce(description, '')) LIKE ?)
                      AND category_id = ?
                    ORDER BY id DESC LIMIT ? OFFSET ?
                    """;
            return jdbcTemplate.query(sql, this::mapRow, text, text, categoryId, size, offset);
        }
    }

    public Product save(Product product) {
        String sql = """
                INSERT INTO products
                    (name, description, price, stock_quantity, category_id, image_url, seller_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    new String[] { "id" }
            );
            setProductParameters(statement, product);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Database did not return a product ID");
        }
        product.setId(generatedId.longValue());
        return product;
    }

    public boolean update(Product product) {
        String sql = """
                UPDATE products
                SET name = ?, description = ?, price = ?, stock_quantity = ?,
                    category_id = ?, image_url = ?
                WHERE id = ? AND seller_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategoryId(),
                product.getImageUrl(),
                product.getId(),
                product.getSellerId()
        ) > 0;
    }

    public boolean updateAny(Product product) {
        String sql = """
                UPDATE products
                SET name = ?, description = ?, price = ?, stock_quantity = ?,
                    category_id = ?, image_url = ?
                WHERE id = ?
                """;

        return jdbcTemplate.update(
                sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategoryId(),
                product.getImageUrl(),
                product.getId()
        ) > 0;
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id) > 0;
    }

    public boolean deleteByIdAndSellerId(long id, long sellerId) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ? AND seller_id = ?", id, sellerId) > 0;
    }

    private void setProductParameters(PreparedStatement statement, Product product)
            throws SQLException {
        statement.setString(1, product.getName());
        statement.setString(2, product.getDescription());
        statement.setBigDecimal(3, product.getPrice());
        statement.setInt(4, product.getStockQuantity());
        statement.setObject(5, product.getCategoryId());
        statement.setString(6, product.getImageUrl());
        statement.setObject(7, product.getSellerId());
    }

    private Product mapRow(ResultSet rs, int rowNum)
            throws SQLException {

          Product product = new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getInt("stock_quantity"),
                rs.getObject("category_id", Long.class),
                rs.getString("image_url")
        );
        product.setSellerId(rs.getObject("seller_id", Long.class));
        return product;
    }
}
