package com.group1.shopease.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.group1.shopease.service.UserService;
import com.group1.shopease.service.LoginVerificationService;
import com.group1.shopease.security.CustomUserDetailsService;
import com.group1.shopease.security.EmailVerificationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Controller
public class AuthViewController {
    private final UserService userService;
    private final LoginVerificationService verificationService;
    private final CustomUserDetailsService userDetailsService;

    public AuthViewController(UserService userService, LoginVerificationService verificationService,
                              CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/verify-login")
    public String verifyLogin(HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute(EmailVerificationSuccessHandler.PENDING_EMAIL);
        if (email == null) return "redirect:/login";
        model.addAttribute("maskedEmail", maskEmail(email));
        return "auth/verify-login";
    }

    @PostMapping("/verify-login")
    public String verifyLogin(@RequestParam String code, HttpServletRequest request, HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        String email = (String) request.getSession().getAttribute(EmailVerificationSuccessHandler.PENDING_EMAIL);
        if (email == null) return "redirect:/login";
        if (!verificationService.verify(email, code)) {
            redirectAttributes.addFlashAttribute("error", "The code is invalid, expired, or has reached its attempt limit.");
            return "redirect:/verify-login";
        }

        var details = userDetailsService.loadUserByUsername(email);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.changeSessionId();
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        String target = (String) request.getSession().getAttribute(EmailVerificationSuccessHandler.PENDING_TARGET);
        request.getSession().removeAttribute(EmailVerificationSuccessHandler.PENDING_EMAIL);
        request.getSession().removeAttribute(EmailVerificationSuccessHandler.PENDING_TARGET);
        return "redirect:" + (target == null ? "/products" : target);
    }

    @PostMapping("/verify-login/resend")
    public String resendLoginCode(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String email = (String) request.getSession().getAttribute(EmailVerificationSuccessHandler.PENDING_EMAIL);
        if (email == null) return "redirect:/login";
        try {
            verificationService.sendCode(email);
            redirectAttributes.addFlashAttribute("message", "A new code has been sent.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "The email could not be sent. Please try again shortly.");
        }
        return "redirect:/verify-login";
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at);
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
