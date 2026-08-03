package com.group1.shopease.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.group1.shopease.service.UserService;

@Controller
public class AuthViewController {
    private final UserService userService;

    public AuthViewController(UserService userService) { this.userService = userService; }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "auth/forgot-password"; }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        userService.createPasswordResetToken(email).ifPresentOrElse(token -> {
            redirectAttributes.addFlashAttribute("resetToken", token);
            redirectAttributes.addFlashAttribute("resetUrl", "/reset-password?token=" + token);
            redirectAttributes.addFlashAttribute("message", "Email verified. Use the token below to reset your password.");
        }, () -> redirectAttributes.addFlashAttribute("error", "No ShopEase account was found with that email address."));
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("validToken", userService.isPasswordResetTokenValid(token));
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password,
                                @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/reset-password?token=" + token;
        }
        try {
            userService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("passwordReset", true);
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}
