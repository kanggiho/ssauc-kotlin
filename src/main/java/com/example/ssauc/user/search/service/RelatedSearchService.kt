package com.example.ssauc.user.search.service;


import com.example.ssauc.user.search.document.SearchLogDocument;
import com.example.ssauc.user.search.repository.SearchLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RelatedSearchService {

    private final SearchLogRepository searchLogRepository;

    public RelatedSearchService(SearchLogRepository searchLogRepository) {
        this.searchLogRepository = searchLogRepository;
    }

    /**
     * 특정 키워드와 '자주 같이 검색된' 다른 키워드 목록을 반환
     *
     * @param keyword 기준 키워드
     * @return 연관 검색어 (상위 5개)
     */
    public List<String> getRelatedSearchTerms(String keyword) {
        // 1) 해당 keyword가 포함된 모든 SearchLogDocument 조회
        List<SearchLogDocument> logs = searchLogRepository.findByKeywordsContaining(keyword);
        if (logs.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) logs마다 keywords를 순회, keyword가 아닌 다른 키워드를 추출
        Map<String, Long> freqMap = new HashMap<>();
        for (SearchLogDocument logDoc : logs) {
            List<String> keywords = logDoc.getKeywords();
            if (keywords == null) continue;

            // 현재 기준 키워드를 제외한 나머지를 freqMap에 카운팅
            for (String k : keywords) {
                if (!k.equalsIgnoreCase(keyword)) {
                    freqMap.put(k, freqMap.getOrDefault(k, 0L) + 1);
                }
            }
        }
        log.debug("연관 검색어 빈도맵: {}", freqMap);

        // 3) 빈도수가 높은 상위 10개 추출
        return freqMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 검색 로그에 키워드 목록 저장 (ex: 한 번에 여러 키워드를 저장)
     */
    public void saveSearchLog(List<String> keywords) {
        SearchLogDocument doc = new SearchLogDocument();
        doc.setKeywords(keywords);
        doc.setSearchedAt(java.time.LocalDateTime.now());
        searchLogRepository.save(doc);
    }

    // 만약 한 번에 하나씩 저장하고 싶다면...
    // public void saveSingleKeywordLog(String keyword) { ... }
}