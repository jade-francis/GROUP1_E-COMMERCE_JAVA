package com.group1.shopease.controller;

import com.group1.shopease.model.User;
import com.group1.shopease.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        User registered = userService.register(user);
        registered.setPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }
    @GetMapping("/me")
    public String currentUser(java.security.Principal principal) { return principal.getName(); }
    @PostMapping("/seller-request")
    public User sellerRequest(java.security.Principal principal) { return userService.requestSeller(principal.getName()); }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetRequested> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.createPasswordResetToken(request.email());
        return ResponseEntity.ok(new PasswordResetRequested("If that email is registered, a reset link has been created."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetRequested> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.password());
        return ResponseEntity.ok(new PasswordResetRequested("Password reset successfully."));
    }

    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(String token, String password) {}
    public record PasswordResetRequested(String message) {}
}
