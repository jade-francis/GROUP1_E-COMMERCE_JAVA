package com.shopease.db;

import java.util.List;

/**
 * CategoryDAO — the CONTRACT for interacting with category data.
 */
public interface CategoryDAO {
    List<Category> getAllCategories();
    Category getCategoryById(int id);
    Category getCategoryByName(String name);
    Category addCategory(String name, String description); // returns created Category (with id), or null if name taken
    boolean updateCategory(int id, String name, String description);
    boolean deleteCategory(int id);
}
