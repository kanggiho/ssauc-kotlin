package com.example.ssauc.user.main.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.main.entity.ProductLike;
import com.example.ssauc.user.main.repository.ProductLikeRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;

    @Transactional // ?
    public boolean toggleLike(Long userId, Long productId) {
        Users users = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        Optional<ProductLike> existingLike = productLikeRepository.findByUserAndProduct(users, product); // 좋아요 여부 체크

        if(existingLike.isPresent()) {
            // 이미 좋아요를 눌렀다면 취소
            productLikeRepository.delete(existingLike.get());
            productRepository.findById(productId).ifPresent(item -> {
                item.setLikeCount(item.getLikeCount() - 1);
                productRepository.save(item);
            });
            return false;
        } else {
            ProductLike newLike = ProductLike.builder().
                    user(users)
                    .product(product)
                    .likedAt(LocalDateTime.now())
                    .build();

            productLikeRepository.save(newLike);
            productRepository.findById(productId).ifPresent(item -> {
                item.setLikeCount(item.getLikeCount() + 1);
                productRepository.save(item);
            });

            return true;
        }
    }
}