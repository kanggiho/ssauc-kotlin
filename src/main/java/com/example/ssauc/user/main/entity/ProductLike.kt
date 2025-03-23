package com.example.ssauc.user.main.entity;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "product_like")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    // 좋아요 한 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 좋아요 한 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private LocalDateTime likedAt;

    public ProductLike(Users user, Product product) {
        this.user = user;
        this.product = product;
        this.likedAt = LocalDateTime.now();
    }
}
