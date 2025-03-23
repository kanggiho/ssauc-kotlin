package com.example.ssauc.user.search.repository

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.search.entity.UserRecentSearch
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface UserRecentSearchRepository : JpaRepository<UserRecentSearch?, Long?> {
    fun findTop10ByUserOrderBySearchedAtDesc(user: Users?): List<UserRecentSearch?>?

    // 동일 키워드에 해당하는 모든 레코드를 삭제하는 메서드
    fun deleteByUserAndKeyword(user: Users?, keyword: String?)
    fun findByUserAndKeyword(user: Users?, keyword: String?): Optional<UserRecentSearch?>?

    // 기존 단일 검색어 조회 (원하는 경우 유지)
    fun findAllByUserAndKeyword(user: Users?, keyword: String?): List<UserRecentSearch?>?
}