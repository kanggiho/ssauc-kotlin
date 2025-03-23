package com.example.ssauc.user.chat.service

import com.example.ssauc.user.chat.dto.ChatRoomDto
import com.example.ssauc.user.chat.entity.ChatMessage
import com.example.ssauc.user.chat.entity.ChatRoom
import com.example.ssauc.user.chat.entity.ChatRoom.Companion.createChatRoom
import com.example.ssauc.user.chat.repository.BanRepository
import com.example.ssauc.user.chat.repository.ChatMessageRepository
import com.example.ssauc.user.chat.repository.ChatRoomRepository
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.main.entity.Notification
import com.example.ssauc.user.main.repository.NotificationRepository
import com.example.ssauc.user.product.repository.ProductRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
@Transactional
class ChatService {
    private val chatRoomRepository: ChatRoomRepository? = null
    private val chatMessageRepository: ChatMessageRepository? = null
    private val userRepository: UsersRepository? = null
    private val productRepository: ProductRepository? = null
    private val banRepository: BanRepository? = null
    private val usersRepository: UsersRepository? = null
    private val notificationRepository: NotificationRepository? = null

    /**
     * 채팅방 생성
     *
     * @param productId 상품 ID
     * @param buyerId   구매자 ID (JWT에서 파싱)
     */
    fun createChatRoom(productId: Long, buyerId: Long): ChatRoom {
        // 1️⃣ 기존 방이 있는지 확인
        val existingRoom = chatRoomRepository!!.findByProductProductIdAndBuyerUserId(productId, buyerId)

        // 2️⃣ 기존 방이 존재하면 해당 방 반환
        if (existingRoom!!.isPresent) {
            System.out.println(("✅ 기존 채팅방이 이미 존재합니다. (Room ID: " + existingRoom.get().getChatRoomId()).toString() + ")")
            return existingRoom.get()
        }

        val product = productRepository!!.findById(productId)
            .orElseThrow { IllegalArgumentException("상품이 존재하지 않습니다.") }
        val buyer = userRepository!!.findById(buyerId)
            .orElseThrow { IllegalArgumentException("구매자 정보가 없습니다.") }

        // 중복 방 체크 로직이 필요하다면 추가 (예: 이미 해당 buyer가 product에 대해 생성된 방이 있으면 재사용)
        val chatRoom = createChatRoom(product, buyer)
        return chatRoomRepository.save(chatRoom)
    }

    /**
     * 메시지 저장
     */
    fun saveMessage(chatRoomId: Long, senderId: Long, otherUserId: Long, message: String): ChatMessage {
        val chatRoom = chatRoomRepository!!.findById(chatRoomId)
            .orElseThrow { IllegalArgumentException("채팅방이 존재하지 않습니다.") }!!
        val sender = userRepository!!.findById(senderId)
            .orElseThrow { IllegalArgumentException("유저가 존재하지 않습니다.") }

        val chatMessage = ChatMessage()
        chatMessage.setChatRoom(chatRoom)
        chatMessage.setSender(sender)
        chatMessage.setMessage(message)
        chatMessage.setSentAt(LocalDateTime.now())

        chatNotice(senderId, otherUserId, message)


        return chatMessageRepository!!.save(chatMessage)
    }

    fun chatNotice(userId: Long, otherUserId: Long, message: String) {
        val notification = Notification.builder()
            .user(usersRepository!!.findById(otherUserId).orElseThrow())
            .type("채팅")
            .message(
                """
                ${usersRepository.findById(userId).orElseThrow().userName}님께서 메세지를 보냈습니다.
                $message
                """.trimIndent()
            )
            .createdAt(LocalDateTime.now())
            .readStatus(1)
            .build()
        notificationRepository!!.save(notification)
    }


    /**
     * userId가 참여 중인 모든 채팅방 조회 (buyer이거나 seller인 방)
     */
    @Transactional(readOnly = true)
    fun findChatRoomsByUserId(userId: Long): List<ChatRoomDto> {
        val rooms = chatRoomRepository!!.findByBuyerUserIdOrProductSellerUserId(userId, userId)
        // DTO 변환
        return rooms!!.stream()
            .map<ChatRoomDto> { room: ChatRoom? ->
                val buyerId: Long = room.getBuyer().getUserId()
                val sellerId: Long = room.getProduct().getSeller().getUserId()
                val buyerName: String = room.getBuyer().getUserName()
                val sellerName: String = room.getProduct().getSeller().getUserName()

                val dto = ChatRoomDto()
                dto.setChatRoomId(room.getChatRoomId())
                dto.setProductName(room.getProduct().getName())
                dto.setBuyerId(buyerId)
                dto.setSellerId(sellerId)
                dto.setProductImage(room.getProduct().getImageUrl())
                dto.setProductPrice(room.getProduct().getPrice())
                dto.setProductStatus(room.getProduct().getStatus())

                // 현재 로그인 사용자가 buyer라면, 상대방은 seller; 그렇지 않으면 buyer
                if (userId == buyerId) {
                    dto.setOtherUserName(sellerName)
                    dto.setOtherUserId(sellerId)
                } else {
                    dto.setOtherUserName(buyerName)
                    dto.setOtherUserId(buyerId)
                }

                // 양방향 차단 여부 확인: buyer가 seller를 차단했거나, seller가 buyer를 차단했으면 true
                val banned = banRepository!!.existsByUserAndBlockedUserAndStatus(
                    room.getBuyer(), room.getProduct().getSeller(), 1
                )
                        || banRepository.existsByUserAndBlockedUserAndStatus(
                    room.getProduct().getSeller(), room.getBuyer(), 1
                )
                dto.setBanned(banned)
                dto
            }
            .collect(Collectors.toList<ChatRoomDto>())
    }


    /**
     * 채팅방 메시지 목록 조회
     */
    @Transactional(readOnly = true)
    fun findMessages(chatRoomId: Long?): List<ChatMessage?>? {
        return chatMessageRepository!!.findByChatRoomChatRoomIdOrderBySentAtAsc(chatRoomId)
    }
}
