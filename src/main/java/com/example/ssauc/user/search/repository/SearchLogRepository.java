package com.example.ssauc.user.search.repository;


import com.example.ssauc.user.search.document.SearchLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchLogRepository extends ElasticsearchRepository<SearchLogDocument, String> {

    // unchanged
    List<SearchLogDocument> findByKeywordsContaining(String keyword);
}