package com.example.ssauc.user.bid.entity;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "bid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;

    // 입찰한 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 입찰한 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private Long bidPrice;

    @Column(nullable = false)
    private LocalDateTime bidTime;

}
