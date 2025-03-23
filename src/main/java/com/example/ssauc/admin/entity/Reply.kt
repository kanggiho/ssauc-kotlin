package com.example.ssauc.admin.entity

import com.example.ssauc.user.contact.entity.Board
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "reply")
class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    public var replyId: Long? = null

    @JvmField
    @OneToOne
    @JoinColumn(name = "board_id", nullable = false)
    public val board: Board? = null

    @ManyToOne
    @JoinColumn(name = "replier_id", nullable = false)
    public val admin: Admin? = null

    @Column(name = "subject", nullable = false, length = 200)
    public var subject: String? = null

    @Column(name = "message", columnDefinition = "TEXT")
    public var message: String? = null

    @Column(name = "complete_at")
    public var completeAt: LocalDateTime? = null
}
