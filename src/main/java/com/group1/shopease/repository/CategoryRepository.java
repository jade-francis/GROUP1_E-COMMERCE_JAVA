package com.group1.shopease.repository;
import com.group1.shopease.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository public class CategoryRepository {
 private final JdbcTemplate jdbc; public CategoryRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public List<Category> findAll(){return jdbc.query("SELECT id,name,description FROM categories ORDER BY name",(rs,n)->new Category(rs.getLong("id"),rs.getString("name"),rs.getString("description")));}
}
