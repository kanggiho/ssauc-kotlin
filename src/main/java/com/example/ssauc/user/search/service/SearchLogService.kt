package com.example.ssauc.user.search.service


import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.function.Supplier

@Slf4j
@Service
@RequiredArgsConstructor
class SearchLogService {
    private val searchKeywordRepository: SearchKeywordRepository? = null
    private val userRecentSearchRepository: UserRecentSearchRepository? = null
    private val esOps: ElasticsearchOperations? = null
    private val recentSearchService: RecentSearchService? = null
    private val popularSearchService: PopularSearchService? = null

    // RedisService를 주입받아 Redis 기반 인기 검색어 업데이트 및 조회에 사용
    private val redisService: RedisService? = null

    /**
     * 검색어 기록:
     * 1) 인기 검색어 DB 기록 (SearchKeyword 엔티티 업데이트)
     * 2) 사용자별 최근 검색어 DB 기록 (UserRecentSearch 엔티티 업데이트)
     * 3) ES에 최근 검색어 인덱싱 (RecentSearchDocument 인덱스 업데이트)
     * 4) Redis에 인기 검색어 업데이트 (Redis sorted set을 이용)
     */
    @Transactional
    fun recordSearch(keyword: String?, user: Users?) {
        if (user == null || keyword == null || keyword.trim { it <= ' ' }.isEmpty()) return

        // 1) 인기 검색어 DB 저장
        val sk: SearchKeyword = searchKeywordRepository.findByKeyword(keyword)
            .orElseGet(Supplier<SearchKeyword> { SearchKeyword(keyword, 0, LocalDateTime.now()) })
        sk.setSearchCount(sk.getSearchCount() + 1)
        sk.setLastSearched(LocalDateTime.now())
        searchKeywordRepository.save<SearchKeyword>(sk)
        logger.info("인기 검색어 DB 업데이트 완료: {}", sk)

        // 2) 사용자별 최근 검색어 저장
        val existing: Optional<UserRecentSearch> = userRecentSearchRepository.findByUserAndKeyword(user, keyword)
        if (existing.isPresent()) {
            // 이미 존재하면 타임스탬프만 업데이트
            val urs: UserRecentSearch = existing.get()
            urs.setSearchedAt(LocalDateTime.now())
            userRecentSearchRepository.save<UserRecentSearch>(urs)
            logger.info("기존 사용자 검색어 갱신: {}", urs)
        } else {
            // 없으면 새로 저장
            val urs: UserRecentSearch = UserRecentSearch()
            urs.setUser(user)
            urs.setKeyword(keyword)
            urs.setSearchedAt(LocalDateTime.now())
            userRecentSearchRepository.save<UserRecentSearch>(urs)
            logger.info("새 사용자 검색어 저장: {}", urs)
        }

        // 3) Elasticsearch 저장
        try {
            val doc: RecentSearchDocument = RecentSearchDocument()
            doc.setUserId(user.userId.toString())
            doc.setKeyword(keyword)
            doc.setSearchedAt(LocalDateTime.now())
            val indexQuery: IndexQuery = IndexQueryBuilder().withObject(doc).build()
            esOps.index(indexQuery, IndexCoordinates.of("recent_search"))

            println("✅ Elasticsearch 저장 완료: $doc")
        } catch (e: Exception) {
            System.err.println("❌ Elasticsearch 저장 오류: " + e.message)
        }

        // 4) Redis에 인기 검색어 업데이트
        recentSearchService!!.addRecentSearch(user.userId.toString(), keyword)
        popularSearchService!!.addSearchKeyword(keyword)
    }

    /**
     * ES에서 최근 검색어 조회 (SearchLogQueryUtil을 활용)
     */
    fun getRecentSearchesFromElasticsearch(userId: String): List<String> {
        // 로그를 통해 호출 횟수 확인
        SearchLogService.log.debug("recordSearch 호출됨 - keyword: {}, userId: {}")
        try {
            val recentSearches: List<String> = SearchLogQueryUtil(esOps).findRecentSearchKeywords(userId, 20)
            println("✅ 사용자 ID: $userId")
            println("📌 최근 검색어 목록: $recentSearches")
            return recentSearches
        } catch (e: Exception) {
            System.err.println("❌ 최근 검색어 조회 오류: " + e.message)
            return listOf() // 오류 발생 시 빈 리스트 반환
        }
    }

    val popularSearchKeywords: List<Any>
        /**
         * 인기 검색어 조회:
         * 우선 Redis에서 인기 검색어 데이터를 조회하고, 없으면 DB에서 조회합니다.
         */
        get() {
            val popularFromRedis: List<SearchKeyword> = redisService.popularSearchKeywords
            if (popularFromRedis != null && !popularFromRedis.isEmpty()) {
                return popularFromRedis
            }
            return searchKeywordRepository.findTop10ByOrderBySearchCountDesc()
        }

    /**
     * 사용자별 최근 검색어 삭제
     */
    @Transactional
    fun deleteUserRecentSearch(user: Users, keyword: String) {
        // 1. MySQL 삭제: 동일 키워드의 모든 레코드를 삭제하도록 합니다.
        userRecentSearchRepository.deleteByUserAndKeyword(user, keyword)
        logger.info("MySQL: 사용자 {}의 최근 검색어 '{}' 삭제 완료", user.userId, keyword)

        // 2. Elasticsearch 삭제:
        // Elasticsearch에서 userId와 keyword가 일치하는 RecentSearchDocument들을 삭제합니다.
        try {
            val criteria = Criteria.where("userId").`is`(user.userId.toString())
                .and(Criteria.where("keyword").`is`(keyword))
            val query = CriteriaQuery(criteria)
            // delete(query, DocumentClass.class, IndexCoordinates) 메서드로 조건에 맞는 모든 문서를 삭제합니다.
            esOps.delete(query, RecentSearchDocument::class.java, IndexCoordinates.of("recent_search"))
            logger.info("Elasticsearch: 사용자 {}의 최근 검색어 '{}' 삭제 완료", user.userId, keyword)
        } catch (e: Exception) {
            logger.error("Elasticsearch 삭제 오류: {}", e.message)
        }

        // 3. Redis 삭제:
        // recentSearchService.deleteRecentSearch(String userId, String keyword) 메서드를 호출합니다.
        recentSearchService!!.deleteRecentSearch(user.userId.toString(), keyword)
        logger.info("Redis: 사용자 {}의 최근 검색어 '{}' 삭제 요청 완료", user.userId, keyword)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SearchLogService::class.java)
    }
}
