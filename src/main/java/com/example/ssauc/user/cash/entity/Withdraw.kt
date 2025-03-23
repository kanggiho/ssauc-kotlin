package com.example.ssauc.user.cash.entity

import com.example.ssauc.user.login.entity.Users
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "withdraw")
class Withdraw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdraw_id")
    var withdrawId: Long? = null

    // 환급 신청 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: Users? = null

    var amount: Long? = null
    var commission: Long? = null

    @Column(length = 255)
    var bank: String? = null

    @Column(length = 255)
    var account: String? = null

    @Column(name = "requested_at")
    var requestedAt: LocalDateTime? = null

    @Column(name = "withdraw_at")
    var withdrawAt: LocalDateTime? = null
}
