package com.group1.shopease.security;

import com.group1.shopease.service.LoginVerificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmailVerificationSuccessHandler implements AuthenticationSuccessHandler {
    public static final String PENDING_EMAIL = "LOGIN_VERIFICATION_EMAIL";
    public static final String PENDING_TARGET = "LOGIN_VERIFICATION_TARGET";
    private final LoginVerificationService verificationService;

    public EmailVerificationSuccessHandler(LoginVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        String target = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "/admin" : "/products";
        try {
            verificationService.sendCode(email);
            request.getSession(true).setAttribute(PENDING_EMAIL, email);
            request.getSession().setAttribute(PENDING_TARGET, target);
            SecurityContextHolder.clearContext();
            response.sendRedirect("/verify-login");
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            response.sendRedirect("/login?verificationDeliveryError=true");
        }
    }
}
