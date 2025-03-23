package com.example.ssauc.user.product.entity

import com.example.ssauc.user.bid.entity.AutoBid
import com.example.ssauc.user.bid.entity.Bid
import com.example.ssauc.user.bid.entity.ProductReport
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.main.entity.ProductLike
import com.example.ssauc.user.main.entity.RecentlyViewed
import com.example.ssauc.user.order.entity.Orders
import jakarta.persistence.*
import lombok.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var productId: Long? = null

    // 판매자 정보
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    var seller: Users? = null

    // 카테고리 정보
    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null

    @Column(nullable = false, length = 200)
    var name: String? = null

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null // 상품 설명

    @Column(nullable = false)
    var price: Long? = null

    var tempPrice: Long? = null

    var startPrice: Long? = null

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null // 이미지 주소

    @Column(length = 50)
    var status: String? = null

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    // 수정 시간: 엔티티가 업데이트될 때 자동으로 시간 갱신
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null // 수정 시간
    var endAt: LocalDateTime? = null

    @Column(name = "view_count")
    var viewCount: Long? = null // 조회수

    @JvmField
    var dealType: Int = 0

    // 거래 유형 (0: 직거래, 1: 택배, 2: 둘 다 선택)
    @JvmField
    var bidCount: Int = 0
    var minIncrement: Int = 0
    @JvmField
    var likeCount: Int = 0


    // 연관 관계 설정
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var recentlyViewedProducts: List<RecentlyViewed>? = null

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var likedProducts: List<ProductLike>? = null

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ReportProducts: List<ProductReport>? = null

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bids: List<Bid>? = null

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var autoBids: List<AutoBid>? = null

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var orders: List<Orders>? = null
}