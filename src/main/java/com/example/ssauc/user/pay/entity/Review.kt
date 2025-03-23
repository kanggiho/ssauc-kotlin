package com.example.ssauc.user.pay.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.order.entity.Orders
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private var reviewId: Long? = null

    // 리뷰를 남긴 사용자
    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private val reviewer: Users? = null

    // 리뷰가 남겨진 사용자
    @ManyToOne
    @JoinColumn(name = "reviewee_id", nullable = false)
    private val reviewee: Users? = null

    // 주문이 완료된 거래
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private val order: Orders? = null

    // true: 긍정, false: 부정으로 가정하여 후에 각각 +0.5 또는 -0.5로 계산 가능
    @Column(name = "option1")
    private var option1: Boolean? = null

    @Column(name = "option2")
    private var option2: Boolean? = null

    @Column(name = "option3")
    private var option3: Boolean? = null

    @Column(name = "base_score")
    private var baseScore: Double? = null

    @Column(name = "comment", columnDefinition = "TEXT")
    private var comment: String? = null

    @Column(name = "created_at")
    private var createdAt: LocalDateTime? = null
}
