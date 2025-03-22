package com.example.ssauc.user.search.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(indexName = "search") // "search" 인덱스와 매핑
public class SearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String keyword;

    @Field(type = FieldType.Text) //  category 필드 추가
    private String category;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    // getCategory() 메서드 추가
    public String getCategory() {
        return category;
    }
}
