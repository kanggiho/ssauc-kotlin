package com.example.ssauc.user.pay.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.order.entity.Orders
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private var paymentId: Long? = null

    // 결제 요청 주문
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private val order: Orders? = null

    // 결제 하는 사용자
    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private val payer: Users? = null

    @Column(name = "amount")
    private var amount: Long? = null

    @Column(name = "payment_method", length = 50)
    private var paymentMethod: String? = null

    @Column(name = "payment_status", length = 50)
    private var paymentStatus: String? = null

    @Column(name = "payment_date")
    private var paymentDate: LocalDateTime? = null

    @Column(name = "payment_number")
    private var paymentNumber: String? = null
}
