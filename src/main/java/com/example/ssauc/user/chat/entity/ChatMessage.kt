package com.example.ssauc.user.chat.entity

import com.example.ssauc.user.login.entity.Users
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChatMessage {
    @Id
    @Column(name = "message_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public var messageId: Long? = null

    // 어떤 채팅방의 메시지인지
    @JvmField
    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    public val chatRoom: ChatRoom? = null

    // 보낸 사람 (Users 엔티티)
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    public val sender: Users? = null

    // 메시지 내용
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    public var message: String? = null

    // 메시지 전송 시각
    @Column(name = "sent_at")
    public var sentAt: LocalDateTime = LocalDateTime.now()
}
