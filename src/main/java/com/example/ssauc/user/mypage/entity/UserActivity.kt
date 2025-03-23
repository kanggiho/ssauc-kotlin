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
@Table(name = "user_activity")
class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private var activityId: Long? = null

    // 사용자 활동
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private val user: Users? = null

    @Column(name = "monthly_trade_count")
    private var monthlyTradeCount: Long? = null

    @Column(name = "last_updated")
    private var lastUpdated: LocalDateTime? = null
}
