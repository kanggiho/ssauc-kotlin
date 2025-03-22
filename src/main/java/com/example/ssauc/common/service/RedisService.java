package com.example.ssauc.common.service;

import com.example.ssauc.user.search.entity.SearchKeyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void saveValueWithExpire(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Redis의 Sorted Set("popularSearch")에 대해,
     * 해당 검색어(member)의 점수를 score만큼 증가시킵니다.
     */
    public void incrementScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    /**
     * Redis의 Sorted Set("popularSearch")에서 상위 10개의 검색어를 조회합니다.
     * 반환된 각 검색어를 SearchKeyword 객체로 변환합니다.
     */
    public List<SearchKeyword> getPopularSearchKeywords() {
        Set<Object> members = redisTemplate.opsForZSet().reverseRange("popularSearch", 0, 9);

        if (members == null) {
            return new ArrayList<>();
        }

        return members.stream()
                .map(Object::toString) // Object -> String 변환
                .map(member -> new SearchKeyword(member, 0, LocalDateTime.now()))
                .collect(Collectors.toList());
    }
}
