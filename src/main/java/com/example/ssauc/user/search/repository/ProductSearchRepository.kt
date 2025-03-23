package com.example.ssauc.user.search.repository

import com.example.ssauc.user.search.document.ProductDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository


@EnableJpaRepositories(basePackages = ["com.example.ssauc.user.search.repository"])
@Repository
interface ProductSearchRepository : ElasticsearchRepository<ProductDocument?, String?> {
    // unchanged
    fun findByNameContaining(keyword: String?): List<ProductDocument?>?
}