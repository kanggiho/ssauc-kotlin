package com.example.ssauc.user.chat.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ChatRoomDto {
    private val chatRoomId: Long? = null
    private val productName: String? = null
    private val buyerId: Long? = null
    private val sellerId: Long? = null


    private val productImage: String? = null // ➡️ 상품 이미지 추가
    private val productPrice: Long? = null // ➡️ 상품 가격 추가
    private val productStatus: String? = null // ➡️ 상품 상태 추가


    private val otherUserName: String? = null
    private val otherUserId: Long? = null

    private val banned = false //    public static ChatRoomDto fromEntity(ChatRoom chatRoom) {
    //        ChatRoomDto dto = new ChatRoomDto();
    //        dto.setChatRoomId(chatRoom.getChatRoomId());
    //        dto.setProductName(chatRoom.getProduct().getName());
    //        dto.setBuyerId(chatRoom.getBuyer().getUserId());
    //        dto.setSellerId(chatRoom.getProduct().getSeller().getUserId());
    //        return dto;
    //    }
}
