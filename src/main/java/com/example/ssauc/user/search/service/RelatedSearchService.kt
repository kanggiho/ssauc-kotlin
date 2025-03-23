package com.example.ssauc.user.search.service

import com.example.ssauc.user.search.document.SearchLogDocument
import com.example.ssauc.user.search.repository.SearchLogRepository
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.stream.Collectors


@Slf4j
@Service
class RelatedSearchService(private val searchLogRepository: SearchLogRepository) {
    /**
     * 특정 키워드와 '자주 같이 검색된' 다른 키워드 목록을 반환
     *
     * @param keyword 기준 키워드
     * @return 연관 검색어 (상위 5개)
     */
    fun getRelatedSearchTerms(keyword: String?): List<String> {
        // 1) 해당 keyword가 포함된 모든 SearchLogDocument 조회
        val logs = searchLogRepository.findByKeywordsContaining(keyword)
        if (logs!!.isEmpty()) {
            return emptyList()
        }

        // 2) logs마다 keywords를 순회, keyword가 아닌 다른 키워드를 추출
        val freqMap: MutableMap<String, Long> = HashMap()
        for (logDoc in logs) {
            val keywords = logDoc.keywords ?: continue

            // 현재 기준 키워드를 제외한 나머지를 freqMap에 카운팅
            for (k in keywords) {
                if (!k.equals(keyword, ignoreCase = true)) {
                    freqMap[k] = freqMap.getOrDefault(k, 0L) + 1
                }
            }
        }
        RelatedSearchService.log.debug("연관 검색어 빈도맵: {}", freqMap)

        // 3) 빈도수가 높은 상위 10개 추출
        return freqMap.entries.stream()
            .sorted { e1: Map.Entry<String, Long>, e2: Map.Entry<String?, Long> -> e2.value.compareTo(e1.value) }
            .limit(10)
            .map<String> { java.util.Map.Entry.key }
            .collect(Collectors.toList<String>())
    }

    /**
     * 검색 로그에 키워드 목록 저장 (ex: 한 번에 여러 키워드를 저장)
     */
    fun saveSearchLog(keywords: List<String>?) {
        val doc = SearchLogDocument()
        doc.keywords = keywords
        doc.searchedAt = LocalDateTime.now()
        searchLogRepository.save(doc)
    } // 만약 한 번에 하나씩 저장하고 싶다면...
    // public void saveSingleKeywordLog(String keyword) { ... }
}