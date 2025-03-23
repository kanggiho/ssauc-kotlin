package com.example.ssauc.user.search.repository

import com.example.ssauc.user.search.entity.SearchKeyword
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface SearchKeywordRepository : JpaRepository<SearchKeyword?, String?> {
    fun findByKeyword(keyword: String?): Optional<SearchKeyword?>?
    fun findTop10ByOrderBySearchCountDesc(): List<SearchKeyword?>?
}