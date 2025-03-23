package com.example.ssauc.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsersStatusRequestDto {
    private Long userId;
    private String status;
}
