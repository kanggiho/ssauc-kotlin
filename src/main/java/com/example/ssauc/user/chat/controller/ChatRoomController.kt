package com.example.ssauc.user.chat.controller

import com.example.ssauc.user.chat.dto.ChatMessageResponse
import com.example.ssauc.user.chat.dto.ChatRoomDto
import com.example.ssauc.user.chat.entity.ChatMessage
import com.example.ssauc.user.chat.service.ChatService
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
class ChatRoomController {
    private val chatService: ChatService? = null


    /**
     * 채팅방 생성
     * 프론트에서 productId를 받고, buyerId는 JWT에서 얻어온다고 가정
     */
    @PostMapping("/rooms")
    fun createRoom(
        @RequestParam productId: Long?,
        @RequestParam buyerId: Long? // 실제로는 SecurityContext 등에서 얻거나 PathVariable로 받을 수도
    ): ResponseEntity<*> {
        val created = chatService!!.createChatRoom(productId, buyerId)
        println("🔍 [DEBUG] 생성된 채팅방 정보: $created") // 👈 디버깅용 로그 추가

        val response = HashMap<String, Any?>()
        response["chatRoomId"] = created.chatRoomId
        response["productId"] = created.product.productId
        response["buyerId"] = created.buyer.userId


        return ResponseEntity.ok(response)
    }


    /**
     * userId가 참여 중인 모든 채팅방 리스트
     */
    //    @GetMapping("/userRooms")
    //    public List<ChatRoomDto> getUserRooms(@RequestParam Long userId) {
    //        // ChatService에서 buyer = userId OR product.seller = userId 인 Room
    //        return chatService.findChatRoomsByUserId(userId);
    //    }
    /**
     * 채팅방 내 메시지 목록 조회
     */
    @GetMapping("/rooms/{roomId}/messages")
    fun getMessages(@PathVariable roomId: Long?): ResponseEntity<List<ChatMessageResponse>> {
        val messages = chatService!!.findMessages(roomId)
        val result = messages.stream()
            .map { msg: ChatMessage? -> ChatMessageResponse.of(msg) }
            .collect(Collectors.toList())
        return ResponseEntity.ok(result)
    }

    /**
     * /api/chat/user/{userId}/rooms
     * -> userId가 buyer 또는 seller로 참여 중인 모든 채팅방 리스트
     */
    @GetMapping("/user/{userId}/rooms")
    fun getUserRooms(@PathVariable userId: Long?): List<ChatRoomDto> {
        // userId가 참여 중인 모든 채팅방(상품의 seller= userId, 또는 buyer= userId)을 조회
        return chatService!!.findChatRoomsByUserId(userId)
    }
}
