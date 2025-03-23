package com.example.ssauc.user.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class ElasticsearchConnectionTest(private val elasticsearchClient: ElasticsearchClient) {
    @PostConstruct
    fun testConnection() {
        try {
            val response = elasticsearchClient.ping()
            val isConnected = response.value()
            println("✅ Elasticsearch 연결 상태: $isConnected")
        } catch (e: Exception) {
            System.err.println("❌ Elasticsearch 연결 실패: " + e.message)
            e.printStackTrace()
        }
    }
}
