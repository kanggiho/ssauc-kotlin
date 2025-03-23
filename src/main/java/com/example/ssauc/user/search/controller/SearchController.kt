package com.example.ssauc.user.search.controller

import com.example.ssauc.user.login.service.JwtService
import com.example.ssauc.user.search.document.SearchDocument
import com.example.ssauc.user.search.service.SearchLogService
import com.example.ssauc.user.search.service.SearchService
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class SearchController {
    private val searchService: SearchService? = null
    private val searchLogService: SearchLogService? = null
    private val jwtService: JwtService? = null

    /**
     * 검색 (SearchDocument 기반)
     */
    @GetMapping("/search")
    fun search(@RequestParam keyword: String): ResponseEntity<List<SearchDocument?>> {
        val results = searchService!!.searchByKeyword(keyword)
        return ResponseEntity.ok(results)
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
    fun getRecentSearches(request: HttpServletRequest): ResponseEntity<*> {
        val user = jwtService!!.extractUser(request)
            ?: return ResponseEntity.badRequest().body(
                java.util.Map.of(
                    "error",
                    "로그인이 필요합니다."
                )
            )

        val userId = user.userId.toString()
        val recentSearches = searchLogService!!.getRecentSearchesFromElasticsearch(userId)

        val response: MutableMap<String, Any?> = HashMap()
        response["recentSearches"] = recentSearches

        return ResponseEntity.ok<Map<String, Any?>>(response)
    }

    @get:GetMapping("/popular-searches")
    val popularSearches: ResponseEntity<*>
        /**
         * 인기 검색어 목록
         */
        get() {
            val popularSearches = searchLogService.getPopularSearchKeywords()

            if (popularSearches!!.isEmpty()) {
                return ResponseEntity.ok(
                    java.util.Map.of(
                        "message",
                        "인기 검색어가 없습니다."
                    )
                )
            }
            val response: MutableMap<String, Any?> =
                HashMap()
            response["popularSearches"] = popularSearches

            return ResponseEntity.ok<Map<String, Any?>>(response)
        }

    /**
     * 최근 검색어 삭제
     */
    @DeleteMapping("/recent-searches")
    fun deleteRecentSearch(
        @RequestParam keyword: String,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val user = jwtService!!.extractUser(request)
            ?: return ResponseEntity.badRequest().body(
                java.util.Map.of(
                    "error",
                    "로그인이 필요합니다."
                )
            )

        searchLogService!!.deleteUserRecentSearch(user, keyword)
        return ResponseEntity.ok(java.util.Map.of("message", "최근 검색어 삭제 완료"))
    }

    /**
     * 검색어 저장 (로그인 시, 최근 검색어/인기 검색어/ES 인덱스 적재)
     */
    @PostMapping("/save-search")
    fun saveSearch(@RequestBody payload: Map<String?, String?>, request: HttpServletRequest): ResponseEntity<String> {
        val keyword = payload["keyword"]
        if (keyword == null || keyword.trim { it <= ' ' }.isEmpty()) {
            return ResponseEntity.badRequest().body("검색어가 비어 있습니다.")
        }

        val user = jwtService!!.extractUser(request)
        if (user != null) {
            searchLogService!!.recordSearch(keyword, user)
        }
        return ResponseEntity.ok("검색어 저장 성공")
    }
}
