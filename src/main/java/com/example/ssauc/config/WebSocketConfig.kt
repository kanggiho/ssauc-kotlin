package com.example.ssauc.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 클라이언트가 WebSocket 핸드셰이크 하는 엔드포인트
        registry.addEndpoint("/ws-stomp")
            .setAllowedOrigins(
                "http://www.ssauc.ap-northeast-2.elasticbeanstalk.com",
                "http://localhost:5000",
                "http://localhost:3000"
            ) // 실제 운영 시에는 특정 도메인만 허용 권장
            .withSockJS() // SockJS fallback
    }

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // 클라이언트에서 메시지 보낼 때의 prefix
        config.setApplicationDestinationPrefixes("/pub", "/app")
        // 클라이언트에서 메시지 구독할 때의 prefix
        config.enableSimpleBroker("/sub", "/topic")
    }
}
