package com.example.ssauc.user.order.service

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.ProductRepository
import lombok.AllArgsConstructor
import org.springframework.stereotype.Service

@Service
@AllArgsConstructor
class OrderService {
    private val productRepository: ProductRepository? = null
    private val usersRepository: UsersRepository? = null

    fun getProductById(productId: Long): Product {
        return productRepository!!.findById(productId).orElseThrow()
    }

    fun getUserById(userId: Long): Users {
        return usersRepository!!.findById(userId).orElseThrow()!!
    }
}
