package com.example.ssauc.user.chat.dto;

import com.example.ssauc.user.login.entity.Users;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private Long chatRoomId;
    private Long senderId;
    private Long otherUserId;
    private String message;
}