package com.example.ssauc.user.history.service

import com.example.ssauc.common.service.CommonUserService
import com.example.ssauc.user.bid.entity.Bid
import com.example.ssauc.user.bid.entity.ProductReport
import com.example.ssauc.user.bid.repository.AutoBidRepository
import com.example.ssauc.user.bid.repository.BidRepository
import com.example.ssauc.user.bid.repository.ProductReportRepository
import com.example.ssauc.user.chat.entity.Ban
import com.example.ssauc.user.chat.entity.Report
import com.example.ssauc.user.chat.repository.BanRepository
import com.example.ssauc.user.chat.repository.ReportRepository
import com.example.ssauc.user.history.dto.*
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.mypage.event.OrderCompletedEvent
import com.example.ssauc.user.mypage.event.OrderShippedEvent
import com.example.ssauc.user.order.entity.Orders
import com.example.ssauc.user.order.repository.OrdersRepository
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.ProductRepository
import lombok.RequiredArgsConstructor
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
@RequiredArgsConstructor
class HistoryService {
    private val commonUserService: CommonUserService? = null
    private val productRepository: ProductRepository? = null
    private val ordersRepository: OrdersRepository? = null
    private val banRepository: BanRepository? = null
    private val bidRepository: BidRepository? = null
    private val autoBidRepository: AutoBidRepository? = null
    private val reportRepository: ReportRepository? = null
    private val productReportRepository: ProductReportRepository? = null
    private val eventPublisher: ApplicationEventPublisher? = null

    // 세션에서 전달된 userId를 이용하여 DB에서 최신 사용자 정보를 조회합니다.
    fun getCurrentUser(email: String?): Users {
        return commonUserService!!.getCurrentUser(email)
    }

    // ===================== 차단 관리 =====================
    // 차단 리스트
    @Transactional(readOnly = true)
    fun getBanListForUser(userId: Long?, pageable: Pageable?): Page<BanHistoryDto> {
        val bans = banRepository!!.findByUserUserId(userId, pageable)
        return bans!!.map<BanHistoryDto> { ban: Ban? ->
            BanHistoryDto(
                ban.getBanId(),
                ban.getBlockedUser().getUserName(),
                ban.getBlockedUser().getProfileImage(),
                ban.getBlockedAt()
            )
        }
    }

    // 차단 해제
    @Transactional
    fun unbanUser(banId: Long, userId: Long?) {
        // 차단 내역 소유 여부 확인 후 삭제 처리
        val ban = banRepository!!.findById(banId)
            .orElseThrow {
                IllegalArgumentException(
                    "유효하지 않은 차단 내역입니다. banId=$banId"
                )
            }!!
        require(ban.getUser().getUserId().equals(userId)) { "차단 해제 권한이 없습니다." }
        banRepository.delete(ban)
    }

    // ===================== 신고 내역 =====================
    // 상품 신고 리스트 조회
    @Transactional(readOnly = true)
    fun getProductReportHistoryPage(reporter: Users?, pageable: Pageable?): Page<ProductReportDto> {
        val reports = productReportRepository!!.findByReporter(reporter, pageable)

        return reports!!.map<ProductReportDto> { report: ProductReport? ->
            val productImageUrl: String = report.getProduct().getImageUrl()
            val mainImage =
                if (productImageUrl != null) productImageUrl.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0] else null
            ProductReportDto.builder()
                .reportId(report!!.reportId)
                .productId(report.getProduct().getProductId())
                .productName(report.getProduct().getName())
                .productImageUrl(mainImage)
                .reportReason(report.reportReason)
                .reportDate(report.reportDate)
                .processedAt(report.processedAt)
                .status(report.status)
                .build()
        }
    }

    // 유저 신고 리스트 조회
    @Transactional(readOnly = true)
    fun getUserReportHistoryPage(reporter: Users?, pageable: Pageable?): Page<UserReportDto> {
        val reports = reportRepository!!.findByReporter(reporter, pageable)
        return reports!!.map<UserReportDto> { report: Report? ->
            UserReportDto.builder()
                .reportId(report.getReportId())
                .reportedUserName(report.getReportedUser().getUserName())
                .profileImageUrl(report.getReportedUser().getProfileImage())
                .reportReason(report.getReportReason())
                .reportDate(report.getReportDate())
                .processedAt(report.getProcessedAt())
                .status(report.getStatus())
                .build()
        }
    }

    // 신고 상세 내역
    @Transactional(readOnly = true)
    fun getReportDetail(filter: String, id: Long): ReportDetailDto {
        if ("product" == filter) {
            val productReportOpt = productReportRepository!!.findById(id)
            // 하나의 메서드 내에서 두 테이블(ProductReport와 Report)을 순차적으로 확인하는 구조라 if‑present로 분기 처리
            if (productReportOpt.isPresent) {
                val pr = productReportOpt.get()
                return ReportDetailDto.builder()
                    .type("product")
                    .productId(pr.getProduct().getProductId())
                    .productName(pr.getProduct().getName())
                    .reportReason(pr.reportReason)
                    .details(pr.details)
                    .reportDate(pr.reportDate)
                    .status(pr.status)
                    .processedAt(pr.processedAt)
                    .build()
            } else {
                throw RuntimeException("상품 신고 내역이 존재하지 않습니다. id=$id")
            }
        } else if ("user" == filter) {
            val reportOpt = reportRepository!!.findById(id)
            if (reportOpt.isPresent) {
                val r = reportOpt.get()
                return ReportDetailDto.builder()
                    .type("user")
                    .reportedUserName(r.getReportedUser().getUserName())
                    .reportReason(r.getReportReason())
                    .details(r.getDetails())
                    .reportDate(r.getReportDate())
                    .status(r.getStatus())
                    .processedAt(r.getProcessedAt())
                    .build()
            } else {
                throw RuntimeException("유저 신고 내역이 존재하지 않습니다. id=$id")
            }
        } else {
            throw IllegalArgumentException("유효하지 않은 신고 유형: $filter")
        }
    }

    // ===================== 판매 내역 =====================
    // 판매중 리스트
    @Transactional(readOnly = true)
    fun getOngoingSellHistoryPage(seller: Users?, pageable: Pageable?): Page<SellHistoryOngoingDto> {
        // 판매중 리스트: 경매 마감 시간에 10분을 더한 값이 현재 시간보다 아직 지나지 않은 상품
        // 즉, 상품의 endAt >= (현재 시간 - 10분)인 경우만 포함
        val cutoff = LocalDateTime.now().minusMinutes(10)
        val productsPage =
            productRepository!!.findBySellerAndStatusAndEndAtGreaterThanEqual(seller, "판매중", cutoff, pageable)

        return productsPage.map { p: Product ->
            val productImageUrl = p.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            SellHistoryOngoingDto.builder()
                .productId(p.productId)
                .productName(p.name)
                .productImageUrl(mainImage)
                .tempPrice(p.tempPrice)
                .price(p.price)
                .createdAt(p.createdAt)
                .endAt(p.endAt)
                .build()
        }
    }

    // 판매 마감 리스트
    @Transactional(readOnly = true)
    fun getEndedSellHistoryPage(seller: Users?, pageable: Pageable?): Page<SellHistoryOngoingDto> {
        // 판매 마감 리스트: 판매중 상태에서 (경매 마감 시간 + 10분)이 이미 지난 상품
        // 즉, 상품의 endAt < (현재 시간 - 10분)인 경우
        val cutoff = LocalDateTime.now().minusMinutes(10)
        val productsPage = productRepository!!.findBySellerAndStatusAndEndAtBefore(seller, "판매중", cutoff, pageable)
        return productsPage.map { p: Product ->
            val productImageUrl = p.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            SellHistoryOngoingDto.builder()
                .productId(p.productId)
                .productName(p.name)
                .productImageUrl(mainImage)
                .tempPrice(p.tempPrice)
                .price(p.price)
                .createdAt(p.createdAt)
                .endAt(p.endAt)
                .build()
        }
    }

    // 판매 완료 리스트
    @Transactional(readOnly = true)
    fun getCompletedSellHistoryPage(seller: Users?, pageable: Pageable?): Page<SellHistoryCompletedDto> {
        val productsPage = productRepository!!.findBySellerAndStatus(seller, "판매완료", pageable)
        return productsPage.map { p: Product ->
            // 판매 완료된 상품의 주문 정보는 보통 하나가 존재한다고 가정합니다.
            val order = p.orders!!.stream().findFirst()
                .orElseThrow { RuntimeException("판매 완료 상품에 주문 정보가 없습니다.") }

            val productImageUrl = p.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            SellHistoryCompletedDto.builder()
                .orderId(order.orderId)
                .productId(p.productId)
                .productImageUrl(mainImage)
                .productName(p.name)
                .buyerName(order.buyer.userName)
                .profileImageUrl(order.buyer.profileImage)
                .totalPrice(order.totalPrice)
                .orderDate(order.orderDate)
                .completedDate(order.completedDate)
                .build()
        }
    }

    // 판매 완료 상세 내역
    @Transactional(readOnly = true)
    fun getSoldDetailByProductId(productId: Long): SoldDetailDto {
        val product = productRepository!!.findById(productId)
            .orElseThrow { RuntimeException("해당 상품이 존재하지 않습니다.") }

        val order = product.orders!!.stream().findFirst()
            .orElseThrow { RuntimeException("해당 상품의 주문 정보가 없습니다.") }

        return SoldDetailDto.builder()
            .productId(product.productId)
            .productName(product.name)
            .startPrice(product.startPrice)
            .createdAt(product.createdAt)
            .dealType(product.dealType)
            .imageUrl(product.imageUrl)
            .orderId(order.orderId)
            .buyerName(order.buyer.userName)
            .totalPrice(order.totalPrice)
            .recipientName(order.recipientName)
            .recipientPhone(order.recipientPhone)
            .postalCode(order.postalCode)
            .deliveryAddress(order.deliveryAddress)
            .deliveryStatus(order.deliveryStatus)
            .orderDate(order.orderDate)
            .completedDate(order.completedDate)
            .build()
    }

    // 운송장 번호 등록 또는 수정
    @Transactional
    fun updateDeliveryStatus(orderId: Long, trackingNumber: String?): Boolean {
        val optionalOrder = ordersRepository!!.findById(orderId)
        if (optionalOrder.isPresent) {
            val orders = optionalOrder.get()
            orders.deliveryStatus = trackingNumber
            ordersRepository.save(orders)

            // 운송장 등록 시각
            val now = LocalDateTime.now()

            // 24시간 내 운송장 등록 이벤트 발행
            // 주문일과 비교하여 24시간 이내이면 이벤트 발행
            if (Duration.between(orders.orderDate, now).toHours() < 24) {
                eventPublisher!!.publishEvent(
                    OrderShippedEvent(this, orders.seller.userId, orders.orderDate, now)
                )
            }
            return true
        }
        return false
    }

    // ===================== 구매 내역 =====================
    // 입찰중 리스트 (Bid 테이블에서 구매자가 입찰한 내역 조회)
    @Transactional(readOnly = true)
    fun getBiddingHistoryPage(buyer: Users?, pageable: Pageable?): Page<BuyBidHistoryDto> {
        val now = LocalDateTime.now()

        val bidPage = bidRepository!!.findLatestBidsByUser(buyer, now, pageable)

        return bidPage!!.map<BuyBidHistoryDto> { bid: Bid? ->
            // 해당 입찰에 대해, 활성화된 AutoBid 엔티티를 조회 (없으면 null)
            val autoBid = autoBidRepository!!.findByUserAndProductAndActive(buyer, bid.getProduct(), true)
            val maxBidAmount = if ((autoBid != null)) autoBid.maxBidAmount else null

            val productImageUrl: String = bid.getProduct().getImageUrl()
            val mainImage =
                if (productImageUrl != null) productImageUrl.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0] else null
            BuyBidHistoryDto.builder()
                .bidId(bid!!.bidId)
                .productId(bid.getProduct().getProductId())
                .productName(bid.getProduct().getName())
                .productImageUrl(mainImage)
                .sellerName(bid.getProduct().getSeller().getUserName())
                .profileImageUrl(bid.getProduct().getSeller().getProfileImage())
                .endAt(bid.getProduct().getEndAt())
                .bidPrice(bid.bidPrice)
                .maxBidAmount(maxBidAmount)
                .tempPrice(bid.getProduct().getTempPrice())
                .build()
        }
    }

    // 구매 완료 리스트 (구매자 기준)
    @Transactional(readOnly = true)
    fun getPurchaseHistoryPage(buyer: Users?, pageable: Pageable?): Page<BuyHistoryDto> {
        // Orders에서 buyer가 일치하는 주문을 조회합니다.
        val ordersPage = ordersRepository!!.findByBuyer(buyer, pageable)


        return ordersPage.map { order: Orders ->
            val productImageUrl = order.product.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            BuyHistoryDto.builder()
                .orderId(order.orderId)
                .productId(order.product.productId)
                .productName(order.product.name)
                .productImageUrl(mainImage)
                .sellerName(order.seller.userName)
                .profileImageUrl(order.seller.profileImage)
                .totalPrice(order.totalPrice)
                .orderDate(order.orderDate)
                .completedDate(order.completedDate)
                .build()
        }
    }

    // 구매 내역 상세 (특정 상품의 구매 내역, 구매자 기준)
    @Transactional(readOnly = true)
    fun getBoughtDetailByProductId(productId: Long, buyer: Users): BoughtDetailDto {
        // Product 조회
        val product = productRepository!!.findById(productId)
            .orElseThrow { RuntimeException("해당 상품이 존재하지 않습니다.") }
        // 해당 상품의 주문 중 구매자가 일치하는 주문 찾기
        val order = product.orders!!.stream()
            .filter { o: Orders -> o.buyer.userId == buyer.userId }
            .findFirst()
            .orElseThrow { RuntimeException("해당 상품의 주문 정보가 없습니다.") }
        return BoughtDetailDto.builder()
            .orderId(order.orderId)
            .productId(product.productId)
            .productName(product.name)
            .startPrice(product.startPrice)
            .createdAt(product.createdAt)
            .dealType(product.dealType)
            .imageUrl(product.imageUrl)
            .sellerName(order.seller.userName)
            .totalPrice(order.totalPrice)
            .recipientName(order.recipientName)
            .recipientPhone(order.recipientPhone)
            .postalCode(order.postalCode)
            .deliveryAddress(order.deliveryAddress)
            .orderDate(order.orderDate)
            .completedDate(order.completedDate)
            .deliveryStatus(order.deliveryStatus)
            .build()
    }

    // 구매 내역 상세에서 거래 완료 기능
    @Transactional
    fun completeOrder(orderId: Long, user: Users) {
        val order = ordersRepository!!.findById(orderId)
            .orElseThrow { RuntimeException("주문 정보가 존재하지 않습니다.") }
        // 구매자 본인 여부 확인
        if (order.buyer.userId != user.userId) {
            throw RuntimeException("주문 완료 권한이 없습니다.")
        }
        order.orderStatus = "완료"
        order.completedDate = LocalDateTime.now()
        ordersRepository.save(order)

        // 주문 완료 이벤트 발행 (reputation)
        eventPublisher!!.publishEvent(
            OrderCompletedEvent(
                this,
                order.buyer.userId,
                order.seller.userId,
                order.completedDate
            )
        )
    }
}
