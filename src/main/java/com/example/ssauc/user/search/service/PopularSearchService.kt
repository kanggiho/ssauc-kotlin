package com.example.ssauc.user.search.service

import lombok.RequiredArgsConstructor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class PopularSearchService {
    private val redisTemplate: RedisTemplate<String, Any>? = null

    /**
     * 검색어의 점수를 1 증가시키고 현재 인기 검색어 목록을 로그에 출력
     * 빈 문자열은 무시합니다.
     */
    fun addSearchKeyword(keyword: String?) {
        var keyword = keyword
        if (keyword == null || keyword.trim { it <= ' ' }.isEmpty()) {
            return
        }
        // 검색어의 앞뒤 따옴표 제거 (예: "\"" 로 둘러싸인 경우)
        keyword = keyword.trim { it <= ' ' }.replace("^\"|\"$".toRegex(), "")

        // 인덱스 작업 전에 StringRedisSerializer 적용
        redisTemplate!!.valueSerializer = StringRedisSerializer()
        val zSetOps = redisTemplate.opsForZSet()
        val newScore = zSetOps.incrementScore(POPULAR_SEARCH_KEY, keyword, 1.0)
        println("검색어 '$keyword'의 새 점수: $newScore")
        val currentSet = zSetOps.reverseRange(POPULAR_SEARCH_KEY, 0, -1)
        println("현재 인기 검색어 목록: $currentSet")
        // 작업 후 원래의 JSON Serializer 복원
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
    }

    /**
     * 인기 검색어 상위 N개를 내림차순으로 조회하여 문자열 Set으로 반환합니다.
     */
    fun getTopNKeywords(topN: Int): Set<String> {
        // 조회 작업 전에 StringRedisSerializer 적용
        redisTemplate!!.valueSerializer = StringRedisSerializer()
        val zSetOps = redisTemplate.opsForZSet()
        val raw = zSetOps.reverseRange(POPULAR_SEARCH_KEY, 0, (topN - 1).toLong())
        val result: MutableSet<String> = LinkedHashSet()
        if (raw != null) {
            for (obj in raw) {
                result.add(obj.toString())
            }
        }
        // 작업 후 원래의 JSON Serializer 복원
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        return result
    }

    fun reset() {
        redisTemplate!!.delete(POPULAR_SEARCH_KEY)
    }

    companion object {
        private const val POPULAR_SEARCH_KEY = "popular_searches"
    }
}
