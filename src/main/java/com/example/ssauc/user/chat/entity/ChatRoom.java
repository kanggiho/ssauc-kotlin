package com.example.ssauc.user.chat.entity;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Users buyer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 채팅 메시지 목록
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    public static ChatRoom createChatRoom(Product product, Users buyer) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setProduct(product);
        chatRoom.setBuyer(buyer);
        chatRoom.setCreatedAt(LocalDateTime.now());
        return chatRoom;
    }
}
