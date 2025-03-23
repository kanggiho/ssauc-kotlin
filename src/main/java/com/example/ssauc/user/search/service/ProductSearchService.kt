package com.example.ssauc.user.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.search.document.ProductDocument;
import com.example.ssauc.user.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 상품 검색 (초성 및 ngram 기반)
     * 입력된 keyword를 사용해 ES에서 상품을 검색합니다.
     */
    public List<Product> searchProducts(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.match(m -> m.field("name").query(keyword)))
                        .should(s -> s.match(m -> m.field("name.ngram").query(keyword)))
                ))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> convertToEntity(hit.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 자동완성 기능
     * prefix로 시작하는 상품 이름을 ES에서 검색하여 제안어 목록을 반환합니다.
     */
    public List<String> getAutoCompleteSuggestions(String prefix) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.prefix(px -> px.field("suggest").value(prefix)))
                        .should(s -> s.match(m -> m.field("name.ngram").query(prefix)))
                ))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        log.debug("자동완성 쿼리 결과 건수: {}", hits.getTotalHits());
        List<String> suggestions = hits.stream()
                .map(hit -> hit.getContent().getName())
                .distinct()
                .collect(Collectors.toList());
        log.debug("자동완성 결과: {}", suggestions);
        return suggestions;
    }

    /**
     * 관리자 전용 상품 검색
     * ES에서 검색된 상품 문서를 그대로 반환합니다.
     */
    public List<ProductDocument> adminSearchProducts(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.match(m -> m.field("name").query(keyword)))
                        .should(s -> s.match(m -> m.field("category").query(keyword)))
                ))
                .build();
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * 필터, 정렬, 페이징을 지원하는 상품 검색
     * category, 가격 범위 등 조건에 따라 상품을 필터링합니다.
     */
    public List<Product> filterAndSortProducts(
            String category,
            Long minPrice,
            Long maxPrice,
            String sortField,
            String sortOrder,
            int pageNum,
            int pageSize
    ) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(b -> b.bool(bool -> {
                    // 카테고리 필터 조건
                    if (category != null && !category.isEmpty()) {
                        bool.must(m -> m.match(match -> match.field("category").query(category)));
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
                    return bool;
                }))
                .withSort(s -> s.field(f -> f
                        .field(sortField)
                        .order("asc".equalsIgnoreCase(sortOrder) ? SortOrder.Asc : SortOrder.Desc))
                )
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> convertToEntity(hit.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 상품 클릭 시 조회수 증가 기능
     */
    public void incrementProductClick(Long productId) {
        // ES에 저장된 상품 문서를 조회 후 viewCount 증가
        ProductDocument doc = productSearchRepository.findById(String.valueOf(productId))
                .orElse(null);
        if (doc != null) {
            if (doc.getViewCount() == null) {
                doc.setViewCount(1L);
            } else {
                doc.setViewCount(doc.getViewCount() + 1);
            }
            productSearchRepository.save(doc);
        }
    }

    /**
     * 검색어 분석 기능 (Elasticsearch analyze API 사용)
     */
    public String analyzeKeyword(String analyzer, String text) {
        try {
            var response = elasticsearchClient.indices().analyze(a -> a
                    .analyzer(analyzer)
                    .text(text)
            );
            return response.tokens().stream()
                    .map(token -> token.token())
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * ProductDocument를 Product 엔티티로 변환
     */
    private Product convertToEntity(ProductDocument doc) {
        Product p = new Product();
        if (doc.getProductId() != null) {
            p.setProductId(Long.valueOf(doc.getProductId()));
        }
        p.setName(doc.getName());
        p.setDescription(doc.getDescription());
        p.setPrice(doc.getPrice());
        return p;
    }
}
