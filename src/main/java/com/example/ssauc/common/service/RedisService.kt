package com.example.ssauc.common.service

import com.example.ssauc.user.search.entity.SearchKeyword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Service
class RedisService {
    @Autowired
    private val redisTemplate: RedisTemplate<String, Any>? = null

    fun saveValue(key: String, value: Any) {
        redisTemplate!!.opsForValue()[key] = value
    }

    fun saveValueWithExpire(key: String, value: Any, timeout: Long, unit: TimeUnit) {
        redisTemplate!!.opsForValue()[key, value, timeout] = unit
    }

    fun getValue(key: String): Any? {
        return redisTemplate!!.opsForValue()[key]
    }

    fun deleteValue(key: String) {
        redisTemplate!!.delete(key)
    }

    /**
     * Redis의 Sorted Set("popularSearch")에 대해,
     * 해당 검색어(member)의 점수를 score만큼 증가시킵니다.
     */
    fun incrementScore(key: String, member: String, score: Double) {
        redisTemplate!!.opsForZSet().incrementScore(key, member, score)
    }

    val popularSearchKeywords: List<SearchKeyword>
        /**
         * Redis의 Sorted Set("popularSearch")에서 상위 10개의 검색어를 조회합니다.
         * 반환된 각 검색어를 SearchKeyword 객체로 변환합니다.
         */
        get() {
            val members =
                redisTemplate!!.opsForZSet().reverseRange("popularSearch", 0, 9) ?: return ArrayList()

            return members.stream()
                .map { obj: Any -> obj.toString() }  // Object -> String 변환
                .map { member: String? -> SearchKeyword(member, 0, LocalDateTime.now()) }
                .collect(Collectors.toList())
        }
}
