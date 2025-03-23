package com.example.ssauc.user.chat.entity

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
@Table(name = "ban")
class Ban(// 차단한 사용자
    @ // FK: user_id
    field: JoinColumn(name = "user_id", nullable = false)
@field:ManyToOne
private val user: Users, blockedUser: Users, blockedAt: java.time.LocalDateTime)
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ban_id")
    private var banId: Long? = null // 차단 PK

    // 차단 당한 사용자
    @ManyToOne @JoinColumn(name = "blocked_user_id", nullable = false)
    private val blockedUser: Users // FK: blocked_user_id


    //차단시간
    @Column(name = "blocked_at")
    private var blockedAt: LocalDateTime

    //상태 (1이면 차단중, 0이면 차단해제중)
    @Column(name = "status")
    private var status = 0


    // user, blockedUser, blockedAt 을 받는 생성자
    init {
        this.user = user
        this.blockedUser = blockedUser
        this.blockedAt = blockedAt
    }
}
