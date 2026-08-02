package com.group1.shopease.controller;

import com.group1.shopease.model.User;
import com.group1.shopease.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    private final UserService userService;

    public AdminViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/sellers")
    public String sellers(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        
        List<User> sellers = userService.pendingSellers();
        model.addAttribute("sellers", sellers);
        return "admin/sellers";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approve(@PathVariable long id) {
        userService.approveSeller(id);
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/suspend")
    public String suspend(@PathVariable long id) {
        userService.suspendSeller(id);
        return "redirect:/admin/sellers";
    }
}