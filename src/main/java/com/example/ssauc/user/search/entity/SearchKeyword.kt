package com.example.ssauc.user.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_keyword")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchKeyword {

    @Id
    @Column(nullable = false, unique = true, length = 255)
    private String keyword;

    @Column(nullable = false)
    private int searchCount;

    @UpdateTimestamp
    private LocalDateTime lastSearched;

    // 🔥 기본 생성자 추가 (에러 방지)
    public SearchKeyword(String keyword, int searchCount, LocalDateTime lastSearched) {
        this.keyword = keyword;
        this.searchCount = searchCount;
        this.lastSearched = lastSearched;
    }
}
