package com.example.ssauc.user.bid.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_report")
class ProductReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public val reportId: Long? = null

    // 신고 할 상품
    @JvmField
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    public val product: Product? = null

    // 신고 한 사용자
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    public val reporter: Users? = null

    // 신고 당한 사용자
    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    public val reportedUser: Users? = null

    @Column(nullable = false, length = 255)
    public var reportReason: String? = null

    @Column(length = 50)
    public var status: String? = null

    @Column(columnDefinition = "TEXT")
    public var details: String? = null

    public val reportDate: LocalDateTime? = null
    public val processedAt: LocalDateTime? = null
}