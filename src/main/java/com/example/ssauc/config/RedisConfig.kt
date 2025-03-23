package com.example.ssauc.config

import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Slf4j
@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private val redisHost: String? = null

    @Value("\${spring.data.redis.port}")
    private val redisPort = 0

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        RedisConfig.log.info("Connecting to Redis server at {}:{}", redisHost, redisPort)
        return LettuceConnectionFactory(redisHost!!, redisPort)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        // key는 문자열, value는 JSON 형식으로 저장
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        template.afterPropertiesSet()
        RedisConfig.log.info("RedisTemplate has been initialized.")
        return template
    }
}
