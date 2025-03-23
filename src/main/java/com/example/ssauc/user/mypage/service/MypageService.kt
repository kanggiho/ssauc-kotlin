package com.example.ssauc.user.mypage.service

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.mypage.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MypageService {
    // JWT 기반 인증 환경에서 사용자 정보 (@AuthenticationPrincipal 사용)
    fun getCurrentUser(email: String?): Users

    // 로그인한 사용자가 수신한 리뷰 목록 조회 (reviewee가 본인)
    fun getReceivedReviews(user: Users, pageable: Pageable?): Page<EvaluationReviewDto>

    // 로그인한 사용자가 작성한 리뷰 목록 조회 (reviewer가 본인)
    fun getWrittenReviews(user: Users, pageable: Pageable?): Page<EvaluationReviewDto>

    // 로그인한 사용자가 아직 리뷰를 작성하지 않은 주문 목록 조회
    fun getPendingReviews(user: Users, pageable: Pageable?): Page<EvaluationPendingDto>

    // 리뷰 제출 처리
    fun submitEvaluation(evaluationDto: EvaluationDto, currentUser: Users)

    // 신규 추가: 리뷰 작성 페이지에 필요한 주문/상품 정보를 EvaluationDto에 담아서 전달
    fun getEvaluationData(orderId: Long, currentUser: Users): EvaluationDto

    // 리뷰 상세 정보
    fun getReviewById(reviewId: Long, currentUserId: Long?): EvaluatedDto

    // 회원 정보 불러오기
    fun getUserInfo(email: String?): Users?

    // 회원 정보 페이지 그래프
    fun getReputationHistory(user: Users?): List<ReputationGraphDto>

    // userName으로 사용자 정보를 조회하여 ResponseUserInfoDto로 반환
    fun getUserInfoJson(userName: String?): ResponseUserInfoDto
}
