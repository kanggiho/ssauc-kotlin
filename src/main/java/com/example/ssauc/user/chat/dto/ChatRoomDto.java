package com.example.ssauc.user.chat.dto;

import com.example.ssauc.user.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long chatRoomId;
    private String productName;
    private Long buyerId;
    private Long sellerId;


    private String productImage;    // ➡️ 상품 이미지 추가
    private Long productPrice;    // ➡️ 상품 가격 추가
    private String productStatus;   // ➡️ 상품 상태 추가


    private String otherUserName;
    private Long otherUserId;

    private boolean banned;



//    public static ChatRoomDto fromEntity(ChatRoom chatRoom) {
//        ChatRoomDto dto = new ChatRoomDto();
//        dto.setChatRoomId(chatRoom.getChatRoomId());
//        dto.setProductName(chatRoom.getProduct().getName());
//        dto.setBuyerId(chatRoom.getBuyer().getUserId());
//        dto.setSellerId(chatRoom.getProduct().getSeller().getUserId());
//        return dto;
//    }
}
