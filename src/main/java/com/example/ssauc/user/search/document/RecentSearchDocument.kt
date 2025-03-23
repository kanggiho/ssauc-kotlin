package com.example.ssauc.user.search.document

import lombok.Data
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Mapping
import java.time.LocalDateTime

@Data
@Document(indexName = "recent_search")
@Mapping(mappingPath = "elasticsearch/mappings/recent_mapping.json")
class RecentSearchDocument {
    @Id
    private val id: String? = null // 반드시 String 타입이어야 함

    @Field(type = FieldType.Keyword)
    private val userId: String? = null

    @Field(type = FieldType.Text, analyzer = "standard")
    private val keyword: String? = null

    @Field(type = FieldType.Date, format = [], pattern = ["yyyy-MM-dd'T'HH:mm:ss"])
    private val searchedAt: LocalDateTime? = null
}
