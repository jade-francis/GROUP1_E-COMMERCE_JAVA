package com.group1.shopease.config;

import com.group1.shopease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminAccountInitializer(UserRepository users, PasswordEncoder passwordEncoder,
                                   @Value("${shopease.admin.email:shopeeaseadmin@gmail.com}") String email,
                                   @Value("${shopease.admin.password:ohlatina}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        users.upsertAdmin("Shopease Admin", email, passwordEncoder.encode(password));
    }
}
