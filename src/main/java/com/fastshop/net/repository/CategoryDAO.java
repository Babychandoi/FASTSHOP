package com.fastshop.net.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fastshop.net.model.Category;
import org.springframework.data.jpa.repository.Query;

public interface CategoryDAO extends JpaRepository<Category, String> {
    Boolean getStatusById(String id);
    List<Category> findByStatus(Boolean status);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();

}
