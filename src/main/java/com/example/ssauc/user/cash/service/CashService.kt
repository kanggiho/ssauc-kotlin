package com.example.ssauc.user.cash.service

import com.example.ssauc.exception.PortoneVerificationException
import com.example.ssauc.user.cash.dto.CalculateDto
import com.example.ssauc.user.cash.dto.ChargeDto
import com.example.ssauc.user.cash.dto.WithdrawDto
import com.example.ssauc.user.cash.entity.Charge
import com.example.ssauc.user.cash.entity.Withdraw
import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface CashService {
    fun getCurrentUser(email: String?): Users?

    // ===================== 결제 내역 =====================
    fun getPaymentCalculatesByUser(user: Users?, pageable: Pageable?): Page<CalculateDto?>?
    fun getPaymentCalculatesByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<CalculateDto?>?

    // ===================== 정산 내역 =====================
    fun getSettlementCalculatesByUser(user: Users?, pageable: Pageable?): Page<CalculateDto?>?
    fun getSettlementCalculatesByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<CalculateDto?>?

    // ===================== 충전 내역 =====================
    fun getChargesByUser(user: Users?, pageable: Pageable?): Page<ChargeDto?>?
    fun getChargesByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<ChargeDto?>?

    // ===================== 환급 내역 =====================
    fun getWithdrawsByUser(user: Users?, pageable: Pageable?): Page<WithdrawDto?>?
    fun getWithdrawsByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<WithdrawDto?>?

    // 각 내역별 총합 계산 메서드 추가
    // ===================== 결제 금액 총합 =====================
    fun getTotalPaymentAmount(user: Users?): Long
    fun getTotalPaymentAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long

    // ===================== 정산 금액 총합 =====================
    fun getTotalSettlementAmount(user: Users?): Long
    fun getTotalSettlementAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long

    // ===================== 충전 금액 총합 =====================
    fun getTotalChargeAmount(user: Users?): Long
    fun getTotalChargeAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long

    // ===================== 환급 금액 총합 =====================
    fun getTotalWithdrawAmount(user: Users?): Long
    fun getTotalWithdrawAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long

    // PortOne 결제 처리
    @Throws(PortoneVerificationException::class)
    fun verifyAndCompletePayment(paymentId: String?, amount: Long?, user: Users?): Charge?

    // 환급 신청 처리 (수수료 계산 포함)
    fun requestWithdraw(user: Users?, amount: Long?, bank: String?, account: String?): Withdraw?

    // 이번 달 환급 신청 횟수를 반환하는 메서드 추가
    fun getCurrentWithdrawCount(user: Users?): Int
    /**
     * PortOne에서 전달받은 웹훅 요청을 처리합니다.
     *
     * @param body 웹훅 요청의 원본 본문
     * @param webhookId 웹훅 아이디
     * @param webhookTimestamp 웹훅 타임스탬프
     * @param webhookSignature 웹훅 서명
     * @throws PortoneVerificationException 검증 실패 시 예외 발생
     */
    //    void handleWebhook(String body, String webhookId, String webhookTimestamp, String webhookSignature)
    //            throws PortoneVerificationException;
}
