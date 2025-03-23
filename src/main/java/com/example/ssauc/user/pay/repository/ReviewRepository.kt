package com.example.ssauc.user.pay.repository

import com.example.ssauc.user.pay.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review?, Long?> {
    // 로그인한 사용자가 review의 reviewee인 경우 → 수신된 리뷰
    fun findByReviewee_UserId(userId: Long?, pageable: Pageable?): Page<Review?>?

    // 로그인한 사용자가 review의 reviewer인 경우 → 작성한 리뷰
    fun findByReviewer_UserId(userId: Long?, pageable: Pageable?): Page<Review?>?

    // 특정 유저의 최근 5개 리뷰를 가져오는 메소드
    fun findTop5ByReviewee_UserIdOrderByCreatedAtDesc(userId: Long?): List<Review?>?

    // 특정 유저의 최근 10개 리뷰를 가져오는 메소드 (10개 미만이면 그 개수만큼)
    fun findTop10ByReviewee_UserIdOrderByCreatedAtDesc(userId: Long?): List<Review?>?

    // 특정 유저의 리뷰 개수 조회
    fun countByReviewee_UserId(userId: Long?): Long?
}