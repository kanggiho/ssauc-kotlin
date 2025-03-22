package com.example.ssauc.user.product.entity;

import com.example.ssauc.user.bid.entity.AutoBid;
import com.example.ssauc.user.bid.entity.Bid;
import com.example.ssauc.user.bid.entity.ProductReport;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.main.entity.ProductLike;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.order.entity.Orders;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    // 판매자 정보
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Users seller;

    // 카테고리 정보
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;        // 상품 설명

    @Column(nullable = false)
    private Long price;

    private Long tempPrice;

    private Long startPrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;           // 이미지 주소

    @Column(length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 수정 시간: 엔티티가 업데이트될 때 자동으로 시간 갱신
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;   // 수정 시간
    private LocalDateTime endAt;

    @Column(name = "view_count")
    private Long viewCount;            // 조회수

    private int dealType;

    // 거래 유형 (0: 직거래, 1: 택배, 2: 둘 다 선택)
    private int bidCount;
    private int minIncrement;
    private int likeCount;

    public Integer getDealType() { return this.dealType; }
    public Integer getBidCount() { return this.bidCount; }
    public Integer getLikeCount() { return this.likeCount; }



    // 연관 관계 설정

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentlyViewed> recentlyViewedProducts;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductLike> likedProducts;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductReport> ReportProducts;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutoBid> autoBids;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders;

}