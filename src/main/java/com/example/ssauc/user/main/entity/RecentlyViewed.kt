package com.example.ssauc.user.main.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "recently_viewed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class RecentlyViewed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val recentlyId: Long? = null

    // 최근에 본 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private val user: Users? = null

    // 최근에 본 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private val product: Product? = null

    private val viewedAt: LocalDateTime? = null
}
