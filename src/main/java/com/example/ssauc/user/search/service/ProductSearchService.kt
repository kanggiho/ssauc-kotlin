package com.example.ssauc.user.search.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldSort
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.search.document.ProductDocument
import com.example.ssauc.user.search.repository.ProductSearchRepository
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Slf4j
@Service
@RequiredArgsConstructor
class ProductSearchService {
    private val productSearchRepository: ProductSearchRepository? = null
    private val elasticsearchOperations: ElasticsearchOperations? = null
    private val elasticsearchClient: ElasticsearchClient? = null

    /**
     * 상품 검색 (초성 및 ngram 기반)
     * 입력된 keyword를 사용해 ES에서 상품을 검색합니다.
     */
    fun searchProducts(keyword: String?): List<Product> {
        val query = NativeQuery.builder()
            .withQuery { q: Query.Builder ->
                q.bool { b: BoolQuery.Builder ->
                    b
                        .should { s: Query.Builder ->
                            s.match { m: MatchQuery.Builder ->
                                m.field(
                                    "name"
                                ).query(keyword)
                            }
                        }
                        .should { s: Query.Builder ->
                            s.match { m: MatchQuery.Builder ->
                                m.field(
                                    "name.ngram"
                                ).query(keyword)
                            }
                        }
                }
            }
            .build()

        val hits = elasticsearchOperations!!.search(query, ProductDocument::class.java)
        return hits.searchHits.stream()
            .map { hit: SearchHit<ProductDocument> -> convertToEntity(hit.content) }
            .collect(Collectors.toList())
    }

    /**
     * 자동완성 기능
     * prefix로 시작하는 상품 이름을 ES에서 검색하여 제안어 목록을 반환합니다.
     */
    fun getAutoCompleteSuggestions(prefix: String?): List<String> {
        val query = NativeQuery.builder()
            .withQuery { q: Query.Builder ->
                q.bool { b: BoolQuery.Builder ->
                    b
                        .should { s: Query.Builder ->
                            s.prefix { px: PrefixQuery.Builder ->
                                px.field(
                                    "suggest"
                                ).value(prefix)
                            }
                        }
                        .should { s: Query.Builder ->
                            s.match { m: MatchQuery.Builder ->
                                m.field(
                                    "name.ngram"
                                ).query(prefix)
                            }
                        }
                }
            }
            .build()

        val hits = elasticsearchOperations!!.search(query, ProductDocument::class.java)
        ProductSearchService.log.debug("자동완성 쿼리 결과 건수: {}", hits.totalHits)
        val suggestions = hits.stream()
            .map { hit: SearchHit<ProductDocument> -> hit.content.name }
            .distinct()
            .collect(Collectors.toList())
        ProductSearchService.log.debug("자동완성 결과: {}", suggestions)
        return suggestions
    }

    /**
     * 관리자 전용 상품 검색
     * ES에서 검색된 상품 문서를 그대로 반환합니다.
     */
    fun adminSearchProducts(keyword: String?): List<ProductDocument> {
        val query = NativeQuery.builder()
            .withQuery { q: Query.Builder ->
                q.bool { b: BoolQuery.Builder ->
                    b
                        .should { s: Query.Builder ->
                            s.match { m: MatchQuery.Builder ->
                                m.field(
                                    "name"
                                ).query(keyword)
                            }
                        }
                        .should { s: Query.Builder ->
                            s.match { m: MatchQuery.Builder ->
                                m.field(
                                    "category"
                                ).query(keyword)
                            }
                        }
                }
            }
            .build()
        val hits = elasticsearchOperations!!.search(query, ProductDocument::class.java)
        return hits.searchHits.stream()
            .map { hit: SearchHit<ProductDocument> -> hit.content }
            .collect(Collectors.toList())
    }

    /**
     * 필터, 정렬, 페이징을 지원하는 상품 검색
     * category, 가격 범위 등 조건에 따라 상품을 필터링합니다.
     */
    fun filterAndSortProducts(
        category: String?,
        minPrice: Long?,
        maxPrice: Long?,
        sortField: String?,
        sortOrder: String?,
        pageNum: Int,
        pageSize: Int
    ): List<Product> {
        val query = NativeQuery.builder()
            .withQuery { b: Query.Builder ->
                b.bool { bool: BoolQuery.Builder ->
                    // 카테고리 필터 조건
                    if (category != null && !category.isEmpty()) {
                        bool.must { m: Query.Builder ->
                            m.match { match: MatchQuery.Builder ->
                                match.field(
                                    "category"
                                ).query(category)
                            }
                        }
                    }
                    // 가격 범위 필터 조건
                    if (minPrice != null || maxPrice != null) {
                        // ********* 문제 발생 구간 *********
                        // 아래 range 쿼리 DSL에서 r.field("price") 메서드를 찾을 수 없어 오류가 발생합니다.
                        // 실제 사용 중인 클라이언트 버전에 맞게 DSL 구문을 수정해야 합니다.
                        // 예시에서는 해당 부분을 주석 처리하고, 필요 시 별도의 DSL 구현으로 대체하세요.
                        /*
                    bool.must(m -> m.range(r ->
                            r.field("price")
                             .gte(minPrice != null ? JsonData.of(minPrice) : null)
                             .lte(maxPrice != null ? JsonData.of(maxPrice) : null)
                    ));
                    */
                        // 임시: 가격 조건 없이 넘어갑니다.
                    }
                    bool
                }
            }
            .withSort { s: SortOptions.Builder ->
                s.field { f: FieldSort.Builder ->
                    f
                        .field(sortField)
                        .order(
                            if ("asc".equals(
                                    sortOrder,
                                    ignoreCase = true
                                )
                            ) SortOrder.Asc else SortOrder.Desc
                        )
                }
            }
            .withPageable(PageRequest.of(pageNum - 1, pageSize))
            .build()

        val hits = elasticsearchOperations!!.search(query, ProductDocument::class.java)
        return hits.searchHits.stream()
            .map { hit: SearchHit<ProductDocument> -> convertToEntity(hit.content) }
            .collect(Collectors.toList())
    }

    /**
     * 상품 클릭 시 조회수 증가 기능
     */
    fun incrementProductClick(productId: Long) {
        // ES에 저장된 상품 문서를 조회 후 viewCount 증가
        val doc = productSearchRepository!!.findById(productId.toString())
            .orElse(null)
        if (doc != null) {
            if (doc.viewCount == null) {
                doc.viewCount = 1L
            } else {
                doc.viewCount = doc.viewCount + 1
            }
            productSearchRepository.save(doc)
        }
    }

    /**
     * 검색어 분석 기능 (Elasticsearch analyze API 사용)
     */
    fun analyzeKeyword(analyzer: String?, text: String?): String {
        try {
            val response = elasticsearchClient!!.indices().analyze { a: AnalyzeRequest.Builder ->
                a
                    .analyzer(analyzer)
                    .text(text)
            }
            return response.tokens().stream()
                .map { token: AnalyzeToken -> token.token() }
                .collect(Collectors.joining(", "))
        } catch (e: Exception) {
            return "Error: " + e.message
        }
    }

    /**
     * ProductDocument를 Product 엔티티로 변환
     */
    private fun convertToEntity(doc: ProductDocument): Product {
        val p = Product()
        if (doc.productId != null) {
            p.productId = doc.productId.toLong()
        }
        p.name = doc.name
        p.description = doc.description
        p.price = doc.price
        return p
    }
}
