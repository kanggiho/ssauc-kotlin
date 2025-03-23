package com.example.ssauc.user.order.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {

    private ProductRepository productRepository;
    private UsersRepository usersRepository;

    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow();
    }

    public Users getUserById(Long userId) {
        return usersRepository.findById(userId).orElseThrow();
    }


}
