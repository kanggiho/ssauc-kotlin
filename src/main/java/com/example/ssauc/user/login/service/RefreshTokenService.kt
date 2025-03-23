package com.example.ssauc.user.login.service

import org.slf4j.Logger

@lombok.extern.slf4j.Slf4j
@org.springframework.stereotype.Service
@lombok.RequiredArgsConstructor
class RefreshTokenService {
    private val redisTemplate: RedisTemplate<String, Any>? = null

    // 예: 14일 (분 단위: 14 * 24 * 60)
    private val REFRESH_TOKEN_EXPIRE_MIN = (14 * 24 * 60).toLong()

    fun saveRefreshToken(email: String, refreshToken: String) {
        val key = "REFRESH:$email"
        try {
            RefreshTokenService.log.debug("Redis에 refresh token 저장 시도: key={}, token={}", key, refreshToken)
            redisTemplate.opsForValue()
                .set(key, refreshToken, REFRESH_TOKEN_EXPIRE_MIN, java.util.concurrent.TimeUnit.MINUTES)
            // 저장 후 즉시 get을 호출하여 값이 존재하는지 확인해볼 수 있습니다.
            val savedToken: Any = redisTemplate.opsForValue().get(key)
            if (savedToken != null && savedToken.toString() == refreshToken) {
                RefreshTokenService.log.info("RefreshToken 저장 성공: key={}, token={}", key, refreshToken)
            } else {
                RefreshTokenService.log.warn(
                    "RefreshToken 저장 후 확인 실패: key={}, expected={}, actual={}",
                    key,
                    refreshToken,
                    savedToken
                )
            }
        } catch (e: java.lang.Exception) {
            RefreshTokenService.log.error(
                "RefreshToken 저장 중 예외 발생: key={}, token={}, error={}",
                key,
                refreshToken,
                e.message
            )
        }
    }

    fun getRefreshToken(email: String): String? {
        val key = "REFRESH:$email"
        val value: Any = redisTemplate.opsForValue().get(key)

        println("== Redis에서 토큰 조회 실행됨 ==")
        RefreshTokenService.log.info("Redis에서 토큰 조회: key={}, value={}", key, value)

        return if (value != null) value.toString() else null
    }

    fun deleteRefreshToken(email: String) {
        val key = "REFRESH:$email"
        val deleted: Boolean = redisTemplate.delete(key)
        if (deleted != null && deleted) {
            logger.info("RefreshToken 삭제 성공: key={}", key)
        } else {
            logger.warn("RefreshToken 삭제 실패 또는 키가 존재하지 않음: key={}", key)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RefreshTokenService::class.java)
    }
}
