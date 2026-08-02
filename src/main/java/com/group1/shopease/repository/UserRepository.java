package com.group1.shopease.repository;

import com.group1.shopease.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    public UserRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Optional<User> findByEmail(String email) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, name, email, password_hash, role, seller_status, created_at FROM users WHERE lower(email) = lower(?)",
                    (rs, row) -> {
                        User user = new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("password_hash"), rs.getString("role"));
                        user.setSellerStatus(rs.getString("seller_status"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        return user;
                    }, email));
        } catch (EmptyResultDataAccessException ex) { return Optional.empty(); }
    }

    public boolean requestSeller(long id) {
        return jdbcTemplate.update("UPDATE users SET role = 'SELLER', seller_status = 'PENDING' WHERE id = ? AND role = 'CUSTOMER'", id) > 0;
    }

    public List<User> findBySellerStatus(String status) {
        return jdbcTemplate.query(
                "SELECT id, name, email, password_hash, role, seller_status, created_at FROM users WHERE seller_status = ? ORDER BY id",
                (rs, row) -> mapUser(rs),
                status
        );
    }

    public Optional<User> findById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, name, email, password_hash, role, seller_status, created_at FROM users WHERE id = ?",
                    (rs, row) -> mapUser(rs), id));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public boolean updateSellerStatus(long id, String status) {
        return jdbcTemplate.update(
                "UPDATE users SET seller_status = ? WHERE id = ? AND role = 'SELLER'",
                status,
                id
        ) > 0;
    }

    public long count() {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return total == null ? 0 : total;
    }

    public User save(User user) {
        Long id = jdbcTemplate.queryForObject("INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?) RETURNING id", Long.class, user.getName(), user.getEmail(), user.getPassword(), user.getRole());
        user.setId(id); return user;
    }

    private User mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        User user = new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role")
        );
        user.setSellerStatus(rs.getString("seller_status"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}