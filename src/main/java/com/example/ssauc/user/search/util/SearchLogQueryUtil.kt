package com.example.ssauc.user.search.util

import com.example.ssauc.user.search.document.RecentSearchDocument
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.Query
import java.util.stream.Collectors


class SearchLogQueryUtil(private val esOps: ElasticsearchOperations) {
    /**
     * 특정 userId의 최근 검색어를 최대 maxResults개 조회합니다.
     * - 'userId'는 Keyword 타입이므로 Criteria.where("userId").is(userId)를 사용합니다.
     * - 검색 결과는 'searchedAt' 필드를 기준으로 내림차순 정렬됩니다.
     */
    fun findRecentSearchKeywords(userId: String, maxResults: Int): List<String> {
        val criteria = Criteria.where("userId").`is`(userId)
        val query = CriteriaQuery(criteria)
        query.addSort<Query>(Sort.by("searchedAt").descending())
        query.setMaxResults(maxResults)

        return esOps.search(query, RecentSearchDocument::class.java)
            .searchHits.stream()
            .map { hit: SearchHit<RecentSearchDocument> -> hit.content.keyword }
            .collect(Collectors.toList())
    }
}
