package com.group1.shopease.controller;

import com.group1.shopease.model.User;
import com.group1.shopease.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sellers")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/pending")
    public List<User> pendingSellers() {
        List<User> sellers = userService.pendingSellers();
        sellers.forEach(user -> user.setPassword(null));
        return sellers;
    }

    @PostMapping("/{id}/approve")
    public User approve(@PathVariable long id) {
        return userService.approveSeller(id);
    }

    @PostMapping("/{id}/suspend")
    public User suspend(@PathVariable long id) {
        return userService.suspendSeller(id);
    }
}
