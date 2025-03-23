package com.example.ssauc.user.search.controller;


import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.service.JwtService;
import com.example.ssauc.user.search.document.SearchDocument;
import com.example.ssauc.user.search.entity.SearchKeyword;
import com.example.ssauc.user.search.service.SearchLogService;
import com.example.ssauc.user.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchLogService searchLogService;
    private final JwtService jwtService;

    /**
     * 검색 (SearchDocument 기반)
     */
    @GetMapping("/search")
    public ResponseEntity<List<SearchDocument>> search(@RequestParam String keyword) {
        List<SearchDocument> results = searchService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * (중복 가능) 카테고리 기반 검색
     * 이미 ProductController에서 처리 가능하므로 주석 처리
     */
/*
    @GetMapping("/search/category")
    public ResponseEntity<List<SearchDocument>> searchByCategory(
            @RequestParam String keyword,
            @RequestParam String category) {
        return ResponseEntity.ok(searchService.searchByKeywordAndCategory(keyword, category));
    }
*/

    /**
     * (SearchDocument 인덱스를 기반으로) 카테고리 목록 + 개수
     * 마찬가지로 ProductController에서 가능하다면 주석 가능
     */
/*
    @GetMapping("/search/categories")
    public ResponseEntity<Map<String, Long>> getCategories() {
        return ResponseEntity.ok(searchService.getCategoryAggregations());
    }
*/

    /**
     * 로그인 사용자의 최근 검색어 조회
     */
    @GetMapping("/recent-searches")
    public ResponseEntity<?> getRecentSearches(HttpServletRequest request) {
        Users user = jwtService.extractUser(request);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "로그인이 필요합니다."));
        }

        String userId = String.valueOf(user.getUserId());
        List<String> recentSearches = searchLogService.getRecentSearchesFromElasticsearch(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("recentSearches", recentSearches);

        return ResponseEntity.ok(response);
    }

    /**
     * 인기 검색어 목록
     */
    @GetMapping("/popular-searches")
    public ResponseEntity<?> getPopularSearches() {
        List<SearchKeyword> popularSearches = searchLogService.getPopularSearchKeywords();

        if (popularSearches.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "인기 검색어가 없습니다."));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("popularSearches", popularSearches);

        return ResponseEntity.ok(response);
    }

    /**
     * 최근 검색어 삭제
     */
    @DeleteMapping("/recent-searches")
    public ResponseEntity<?> deleteRecentSearch(@RequestParam String keyword,
                                                HttpServletRequest request) {
        Users user = jwtService.extractUser(request);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "로그인이 필요합니다."));
        }

        searchLogService.deleteUserRecentSearch(user, keyword);
        return ResponseEntity.ok(Map.of("message", "최근 검색어 삭제 완료"));
    }

    /**
     * 검색어 저장 (로그인 시, 최근 검색어/인기 검색어/ES 인덱스 적재)
     */
    @PostMapping("/save-search")
    public ResponseEntity<String> saveSearch(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String keyword = payload.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("검색어가 비어 있습니다.");
        }

        Users user = jwtService.extractUser(request);
        if (user != null) {
            searchLogService.recordSearch(keyword, user);
        }
        return ResponseEntity.ok("검색어 저장 성공");
    }
}
