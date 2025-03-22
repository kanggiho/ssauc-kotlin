package com.example.ssauc.user.product.repository;

import com.example.ssauc.user.product.entity.Category;
import com.example.ssauc.user.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    List<Category> findAll();
}