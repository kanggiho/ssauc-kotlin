package com.example.ssauc.user.main.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "product_like")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ProductLike(// 좋아요 한 사용자
    @field:JoinColumn(name = "user_id", nullable = false) @field:ManyToOne private val user: Users, // 좋아요 한 상품
    @field:JoinColumn(name = "product_id", nullable = false) @field:ManyToOne private val product: Product
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val likeId: Long? = null

    private val likedAt: LocalDateTime = LocalDateTime.now()
}
