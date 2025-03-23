package com.example.ssauc.user.search.config

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElasticsearchConfig {
    //    @Bean
    //    public ElasticsearchClient elasticsearchClient() {
    //        // Elasticsearch 기본 RestClient 생성
    //        RestClient restClient = RestClient.builder(
    //                new HttpHost("localhost", 9200, "http") // 보안 비활성화 상태이므로 http 사용
    //        ).build();
    //
    //        // JSON 매퍼 설정
    //        RestClientTransport transport = new RestClientTransport(
    //                restClient, new JacksonJsonpMapper()
    //        );
    //
    //        return new ElasticsearchClient(transport);
    //    }
    //test
    @Value("\${spring.elasticsearch.rest.uris}")
    private val elasticsearchUri: String? = null

    @Value("\${spring.elasticsearch.rest.username}")
    private val username: String? = null

    @Value("\${spring.elasticsearch.rest.password}")
    private val password: String? = null

    @Bean
    fun elasticsearchClient(): ElasticsearchClient {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials(username, password)
        )

        val builder = RestClient.builder(HttpHost.create(elasticsearchUri))
            .setHttpClientConfigCallback { httpClientBuilder: HttpAsyncClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(
                    credentialsProvider
                )
            }

        val restClient = builder.build()
        val transport = RestClientTransport(restClient, JacksonJsonpMapper())

        return ElasticsearchClient(transport)
    }
}
