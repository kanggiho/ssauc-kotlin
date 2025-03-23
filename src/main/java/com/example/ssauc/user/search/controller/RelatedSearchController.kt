package com.example.ssauc.user.search.controller;


import com.example.ssauc.user.search.service.RelatedSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/related-search")
public class RelatedSearchController {

    private final RelatedSearchService relatedSearchService;

    public RelatedSearchController(RelatedSearchService relatedSearchService) {
        this.relatedSearchService = relatedSearchService;
    }

    /**
     * 특정 검색어에 대한 연관 검색어 조회
     * 예: GET /api/related-search?keyword=맥북
     */
    @GetMapping
    public List<String> getRelatedSearchTerms(@RequestParam String keyword) {
        return relatedSearchService.getRelatedSearchTerms(keyword);
    }

    /**
     * 검색 로그 저장 (테스트용)
     * 예: POST /api/related-search?keywords=맥북,애플워치
     */
    @PostMapping
    public String saveSearchLog(@RequestParam String keywords) {
        List<String> list = Arrays.asList(keywords.split(","));
        relatedSearchService.saveSearchLog(list);
        return "Saved search log: " + list;
    }
}
