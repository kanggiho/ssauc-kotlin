package com.example.ssauc.user.search.repository

import com.example.ssauc.user.search.document.SearchLogDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository


@Repository
interface SearchLogRepository : ElasticsearchRepository<SearchLogDocument?, String?> {
    // unchanged
    fun findByKeywordsContaining(keyword: String?): List<SearchLogDocument?>?
}