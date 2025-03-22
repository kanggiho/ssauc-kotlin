package com.example.ssauc.user.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PopularSearchService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String POPULAR_SEARCH_KEY = "popular_searches";

    /**
     * 검색어의 점수를 1 증가시키고 현재 인기 검색어 목록을 로그에 출력
     * 빈 문자열은 무시합니다.
     */
    public void addSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        // 검색어의 앞뒤 따옴표 제거 (예: "\"" 로 둘러싸인 경우)
        keyword = keyword.trim().replaceAll("^\"|\"$", "");

        // 인덱스 작업 전에 StringRedisSerializer 적용
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Double newScore = zSetOps.incrementScore(POPULAR_SEARCH_KEY, keyword, 1.0);
        System.out.println("검색어 '" + keyword + "'의 새 점수: " + newScore);
        Set<Object> currentSet = zSetOps.reverseRange(POPULAR_SEARCH_KEY, 0, -1);
        System.out.println("현재 인기 검색어 목록: " + currentSet);
        // 작업 후 원래의 JSON Serializer 복원
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    }

    /**
     * 인기 검색어 상위 N개를 내림차순으로 조회하여 문자열 Set으로 반환합니다.
     */
    public Set<String> getTopNKeywords(int topN) {
        // 조회 작업 전에 StringRedisSerializer 적용
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<Object> raw = zSetOps.reverseRange(POPULAR_SEARCH_KEY, 0, topN - 1);
        Set<String> result = new LinkedHashSet<>();
        if (raw != null) {
            for (Object obj : raw) {
                result.add(obj.toString());
            }
        }
        // 작업 후 원래의 JSON Serializer 복원
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return result;
    }

    public void reset() {
        redisTemplate.delete(POPULAR_SEARCH_KEY);
    }
}
