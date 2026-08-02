package com.group1.shopease.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class User {
    private Long id;
    @NotBlank @Size(max = 100) private String name;
    @NotBlank @Email @Size(max = 150) private String email;
    @NotBlank @Size(min = 8, max = 100) private String password;
    private String role = "CUSTOMER";
    private String sellerStatus = "NOT_SELLER";
    private LocalDateTime createdAt;

    public User() {}
    public User(Long id, String name, String email, String password, String role) {
        this.id = id; this.name = name; this.email = email; this.password = password; this.role = role;
    }
    public String getSellerStatus() { return sellerStatus; }
    public void setSellerStatus(String sellerStatus) { this.sellerStatus = sellerStatus; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
