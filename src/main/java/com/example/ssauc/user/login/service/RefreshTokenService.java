package com.example.ssauc.user.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    // 예: 14일 (분 단위: 14 * 24 * 60)
    private final long REFRESH_TOKEN_EXPIRE_MIN = 14 * 24 * 60;

    public void saveRefreshToken(String email, String refreshToken) {
        String key = "REFRESH:" + email;
        try {
            log.debug("Redis에 refresh token 저장 시도: key={}, token={}", key, refreshToken);
            redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRE_MIN, TimeUnit.MINUTES);
            // 저장 후 즉시 get을 호출하여 값이 존재하는지 확인해볼 수 있습니다.
            Object savedToken = redisTemplate.opsForValue().get(key);
            if (savedToken != null && savedToken.toString().equals(refreshToken)) {
                log.info("RefreshToken 저장 성공: key={}, token={}", key, refreshToken);
            } else {
                log.warn("RefreshToken 저장 후 확인 실패: key={}, expected={}, actual={}", key, refreshToken, savedToken);
            }
        } catch(Exception e) {
            log.error("RefreshToken 저장 중 예외 발생: key={}, token={}, error={}", key, refreshToken, e.getMessage());
        }
    }

    public String getRefreshToken(String email) {
        String key = "REFRESH:" + email;
        Object value = redisTemplate.opsForValue().get(key);

        System.out.println("== Redis에서 토큰 조회 실행됨 ==");
        log.info("Redis에서 토큰 조회: key={}, value={}", key, value);

        return value != null ? value.toString() : null;
    }

    public void deleteRefreshToken(String email) {
        String key = "REFRESH:" + email;
        Boolean deleted = redisTemplate.delete(key);
        if (deleted != null && deleted) {
            logger.info("RefreshToken 삭제 성공: key={}", key);
        } else {
            logger.warn("RefreshToken 삭제 실패 또는 키가 존재하지 않음: key={}", key);
        }
    }
}
