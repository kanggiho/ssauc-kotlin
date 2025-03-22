package com.example.ssauc.admin.dto;

import com.example.ssauc.admin.entity.Admin;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyDto {
    private long boardId;
    private String title;
    private String content;
    private Admin admin;
}
