package com.example.ssauc.user.search.service

import com.example.ssauc.user.search.document.SearchDocument
import com.example.ssauc.user.search.repository.SearchKeywordRepository
import com.example.ssauc.user.search.repository.UserRecentSearchRepository
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.stereotype.Service
import java.util.function.Function
import java.util.stream.Collectors


@Service
class SearchService(
    private val elasticsearchOperations: ElasticsearchOperations,
    private val searchKeywordRepository: SearchKeywordRepository,
    private val userRecentSearchRepository: UserRecentSearchRepository
) {
    // ✅ Elasticsearch를 이용한 키워드 검색 기능
    fun searchByKeyword(keyword: String): List<SearchDocument> {
        val criteria = Criteria.where("keyword").contains(keyword)
        val query: Query = CriteriaQuery(criteria)
        return elasticsearchOperations.search(query, SearchDocument::class.java)
            .map { searchHit: SearchHit<SearchDocument?> -> searchHit.content }
            .toList()
    }

    // ✅ 1. `CriteriaQuery`를 활용한 카테고리별 검색
    fun searchByKeywordAndCategory(keyword: String, category: String): List<SearchDocument> {
        val criteria = Criteria("name").contains(keyword)
            .and(Criteria("category").`is`(category))

        val query: Query = CriteriaQuery(criteria)

        return elasticsearchOperations.search(query, SearchDocument::class.java)
            .map { searchHit: SearchHit<SearchDocument?> -> searchHit.content }
            .toList()
    }

    val categoryAggregations: Map<String?, Long>
        // ✅ 2. 카테고리 목록 및 개수 조회 (Aggregation 없이 직접 집계)
        get() {
            val query: Query = CriteriaQuery(Criteria())

            val documents = elasticsearchOperations.search(
                query,
                SearchDocument::class.java
            )
                .map { searchHit: SearchHit<SearchDocument?> -> searchHit.content }
                .toList()

            return documents.stream()
                .collect(
                    Collectors.groupingBy<SearchDocument, String?, Any, Long>(
                        Function { obj: SearchDocument -> obj.category }, Collectors.counting()
                    )
                )
        }

    // ------------------------------
    // ✅ 임시 인메모리 기반 최근 검색어 관리 (추후 DB 연동 가능)
    // ------------------------------
    private val recentSearches: MutableList<String> = ArrayList()

    fun saveRecentSearch(keyword: String) {
        recentSearches.remove(keyword)
        recentSearches.add(0, keyword)
        if (recentSearches.size > 10) {
            recentSearches.removeAt(recentSearches.size - 1)
        }
    }

    fun getRecentSearches(): List<String> {
        return ArrayList(recentSearches)
    }

    val popularSearches: List<String>
        // ------------------------------
        get() = listOf("맥북", "아이폰", "갤럭시", "PS5", "닌텐도")
}
