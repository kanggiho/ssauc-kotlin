package com.example.ssauc.admin.dto

import com.example.ssauc.admin.entity.Admin
import lombok.*

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ReplyDto {
    public val boardId: Long = 0
    public val title: String? = null
    public val content: String? = null
    public val admin: Admin? = null
}
