package com.group1.shopease.controller;

import com.group1.shopease.model.Category;
import com.group1.shopease.model.Product;
import com.group1.shopease.model.User;
import com.group1.shopease.repository.CategoryRepository;
import com.group1.shopease.service.ProductService;
import com.group1.shopease.service.ImageStorageService;
import com.group1.shopease.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/seller/products")
public class SellerProductController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;

    public SellerProductController(ProductService productService, UserService userService, CategoryRepository categoryRepository,
                                   ImageStorageService imageStorageService) {
        this.productService = productService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.imageStorageService = imageStorageService;
    }

    private User getCurrentSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public String listProducts(Model model) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        List<Product> products = productService.findBySellerId(seller.getId());
        model.addAttribute("products", products);
        model.addAttribute("categoryNames", categoryRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Category::id, Category::name)));
        return "seller/products/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", false);
        return "seller/products/form";
    }

    @PostMapping
    public String createProduct(@Valid @ModelAttribute("product") Product product,
                                BindingResult result,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", false);
            return "seller/products/form";
        }
        
        try {
            String uploadedImageUrl = imageStorageService.storeProductImage(imageFile);
            if (uploadedImageUrl != null) product.setImageUrl(uploadedImageUrl);
            productService.create(product, seller.getEmail());
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            return "redirect:/seller/products";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/products/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        try {
            Product product = productService.findById(id);
            if (product.getSellerId() == null || product.getSellerId() != seller.getId()) {
                redirectAttributes.addFlashAttribute("error", "Product not found or access denied");
                return "redirect:/seller/products";
            }
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", true);
            return "seller/products/form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/seller/products";
        }
    }

    @PostMapping("/{id}")
    public String updateProduct(@PathVariable long id,
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult result,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", true);
            return "seller/products/form";
        }
        
        try {
            String uploadedImageUrl = imageStorageService.storeProductImage(imageFile);
            if (uploadedImageUrl != null) product.setImageUrl(uploadedImageUrl);
            productService.update(id, product, seller.getEmail());
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
            return "redirect:/seller/products";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/products/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable long id, RedirectAttributes redirectAttributes) {
        User seller = getCurrentSeller();
        if (!"SELLER".equals(seller.getRole()) || !"APPROVED".equals(seller.getSellerStatus())) {
            return "redirect:/profile";
        }
        
        try {
            productService.delete(id, seller.getEmail());
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/products";
    }
}
