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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        List<User> pendingSellers = userService.pendingSellers();
        List<User> allSellers = userService.allSellers();
        
        model.addAttribute("pendingSellers", pendingSellers);
        model.addAttribute("allSellers", allSellers);
        return "admin/sellers";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approve(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            userService.approveSeller(id);
            redirectAttributes.addFlashAttribute("success", "Seller approved successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/suspend")
    public String suspend(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            userService.suspendSeller(id);
            redirectAttributes.addFlashAttribute("success", "Seller suspended successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/revoke")
    public String revoke(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            if (userService.revokeSellerRequest(id)) {
                redirectAttributes.addFlashAttribute("success", "Seller request revoked. User is now a customer.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Cannot revoke: request not pending or user not a seller.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sellers";
    }
}