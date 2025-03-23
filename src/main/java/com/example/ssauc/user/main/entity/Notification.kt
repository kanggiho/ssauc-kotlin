package com.example.ssauc.user.main.entity

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
@Table(name = "notification")
class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val notificationId: Long? = null

    // 알림 대상 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private val user: Users? = null

    @Column(nullable = false, length = 50)
    private var type: String? = null

    @Column(nullable = false, length = 255)
    private var message: String? = null

    private val createdAt: LocalDateTime? = null

    private val readStatus = 0
}
