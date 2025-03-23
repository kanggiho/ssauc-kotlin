package com.example.ssauc.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

// RestTemplate 빈 등록
@Configuration
class AppConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

