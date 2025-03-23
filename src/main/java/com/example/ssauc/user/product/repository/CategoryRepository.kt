package com.example.ssauc.user.product.repository

import com.example.ssauc.user.product.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category?, Long?> {
    fun findByName(name: String?): Optional<Category?>
    override fun findAll(): List<Category?>
}