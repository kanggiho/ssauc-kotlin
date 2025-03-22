package com.example.ssauc.user.search.repository;


import com.example.ssauc.user.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableJpaRepositories(basePackages = "com.example.ssauc.user.search.repository")
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    // unchanged
    List<ProductDocument> findByNameContaining(String keyword);
}