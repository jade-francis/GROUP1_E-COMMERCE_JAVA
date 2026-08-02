package com.group1.shopease.controller;

import com.group1.shopease.model.User;
import com.group1.shopease.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller")
public class SellerController {

    private final UserService userService;

    public SellerController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/request")
    public String showRequestForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Redirect based on current status
        if ("SELLER".equals(user.getRole())) {
            if ("PENDING".equals(user.getSellerStatus())) {
                return "redirect:/profile?status=pending";
            } else if ("APPROVED".equals(user.getSellerStatus())) {
                return "redirect:/seller/dashboard";
            } else if ("SUSPENDED".equals(user.getSellerStatus())) {
                return "redirect:/profile?status=suspended";
            }
        }
        
        model.addAttribute("user", user);
        return "seller/request";
    }

    @PostMapping("/request")
    public String submitRequest(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        try {
            User updated = userService.requestSeller(auth.getName());
            redirectAttributes.addFlashAttribute("success", "Seller request submitted! Your account is now pending admin approval.");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/request";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!"SELLER".equals(user.getRole()) || !"APPROVED".equals(user.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        model.addAttribute("user", user);
        return "seller/dashboard";
    }
}