package com.group1.shopease.service;

import com.group1.shopease.model.User;
import com.group1.shopease.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) { this.repository = repository; this.passwordEncoder = passwordEncoder; }
    public User register(User user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) throw new IllegalArgumentException("Email is already registered");
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("CUSTOMER");
        return repository.save(user);
    }
    public User requestSeller(String email) {
        User user = repository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!repository.requestSeller(user.getId())) throw new IllegalArgumentException("User is already a seller or has a pending request");
        user.setRole("SELLER"); user.setSellerStatus("PENDING"); return user;
    }

    public long count() {
        return repository.count();
    }

    public List<User> pendingSellers() {
        return repository.findBySellerStatus("PENDING");
    }

    public User approveSeller(long id) {
        return changeSellerStatus(id, "APPROVED");
    }

    public User suspendSeller(long id) {
        return changeSellerStatus(id, "SUSPENDED");
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User findOrCreateGoogleUser(String name, String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> existing = repository.findByEmail(normalizedEmail);
        if (existing.isPresent()) return existing.get();
        String displayName = name == null || name.isBlank() ? normalizedEmail.substring(0, normalizedEmail.indexOf('@')) : name.trim();
        String unusablePassword = passwordEncoder.encode(UUID.randomUUID().toString() + UUID.randomUUID());
        return repository.findOrCreateGoogleUser(displayName, normalizedEmail, unusablePassword);
    }

    public List<User> allSellers() {
        return repository.findBySellerRole();
    }

    public boolean revokeSellerRequest(long id) {
        return repository.revokeSellerRequest(id);
    }

    public List<User> allUsers() { return repository.findAllUsers(); }

    public boolean deleteUser(long id) { return repository.deleteUser(id); }

    public Optional<String> createPasswordResetToken(String email) {
        return repository.findByEmail(email.trim().toLowerCase()).map(user -> {
            String token = UUID.randomUUID().toString() + UUID.randomUUID();
            repository.replacePasswordResetToken(user.getId(), hashToken(token), LocalDateTime.now().plusMinutes(30));
            return token;
        });
    }

    public boolean isPasswordResetTokenValid(String token) {
        return token != null && repository.findUserIdByValidResetToken(hashToken(token)).isPresent();
    }

    public void resetPassword(String token, String password) {
        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new IllegalArgumentException("Password must be between 8 and 100 characters");
        }
        if (token == null || !repository.resetPassword(hashToken(token), passwordEncoder.encode(password))) {
            throw new IllegalArgumentException("This password reset link is invalid or has expired");
        }
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private User changeSellerStatus(long id, String status) {
        if (!repository.updateSellerStatus(id, status)) {
            throw new IllegalArgumentException("Seller account was not found");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seller account was not found"));
        user.setPassword(null);
        return user;
    }
}
