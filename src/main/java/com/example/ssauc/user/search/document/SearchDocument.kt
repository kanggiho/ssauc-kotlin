package com.example.ssauc.user.search.document

import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime

@Getter
@Setter
@Document(indexName = "search") // "search" 인덱스와 매핑
class SearchDocument {
    @Id
    private val id: String? = null

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private val keyword: String? = null

    // getCategory() 메서드 추가
    @Field(type = FieldType.Text) //  category 필드 추가
    val category: String? = null

    @Field(type = FieldType.Date)
    private val createdAt: LocalDateTime? = null
}
