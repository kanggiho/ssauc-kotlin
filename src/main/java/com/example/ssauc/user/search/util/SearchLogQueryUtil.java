package com.example.ssauc.user.search.util;


import com.example.ssauc.user.search.document.RecentSearchDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.List;
import java.util.stream.Collectors;

public class SearchLogQueryUtil {

    private final ElasticsearchOperations esOps;

    public SearchLogQueryUtil(ElasticsearchOperations esOps) {
        this.esOps = esOps;
    }

    /**
     * 특정 userId의 최근 검색어를 최대 maxResults개 조회합니다.
     * - 'userId'는 Keyword 타입이므로 Criteria.where("userId").is(userId)를 사용합니다.
     * - 검색 결과는 'searchedAt' 필드를 기준으로 내림차순 정렬됩니다.
     */
    public List<String> findRecentSearchKeywords(String userId, int maxResults) {
        Criteria criteria = Criteria.where("userId").is(userId);
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.addSort(Sort.by("searchedAt").descending());
        query.setMaxResults(maxResults);

        return esOps.search(query, RecentSearchDocument.class)
                .getSearchHits().stream()
                .map(hit -> hit.getContent().getKeyword())
                .collect(Collectors.toList());
    }
}
