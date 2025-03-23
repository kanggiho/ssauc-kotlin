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
@Table(name = "report")
class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public val reportId: Long? = null // 신고 아이디 (PK)

    // 신고 한 사용자
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    public val reporter: Users? = null // 신고자 아이디 (FK)

    // 신고 당한 사용자
    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    public val reportedUser: Users? = null // 피신고자 아이디

    @Column(nullable = false, length = 255)
    public var reportReason: String? = null // 신고 사유

    @Column(length = 50)
    public var status: String? = null // 처리 상태

    @Column(columnDefinition = "TEXT", nullable = false)
    public var details: String? = null // 신고 내용 (TEXT)


    public val reportDate: LocalDateTime? = null // 신고 시간
    public val processedAt: LocalDateTime? = null // 처리 시간
}
