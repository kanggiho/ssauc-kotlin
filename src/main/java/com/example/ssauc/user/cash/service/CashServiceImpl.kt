package com.example.ssauc.user.cash.service

import com.example.ssauc.common.service.CommonUserService
import com.example.ssauc.exception.PortoneVerificationException
import com.example.ssauc.user.cash.dto.CalculateDto
import com.example.ssauc.user.cash.dto.ChargeDto
import com.example.ssauc.user.cash.dto.WithdrawDto
import com.example.ssauc.user.cash.entity.Charge
import com.example.ssauc.user.cash.entity.Charge.amount
import com.example.ssauc.user.cash.entity.Charge.chargeId
import com.example.ssauc.user.cash.entity.Charge.chargeType
import com.example.ssauc.user.cash.entity.Charge.createdAt
import com.example.ssauc.user.cash.entity.Charge.receiptUrl
import com.example.ssauc.user.cash.entity.Charge.status
import com.example.ssauc.user.cash.entity.Withdraw
import com.example.ssauc.user.cash.entity.Withdraw.account
import com.example.ssauc.user.cash.entity.Withdraw.amount
import com.example.ssauc.user.cash.entity.Withdraw.bank
import com.example.ssauc.user.cash.entity.Withdraw.commission
import com.example.ssauc.user.cash.entity.Withdraw.withdrawAt
import com.example.ssauc.user.cash.entity.Withdraw.withdrawId
import com.example.ssauc.user.cash.repository.ChargeRepository
import com.example.ssauc.user.cash.repository.WithdrawRepository
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.order.entity.Orders
import com.example.ssauc.user.order.repository.OrdersRepository
import com.example.ssauc.user.product.entity.Product.productId
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Function

@Service
@RequiredArgsConstructor
class CashServiceImpl : CashService {
    private val commonUserService: CommonUserService? = null
    private val chargeRepository: ChargeRepository? = null
    private val withdrawRepository: WithdrawRepository? = null
    private val ordersRepository: OrdersRepository? = null
    private val usersRepository: UsersRepository? = null

    @Value("\${portone.secret.api}")
    private val portoneApiSecret: String? = null

    // 만약 필요하다면 웹훅 관련 키도 주입
    // @Value("${portone.secret.webhook}")
    // private String portoneWebhookSecret;
    private val restTemplate = RestTemplate()

    override fun getCurrentUser(email: String?): Users? {
        return commonUserService!!.getCurrentUser(email)
    }

    // ===================== 결제 내역 =====================
    override fun getPaymentCalculatesByUser(user: Users?, pageable: Pageable?): Page<CalculateDto?>? {
        // 주문 중 구매자인 경우 (user가 buyer)
        val ordersPage = ordersRepository!!.findByBuyer(user, pageable)
        return ordersPage.map<CalculateDto?>(Function<Orders, CalculateDto?> { order: Orders ->
            val payment = order.payments[0]
            val productImageUrl = order.product.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            CalculateDto.builder()
                .orderId(order.orderId)
                .paymentAmount(order.totalPrice)
                .productId(order.product.productId)
                .productName(order.product.name)
                .productImageUrl(mainImage)
                .paymentTime(payment.paymentDate)
                .orderStatus(order.orderStatus)
                .build()
        })
    }

    override fun getPaymentCalculatesByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<CalculateDto?>? {
        val ordersPage = ordersRepository!!.findByBuyerAndPaymentTimeBetween(user, startDate, endDate, pageable)
        return ordersPage.map<CalculateDto?>(Function<Orders, CalculateDto?> { order: Orders ->
            val payment = order.payments[0]
            val productImageUrl = order.product.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            CalculateDto.builder()
                .orderId(order.orderId)
                .paymentAmount(order.totalPrice)
                .productId(order.product.productId)
                .productName(order.product.name)
                .productImageUrl(mainImage)
                .paymentTime(payment.paymentDate)
                .orderStatus(order.orderStatus)
                .build()
        })
    }

    // ===================== 정산 내역 =====================
    override fun getSettlementCalculatesByUser(user: Users?, pageable: Pageable?): Page<CalculateDto?>? {
        // 주문 중 판매자인 경우 (user가 seller)
        val ordersPage = ordersRepository!!.findBySeller(user, pageable)
        return ordersPage.map<CalculateDto?>(Function<Orders, CalculateDto?> { order: Orders ->
            val payment = order.payments[0]
            val productImageUrl = order.product.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            CalculateDto.builder()
                .orderId(order.orderId)
                .paymentAmount(order.totalPrice)
                .productId(order.product.productId)
                .productName(order.product.name)
                .productImageUrl(mainImage)
                .paymentTime(payment.paymentDate)
                .orderStatus(order.orderStatus)
                .build()
        })
    }

    override fun getSettlementCalculatesByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<CalculateDto?>? {
        val ordersPage = ordersRepository!!.findBySellerAndPaymentTimeBetween(user, startDate, endDate, pageable)
        return ordersPage.map<CalculateDto?>(Function<Orders, CalculateDto?> { order: Orders ->
            val payment = order.payments[0]
            val productImageUrl = order.product.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            CalculateDto.builder()
                .orderId(order.orderId)
                .paymentAmount(order.totalPrice)
                .productId(order.product.productId)
                .productName(order.product.name)
                .productImageUrl(mainImage)
                .paymentTime(payment.paymentDate)
                .orderStatus(order.orderStatus)
                .build()
        })
    }

    // ===================== 충전 내역 =====================
    override fun getChargesByUser(user: Users?, pageable: Pageable?): Page<ChargeDto?>? {
        val chargePage = chargeRepository!!.findByUser(user, pageable)
        return chargePage!!.map<ChargeDto?>(Function<Charge?, ChargeDto?> { ch: Charge? ->
            ChargeDto.builder()
                .chargeId(ch!!.chargeId)
                .chargeType(ch.chargeType)
                .amount(ch.amount)
                .status(ch.status)
                .createdAt(ch.createdAt)
                .receiptUrl(ch.receiptUrl)
                .build()
        })
    }

    override fun getChargesByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<ChargeDto?>? {
        val chargePage = chargeRepository!!.findByUserAndCreatedAtBetween(user, startDate, endDate, pageable)
        return chargePage!!.map<ChargeDto?>(Function<Charge?, ChargeDto?> { ch: Charge? ->
            ChargeDto.builder()
                .chargeId(ch!!.chargeId)
                .chargeType(ch.chargeType)
                .amount(ch.amount)
                .status(ch.status)
                .createdAt(ch.createdAt)
                .receiptUrl(ch.receiptUrl)
                .build()
        })
    }

    // ===================== 환급 내역 =====================
    override fun getWithdrawsByUser(user: Users?, pageable: Pageable?): Page<WithdrawDto?>? {
        // ※ WithdrawRepository에 Page<Withdraw> findByUser(Users user, Pageable pageable) 메서드가 필요합니다.
        val withdrawPage = withdrawRepository!!.findByUser(user, pageable)
        return withdrawPage!!.map<WithdrawDto?>(Function<Withdraw?, WithdrawDto?> { w: Withdraw? ->
            val status = if ((w!!.withdrawAt != null)) "완료" else "처리중"
            WithdrawDto.builder()
                .withdrawId(w.withdrawId)
                .bank(w.bank)
                .account(w.account)
                .netAmount(w.amount!! - w.commission!!)
                .withdrawAt(w.withdrawAt)
                .requestStatus(status)
                .build()
        })
    }

    override fun getWithdrawsByUser(
        user: Users?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable?
    ): Page<WithdrawDto?>? {
        val withdrawPage = withdrawRepository!!.findByUserAndWithdrawAtBetween(user, startDate, endDate, pageable)
        return withdrawPage!!.map<WithdrawDto?>(Function<Withdraw?, WithdrawDto?> { w: Withdraw? ->
            val status = if ((w!!.withdrawAt != null)) "환급완료" else "처리중"
            WithdrawDto.builder()
                .withdrawId(w.withdrawId)
                .bank(w.bank)
                .account(w.account)
                .netAmount(w.amount!! - w.commission!!)
                .withdrawAt(w.withdrawAt)
                .requestStatus(status)
                .build()
        })
    }

    // ===================== 결제 금액 총합 =====================
    override fun getTotalPaymentAmount(user: Users?): Long {
        return ordersRepository!!.sumTotalPriceByBuyer(user)
    }

    override fun getTotalPaymentAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long {
        return ordersRepository!!.sumTotalPriceByBuyerAndPaymentDateBetween(user, startDate, endDate)
    }

    // ===================== 정산 금액 총합 =====================
    override fun getTotalSettlementAmount(user: Users?): Long {
        return ordersRepository!!.sumTotalPriceBySeller(user)
    }

    override fun getTotalSettlementAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long {
        return ordersRepository!!.sumTotalPriceBySellerAndPaymentDateBetween(user, startDate, endDate)
    }

    // ===================== 충전 금액 총합 =====================
    override fun getTotalChargeAmount(user: Users?): Long {
        return chargeRepository!!.sumAmountByUser(user)
    }

    override fun getTotalChargeAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long {
        return chargeRepository!!.sumAmountByUserAndCreatedAtBetween(user, startDate, endDate)
    }

    // ===================== 환급 금액 총합 =====================
    override fun getTotalWithdrawAmount(user: Users?): Long {
        return withdrawRepository!!.sumNetAmountByUser(user)
    }

    override fun getTotalWithdrawAmount(user: Users?, startDate: LocalDateTime?, endDate: LocalDateTime?): Long {
        return withdrawRepository!!.sumNetAmountByUserAndWithdrawAtBetween(user, startDate, endDate)
    }

    // ===================== 결제(Portone) =====================
    @Throws(PortoneVerificationException::class)
    override fun verifyAndCompletePayment(paymentId: String?, providedAmount: Long, user: Users): Charge? {
        // 1. 결제 정보 사전 등록(pre‑register) API 호출 (POST)
        val preRegisterUrl = "https://api.portone.io/payments/$paymentId/pre-register"
        // RestTemplate에 필요한 헤더(Authorization 등)를 추가하는 방법은
        // 예를 들어 HttpHeaders와 HttpEntity를 이용해서 구성할 수 있습니다.
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = "PortOne $portoneApiSecret"

        // 사전 등록 요청 바디 구성 (필요한 값만 전달)
        // storeId는 생략하면 토큰에 담긴 상점 아이디 사용, taxFreeAmount는 0으로 설정할 수 있습니다.
        val preRegisterRequest: MutableMap<String, Any> = HashMap()
        preRegisterRequest["totalAmount"] = providedAmount
        preRegisterRequest["taxFreeAmount"] = 0
        preRegisterRequest["currency"] = "KRW"

        val preRegisterEntity = HttpEntity<Map<String, Any>>(preRegisterRequest, headers)
        var preRegisterResponse: ResponseEntity<String?>? = null
        try {
            preRegisterResponse = restTemplate.exchange(
                preRegisterUrl, HttpMethod.POST, preRegisterEntity,
                String::class.java
            )
        } catch (e: HttpClientErrorException) {
            if (e.statusCode === HttpStatus.CONFLICT) {
                // 409 Conflict: 이미 사전 등록된 결제 건이 있으므로, 계속 진행
                // Optionally, log a warning here.
            } else {
                throw PortoneVerificationException("Pre-register API 호출 중 오류 발생", e)
            }
        }

        // If preRegisterResponse exists and is not successful, but it's not conflict, throw an error.
        if (preRegisterResponse != null && !preRegisterResponse.statusCode.is2xxSuccessful && preRegisterResponse.statusCode !== HttpStatus.CONFLICT) {
            throw PortoneVerificationException("Pre-register API 호출 실패: " + preRegisterResponse.statusCode)
        }


        // 2. GET /payments/{paymentId}로 결제 상세 정보를 조회
        val getUrl = "https://api.portone.io/payments/$paymentId"
        val getEntity = HttpEntity<String>(null, headers) // GET 요청이므로 body는 null로 설정
        val responseEntity: ResponseEntity<String>
        try {
            responseEntity = restTemplate.exchange(
                getUrl, HttpMethod.GET, getEntity,
                String::class.java
            )
        } catch (e: Exception) {
            throw PortoneVerificationException("PortOne GET API 호출 중 오류 발생", e)
        }

        if (!responseEntity.statusCode.is2xxSuccessful) {
            throw PortoneVerificationException("PortOne GET API 호출 실패: " + responseEntity.statusCode)
        }

        // 3. JSON 응답 파싱
        val mapper = ObjectMapper()
        val responseMap: Map<String, Any>
        try {
            responseMap =
                mapper.readValue<Map<String, Any>>(responseEntity.body, object : TypeReference<Map<String?, Any?>?>() {
                })
        } catch (e: Exception) {
            throw PortoneVerificationException("응답 파싱 실패", e)
        }

        // 4. PortOne 응답에서 필요한 필드 추출
        // (필드 이름은 PortOne API v2 문서를 참고) --> 로그에 찍힌 JSON 응답 기반
        val impUid = responseMap["id"] as String? // PortOne 고유 아이디

        // 결제 수단 처리
        var payMethod: String? = null
        val methodObj = responseMap["method"]
        if (methodObj is Map<*, *>) {
            val methodMap = methodObj as Map<String, Any>
            val methodType = methodMap["type"] as String?
            if ("PaymentMethodEasyPay" == methodType) {
                // 간편 결제인 경우, provider 값을 이용하여 "간편결제(공급자)" 형태로 만듭니다.
                val provider = methodMap["provider"] as String?
                payMethod = if (provider != null) "간편결제($provider)" else "간편결제"
            } else if ("PaymentMethodCard" == methodType) {
                // 카드 결제인 경우, card 객체 내 issuer 값을 이용하여 "카드 결제(발급사)" 형태로 만듭니다.
                val cardObj = methodMap["card"]
                if (cardObj is Map<*, *>) {
                    val cardMap = cardObj as Map<String, Any>
                    val issuer = cardMap["issuer"] as String?
                    payMethod = if (issuer != null) "카드 결제($issuer)" else "카드 결제"
                } else {
                    payMethod = "카드 결제"
                }
            } else {
                // 그 외의 경우에는 기본적으로 type과 provider를 이어서 사용
                val provider = methodMap["provider"] as String?
                if (methodType != null && provider != null) {
                    payMethod = "$methodType - $provider"
                } else if (methodType != null) {
                    payMethod = methodType
                } else if (provider != null) {
                    payMethod = provider
                }
            }
        }

        // amountObj가 null이면 클라이언트에서 제공한 금액 사용
        // amount 객체 내에서 총 결제 금액 추출
        val parsedAmount: Long
        val amountObj = responseMap["amount"]
        if (amountObj is Map<*, *>) {
            val amountMap = amountObj as Map<String, Any?>
            parsedAmount = if (amountMap["total"] != null) {
                try {
                    amountMap["total"].toString().toLong()
                } catch (e: Exception) {
                    throw PortoneVerificationException("amount 파싱 실패", e)
                }
            } else {
                providedAmount
            }
        } else {
            // fallback: 클라이언트에서 전달한 값 사용
            parsedAmount = providedAmount
        }
        // 결제 금액이 다르면 오류 발생 (클라이언트에서 조작했을 가능성 있음)
        if (parsedAmount != providedAmount) {
            throw PortoneVerificationException("결제 금액 불일치: 제공된 금액($providedAmount) vs 실제 결제 금액($parsedAmount)")
        }

        // 결제 상태 (예: "PAID")
        var status = responseMap["status"] as String?
        status = when (status) {
            "PAID" -> "충전완료"
            else -> "충전실패"
        }

        // 결제 상세 메시지
        var pgResultMsg: String? = null
        val pgResponseObj = responseMap["pgResponse"]
        if (pgResponseObj is Map<*, *>) {
            val pgResponseMap = pgResponseObj as Map<String, Any>
            pgResultMsg = pgResponseMap["ResultMsg"] as String?
        } else if (pgResponseObj is String) {
            try {
                // JSON 문자열을 Map으로 변환
                val pgResponseMap: Map<String, Any> = mapper.readValue<Map<String, Any>>(
                    pgResponseObj, object : TypeReference<Map<String?, Any?>?>() {
                    })
                pgResultMsg = pgResponseMap["ResultMsg"] as String?
            } catch (e: Exception) {
                throw PortoneVerificationException("pgResponse 파싱 실패", e)
            }
        }
        // 영수증 URL
        val receiptUrl = responseMap["receiptUrl"] as String?


        // paid_at 처리 (ISO‑8601 문자열을 한국 시간으로 변환)
        val paidAtStr = responseMap["paidAt"] as String?
        var paidAt: LocalDateTime?
        if (paidAtStr == null || paidAtStr.trim { it <= ' ' }.isEmpty()) {
            paidAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        } else {
            try {
                val zdt = ZonedDateTime.parse(paidAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val koreaZdt = zdt.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                paidAt = koreaZdt.toLocalDateTime()
            } catch (e: Exception) {
                paidAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
            }
        }

        // 5. 새 Charge 객체 생성
        val charge: Charge = Charge.builder()
            .user(user)
            .impUid(impUid)
            .chargeType(payMethod)
            .amount(parsedAmount)
            .status(status)
            .details(pgResultMsg)
            .receiptUrl(receiptUrl)
            .createdAt(paidAt)
            .build()

        // 6. DB에 Charge 저장
        chargeRepository!!.save(charge)

        // 7. 결제 성공 시 사용자 잔액 업데이트 (결과를 DB에 반영)
        if ("충전완료".equals(status, ignoreCase = true)) {
            // 예시: 사용자 객체의 캐시 값을 업데이트하고, 세션에 다시 저장
            user.cash = user.cash!! + parsedAmount
            usersRepository!!.save(user)
        }

        // 응답 json 확인용
        val responseBody = responseEntity.body
        println("Raw PortOne API Response: $responseBody")

        return charge
    }

    // ===================== 환급 신청 =====================
    override fun requestWithdraw(user: Users, amount: Long, bank: String?, account: String?): Withdraw? {
        // 서버 측 검증: 환급 요청 금액이 사용자의 잔액 이하인지 확인
        require(amount <= user.cash!!) { "환급 신청 금액이 보유한 머니를 초과합니다." }

        val now = LocalDateTime.now()
        // 이번 달의 시작과 끝 구하기
        val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
            .withHour(23).withMinute(59).withSecond(59)
        // 이번 달 환급 신청 건수 확인
        val withdrawCount = withdrawRepository!!.countByUserAndRequestedAtBetween(user, startOfMonth, endOfMonth)
        // 월 3회까지 무료, 초과부터 1,000원 수수료 적용
        val commission = (if (withdrawCount < 3) 0 else 1000).toLong()

        require(amount > commission) { "환급 요청 금액이 수수료보다 작거나 같습니다. 환급 요청 금액을 수정해주세요." }

        val withdraw: Withdraw = Withdraw.builder()
            .user(user)
            .amount(amount)
            .commission(commission)
            .bank(bank)
            .account(account)
            .requestedAt(now)
            .withdrawAt(null) // 아직 환급 완료되지 않았으므로 null
            .build()
        withdrawRepository.save(withdraw)
        return withdraw
    }

    // ===================== 환급 신청 횟수 =====================
    override fun getCurrentWithdrawCount(user: Users?): Int {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
            .withHour(23).withMinute(59).withSecond(59)
        // requested_at 기준으로 환급 횟수 계산
        return withdrawRepository!!.countByUserAndRequestedAtBetween(user, startOfMonth, endOfMonth)
    } //    @Override
    //    public void handleWebhook(String body, String webhookId, String webhookTimestamp, String webhookSignature)
    //            throws PortoneVerificationException {
    //        // 웹훅 검증 및 처리 로직 구현
    //        // 예시: webhook 서명 검증 후, body에서 paymentId 추출하여 verifyAndCompletePayment() 호출
    //    }
}
