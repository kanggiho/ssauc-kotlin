package com.example.ssauc.user.mypage.entity

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
@Table(name = "reputation_history")
class ReputationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private var historyId: Long? = null

    // 사용자 평판
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private val user: Users? = null

    @Column(name = "change_type", length = 50)
    private var changeType: String? = null

    private val changeAmount: Double? = null
    private val newScore: Double? = null

    @Column(name = "created_at")
    private var createdAt: LocalDateTime? = null
}