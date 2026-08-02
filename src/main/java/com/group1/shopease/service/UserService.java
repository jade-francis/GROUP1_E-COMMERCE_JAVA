package com.group1.shopease.service;

import com.group1.shopease.model.User;
import com.group1.shopease.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

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

    public List<User> pendingSellers() {
        return repository.findBySellerStatus("PENDING");
    }

    public User approveSeller(long id) {
        return changeSellerStatus(id, "APPROVED");
    }

    public User suspendSeller(long id) {
        return changeSellerStatus(id, "SUSPENDED");
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
