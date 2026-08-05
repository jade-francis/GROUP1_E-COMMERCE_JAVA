package com.group1.shopease.service;

import com.group1.shopease.model.User;
import com.group1.shopease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class LoginVerificationService {
    private static final int CODE_LIFETIME_MINUTES = 10;
    private final UserRepository users;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String fromAddress;

    public LoginVerificationService(UserRepository users, JavaMailSender mailSender,
                                    @Value("${shopease.mail.from:${spring.mail.username:no-reply@shopease.local}}") String fromAddress) {
        this.users = users;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendCode(String email) {
        User user = users.findByEmail(normalize(email))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        String code = "%06d".formatted(secureRandom.nextInt(1_000_000));
        users.replaceLoginVerificationCode(user.getId(), hash(code), LocalDateTime.now().plusMinutes(CODE_LIFETIME_MINUTES));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Your ShopEase sign-in code");
        message.setText("Your ShopEase verification code is " + code + ".\n\nIt expires in "
                + CODE_LIFETIME_MINUTES + " minutes. If you did not try to sign in, you can ignore this email.");
        mailSender.send(message);
    }

    public boolean verify(String email, String code) {
        if (code == null || !code.matches("\\d{6}")) return false;
        return users.consumeValidLoginVerificationCode(normalize(email), hash(code));
    }

    private String normalize(String email) { return email.trim().toLowerCase(); }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
