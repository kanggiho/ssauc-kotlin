package com.example.ssauc.user.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchConnectionTest {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchConnectionTest(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @PostConstruct
    public void testConnection() {
        try {
            BooleanResponse response = elasticsearchClient.ping();
            boolean isConnected = response.value();
            System.out.println("✅ Elasticsearch 연결 상태: " + isConnected);
        } catch (Exception e) {
            System.err.println("❌ Elasticsearch 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
