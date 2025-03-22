package com.example.ssauc.user.search.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Document(indexName = "search_log")
@Mapping(mappingPath = "elasticsearch/mappings/related_mapping.json")
public class SearchLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private List<String> keywords;

    @Field(type = FieldType.Date)
    private LocalDateTime searchedAt;
}
