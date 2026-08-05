package com.group1.shopease.repository;

import com.group1.shopease.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

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

    public User findOrCreateGoogleUser(String name, String email, String passwordHash) {
        jdbcTemplate.update("""
                INSERT INTO users (name, email, password_hash, role)
                VALUES (?, ?, ?, 'CUSTOMER')
                ON CONFLICT (email) DO NOTHING
                """, name, email, passwordHash);
        return findByEmail(email).orElseThrow();
    }

    public void replaceLoginVerificationCode(long userId, String codeHash, LocalDateTime expiresAt) {
        jdbcTemplate.update("DELETE FROM login_verification_codes WHERE user_id = ?", userId);
        jdbcTemplate.update("INSERT INTO login_verification_codes (user_id, code_hash, expires_at) VALUES (?, ?, ?)",
                userId, codeHash, expiresAt);
    }

    public boolean consumeValidLoginVerificationCode(String email, String codeHash) {
        int updated = jdbcTemplate.update("""
                UPDATE login_verification_codes c
                SET used_at = CURRENT_TIMESTAMP
                FROM users u
                WHERE c.user_id = u.id
                  AND lower(u.email) = lower(?)
                  AND c.code_hash = ?
                  AND c.used_at IS NULL
                  AND c.expires_at > CURRENT_TIMESTAMP
                  AND c.attempts < 5
                """, email, codeHash);
        if (updated > 0) return true;
        jdbcTemplate.update("""
                UPDATE login_verification_codes c
                SET attempts = attempts + 1
                FROM users u
                WHERE c.user_id = u.id
                  AND lower(u.email) = lower(?)
                  AND c.used_at IS NULL
                  AND c.expires_at > CURRENT_TIMESTAMP
                """, email);
        return false;
    }

    public void upsertAdmin(String name, String email, String passwordHash) {
        jdbcTemplate.update("""
                INSERT INTO users (name, email, password_hash, role, seller_status)
                VALUES (?, ?, ?, 'ADMIN', 'NOT_SELLER')
                ON CONFLICT (email) DO UPDATE SET
                    name = EXCLUDED.name,
                    password_hash = EXCLUDED.password_hash,
                    role = 'ADMIN',
                    seller_status = 'NOT_SELLER'
                """, name, email.toLowerCase(), passwordHash);
    }

    public void replacePasswordResetToken(long userId, String tokenHash, LocalDateTime expiresAt) {
        jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE user_id = ?", userId);
        jdbcTemplate.update("INSERT INTO password_reset_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)",
                userId, tokenHash, expiresAt);
    }

    public Optional<Long> findUserIdByValidResetToken(String tokenHash) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT user_id FROM password_reset_tokens WHERE token_hash = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP",
                    Long.class, tokenHash));
        } catch (EmptyResultDataAccessException ex) { return Optional.empty(); }
    }

    public boolean resetPassword(String tokenHash, String passwordHash) {
        Optional<Long> userId = findUserIdByValidResetToken(tokenHash);
        if (userId.isEmpty()) return false;
        jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?", passwordHash, userId.get());
        jdbcTemplate.update("UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP WHERE token_hash = ?", tokenHash);
        return true;
    }

    public List<User> findBySellerRole() {
        String sql = """
                SELECT id, name, email, password_hash, role, seller_status, created_at
                FROM users
                WHERE role = 'SELLER'
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs));
    }

    public List<User> findAllUsers() {
        return jdbcTemplate.query("SELECT id, name, email, password_hash, role, seller_status, created_at FROM users ORDER BY id", (rs, rowNum) -> mapUser(rs));
    }

    public boolean deleteUser(long id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ? AND role <> 'ADMIN'", id) > 0;
    }

    public boolean revokeSellerRequest(long id) {
        return jdbcTemplate.update("UPDATE users SET role = 'CUSTOMER', seller_status = 'NOT_SELLER' WHERE id = ? AND role = 'SELLER' AND seller_status = 'PENDING'", id) > 0;
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
