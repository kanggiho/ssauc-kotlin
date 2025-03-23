package com.example.ssauc.user.bid.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "bid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public val bidId: Long? = null

    // 입찰한 상품
    @JvmField
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    public val product: Product? = null

    // 입찰한 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public val user: Users? = null

    @Column(nullable = false)
    public var bidPrice: Long? = null

    @Column(nullable = false)
    public var bidTime: LocalDateTime? = null
}
