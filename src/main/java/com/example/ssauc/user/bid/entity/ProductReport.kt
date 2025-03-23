package com.example.ssauc.user.bid.entity;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_report")
public class ProductReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // 신고 할 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 신고 한 사용자
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private Users reporter;

    // 신고 당한 사용자
    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private Users reportedUser;

    @Column(nullable = false, length = 255)
    private String reportReason;

    @Column(length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime reportDate;
    private LocalDateTime processedAt;
}