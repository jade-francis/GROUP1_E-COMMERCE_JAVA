package com.group1.shopease.controller;
import com.group1.shopease.model.Category;
import com.group1.shopease.repository.CategoryRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/categories") public class CategoryController {
 private final CategoryRepository repository; public CategoryController(CategoryRepository repository){this.repository=repository;}
 @GetMapping public List<Category> findAll(){return repository.findAll();}
}
