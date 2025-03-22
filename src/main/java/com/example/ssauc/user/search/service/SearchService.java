package com.example.ssauc.user.search.service;


import com.example.ssauc.user.search.document.SearchDocument;
import com.example.ssauc.user.search.repository.SearchKeywordRepository;
import com.example.ssauc.user.search.repository.UserRecentSearchRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchKeywordRepository searchKeywordRepository;
    private final UserRecentSearchRepository userRecentSearchRepository;

    public SearchService(ElasticsearchOperations elasticsearchOperations,
                         SearchKeywordRepository searchKeywordRepository,
                         UserRecentSearchRepository userRecentSearchRepository) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.searchKeywordRepository = searchKeywordRepository;
        this.userRecentSearchRepository = userRecentSearchRepository;
    }

    // ✅ Elasticsearch를 이용한 키워드 검색 기능
    public List<SearchDocument> searchByKeyword(String keyword) {
        Criteria criteria = Criteria.where("keyword").contains(keyword);
        Query query = new CriteriaQuery(criteria);
        return elasticsearchOperations.search(query, SearchDocument.class)
                .map(searchHit -> searchHit.getContent())
                .toList();
    }

    // ✅ 1. `CriteriaQuery`를 활용한 카테고리별 검색
    public List<SearchDocument> searchByKeywordAndCategory(String keyword, String category) {
        Criteria criteria = new Criteria("name").contains(keyword)
                .and(new Criteria("category").is(category));

        Query query = new CriteriaQuery(criteria);

        return elasticsearchOperations.search(query, SearchDocument.class)
                .map(searchHit -> searchHit.getContent())
                .toList();
    }

    // ✅ 2. 카테고리 목록 및 개수 조회 (Aggregation 없이 직접 집계)
    public Map<String, Long> getCategoryAggregations() {
        Query query = new CriteriaQuery(new Criteria());

        List<SearchDocument> documents = elasticsearchOperations.search(query, SearchDocument.class)
                .map(searchHit -> searchHit.getContent())
                .toList();

        return documents.stream()
                .collect(Collectors.groupingBy(SearchDocument::getCategory, Collectors.counting()));
    }

    // ------------------------------
    // ✅ 임시 인메모리 기반 최근 검색어 관리 (추후 DB 연동 가능)
    // ------------------------------
    private final List<String> recentSearches = new ArrayList<>();

    public void saveRecentSearch(String keyword) {
        recentSearches.remove(keyword);
        recentSearches.add(0, keyword);
        if (recentSearches.size() > 10) {
            recentSearches.remove(recentSearches.size() - 1);
        }
    }

    public List<String> getRecentSearches() {
        return new ArrayList<>(recentSearches);
    }

    // ------------------------------
    // ✅ 인기 검색어 반환 (임시 데이터; 추후 DB 연동하여 구현)
    // ------------------------------
    public List<String> getPopularSearches() {
        return List.of("맥북", "아이폰", "갤럭시", "PS5", "닌텐도");
    }
}
