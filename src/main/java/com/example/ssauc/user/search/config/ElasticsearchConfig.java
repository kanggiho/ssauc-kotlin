package com.example.ssauc.user.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

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
    @Value("${spring.elasticsearch.rest.uris}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.rest.username}")
    private String username;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(org.apache.http.HttpHost.create(elasticsearchUri))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        RestClient restClient = builder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
