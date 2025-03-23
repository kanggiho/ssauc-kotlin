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
@Table(name = "charge")
class Charge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    public var chargeId: Long? = null

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public val user: Users? = null

    @Column(name = "imp_uid", length = 255)
    public var impUid: String? = null

    @Column(name = "charge_type", length = 50)
    public var chargeType: String? = null

    public val amount: Long? = null

    @Column(name = "status", length = 50)
    public var status: String? = null

    @Column(columnDefinition = "TEXT")
    public var details: String? = null

    @Column(name = "receipt_url", length = 255)
    public var receiptUrl: String? = null

    @Column(name = "created_at")
    public var createdAt: LocalDateTime? = null
}

