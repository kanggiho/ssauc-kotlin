package com.example.ssauc.user.search.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecentSearchService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_RECENT_SEARCHES = 10; // 최대 저장 개수

    public RecentSearchService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 사용자별 최근 검색어 저장.
     * 동일 키워드는 먼저 삭제 후 새로 추가하여 최신순 정렬을 유지합니다.
     */
    public void addRecentSearch(String userId, String keyword) {
        String key = "recent_search:" + userId;
        // 중복 검색어 제거 (0은 모든 항목 제거)
        redisTemplate.opsForList().remove(key, 0, keyword);
        // 검색어를 왼쪽에 추가
        redisTemplate.opsForList().leftPush(key, keyword);
        // 리스트 크기를 최대 개수로 제한 (0부터 MAX-1까지)
        redisTemplate.opsForList().trim(key, 0, MAX_RECENT_SEARCHES - 1);
    }

    /**
     * 해당 사용자에 대한 최근 검색어 리스트 반환.
     */
    public List<String> getRecentSearches(String userId) {
        String key = "recent_search:" + userId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 특정 검색어를 삭제.
     */
    public void deleteRecentSearch(String userId, String keyword) {
        String key = "recent_search:" + userId;
        redisTemplate.opsForList().remove(key, 0, keyword);
    }
}
