package com.example.ssauc.user.search.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.ToString
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "search_keyword")
@Getter
@Setter
@NoArgsConstructor
@ToString
class SearchKeyword // 🔥 기본 생성자 추가 (에러 방지)
    (
    @field:Column(
        nullable = false,
        unique = true,
        length = 255
    ) @field:Id private var keyword: String,
    @field:Column(nullable = false) private var searchCount: Int,
    @field:UpdateTimestamp private val lastSearched: LocalDateTime
)
