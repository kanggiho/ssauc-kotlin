package com.example.ssauc.user.chat.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    public var chatRoomId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    public val product: Product? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    public val buyer: Users? = null

    @Column(name = "created_at", nullable = false)
    public var createdAt: LocalDateTime = LocalDateTime.now()

    // 채팅 메시지 목록
    @OneToMany(mappedBy = "chatRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val messages: List<ChatMessage>? = null

    companion object {
        @JvmStatic
        fun createChatRoom(product: Product?, buyer: Users?): ChatRoom {
            val chatRoom = ChatRoom()
            chatRoom.setProduct(product)
            chatRoom.setBuyer(buyer)
            chatRoom.setCreatedAt(LocalDateTime.now())
            return chatRoom
        }
    }
}
