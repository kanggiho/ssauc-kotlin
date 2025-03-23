package com.example.ssauc.user.search.document

import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Mapping
import java.time.LocalDateTime

@Getter
@Setter
@Document(indexName = "search_log")
@Mapping(mappingPath = "elasticsearch/mappings/related_mapping.json")
class SearchLogDocument {
    @Id
    private val id: String? = null

    @Field(type = FieldType.Keyword)
    private val keywords: List<String>? = null

    @Field(type = FieldType.Date)
    private val searchedAt: LocalDateTime? = null
}
