package com.example.ssauc.user.search.controller

import com.example.ssauc.user.search.service.RelatedSearchService
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/related-search")
class RelatedSearchController(private val relatedSearchService: RelatedSearchService) {
    /**
     * 특정 검색어에 대한 연관 검색어 조회
     * 예: GET /api/related-search?keyword=맥북
     */
    @GetMapping
    fun getRelatedSearchTerms(@RequestParam keyword: String?): List<String?> {
        return relatedSearchService.getRelatedSearchTerms(keyword)
    }

    /**
     * 검색 로그 저장 (테스트용)
     * 예: POST /api/related-search?keywords=맥북,애플워치
     */
    @PostMapping
    fun saveSearchLog(@RequestParam keywords: String): String {
        val list = Arrays.asList(*keywords.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        relatedSearchService.saveSearchLog(list)
        return "Saved search log: $list"
    }
}
