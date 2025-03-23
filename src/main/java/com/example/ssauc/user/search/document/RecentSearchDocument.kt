package com.example.ssauc.user.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@Document(indexName = "recent_search")
@Mapping(mappingPath = "elasticsearch/mappings/recent_mapping.json")
public class RecentSearchDocument {

    @Id
    private String id; // 반드시 String 타입이어야 함

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String keyword;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime searchedAt;
}
