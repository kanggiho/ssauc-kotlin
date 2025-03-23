package com.example.ssauc.user.mypage.service

import com.example.ssauc.common.service.CommonUserService
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.mypage.dto.*
import com.example.ssauc.user.mypage.entity.ReputationHistory
import com.example.ssauc.user.mypage.event.ReviewSubmittedEvent
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository
import com.example.ssauc.user.order.entity.Orders
import com.example.ssauc.user.order.repository.OrdersRepository
import com.example.ssauc.user.pay.entity.Review
import com.example.ssauc.user.pay.repository.ReviewRepository
import lombok.RequiredArgsConstructor
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Supplier
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class MypageServiceImpl : MypageService {
    private val commonUserService: CommonUserService? = null
    private val reviewRepository: ReviewRepository? = null
    private val ordersRepository: OrdersRepository? = null
    private val eventPublisher: ApplicationEventPublisher? = null
    private val usersRepository: UsersRepository? = null
    private val reputationHistoryRepository: ReputationHistoryRepository? = null

    // JWT 기반 DB에서 최신 사용자 정보를 조회
    override fun getCurrentUser(email: String?): Users {
        return commonUserService!!.getCurrentUser(email)
    }

    // ===================== 회원 정보 =====================
    override fun getUserInfo(email: String?): Users? {
        return usersRepository!!.findByEmail(email)
            .orElseThrow { RuntimeException("User not found") }
    }

    // userName으로 사용자 정보를 조회하여 DTO로 변환
    override fun getUserInfoJson(userName: String?): ResponseUserInfoDto {
        val user = usersRepository!!.findByUserName(userName)
            .orElseThrow(Supplier { RuntimeException("User not found") })!!

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")

        val formattedCreatedAt = user.createdAt!!.format(formatter)
        val formattedLastLogin = user.lastLogin!!.format(formatter2)

        return ResponseUserInfoDto.builder()
            .userName(user.userName)
            .profileImage(user.profileImage)
            .reputation(user.reputation)
            .location(user.location)
            .createdAt(formattedCreatedAt)
            .lastLogin(formattedLastLogin)
            .reviewSummary(user.reviewSummary)
            .build()
    }

    override fun getReputationHistory(user: Users?): List<ReputationGraphDto> {
        // 각 유저의 전체 평판 기록을 조회
        return reputationHistoryRepository!!.findByUser(user)
            .stream()
            .map { rh: ReputationHistory? ->
                ReputationGraphDto.builder()
                    .createdAt(rh.getCreatedAt())
                    .newScore(rh.getNewScore())
                    .build()
            }
            .sorted(Comparator.comparing { obj: ReputationGraphDto -> obj.createdAt }) // 그래프 그릴 때 시간 순서대로 점 찍기 위해 필요
            .collect(Collectors.toList())
    }

    // ===================== 리뷰 관리 =====================
    // 받은 리뷰 목록 조회
    override fun getReceivedReviews(user: Users, pageable: Pageable?): Page<EvaluationReviewDto> {
        return reviewRepository!!.findByReviewee_UserId(user.userId, pageable)
            .map { review: Review -> convertToDto(review, user.userId) }
    }

    // 작성한 리뷰 목록 조회
    override fun getWrittenReviews(user: Users, pageable: Pageable?): Page<EvaluationReviewDto> {
        return reviewRepository!!.findByReviewer_UserId(user.userId, pageable)
            .map { review: Review -> convertToDto(review, user.userId) }
    }

    // DTO 변환 메서드 (코드 중복 제거)
    private fun convertToDto(review: Review, currentUserId: Long?): EvaluationReviewDto {
        return EvaluationReviewDto.builder()
            .reviewId(review.reviewId)
            .orderId(review.order.orderId)
            .reviewer(review.reviewer.userName)
            .reviewee(review.reviewee.userName)
            .profileImageUrl1(review.reviewer.profileImage)
            .profileImageUrl2(review.reviewee.profileImage)
            .productId(review.order.product.productId)
            .productName(review.order.product.name)
            .productImageUrl(review.order.product.imageUrl!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0])
            .createdAt(review.createdAt)
            .transactionType(getTransactionType(review, currentUserId))
            .build()
    }

    // 거래 타입 (판매 / 구매) 구분
    private fun getTransactionType(review: Review, currentUserId: Long?): String {
        val buyer = review.order.buyer
        return if (buyer.userId == currentUserId) "구매" else "판매"
    }

    // 아직 리뷰를 작성하지 않은 주문 목록 조회
    override fun getPendingReviews(user: Users, pageable: Pageable?): Page<EvaluationPendingDto> {
        // 로그인한 사용자가 아직 리뷰를 작성하지 않은 주문 조회
        val orders = ordersRepository!!.findPendingReviewOrders(user.userId, pageable)
        return orders.map { order: Orders ->
            var reviewTarget = ""
            var profileImage: String? = ""
            var transactionType = ""

            // 주문에서 본인이 buyer면 대상은 seller, 본인이 seller면 대상은 buyer
            if (order.buyer.userId == user.userId) {
                reviewTarget = order.seller.userName
                profileImage = order.seller.profileImage
                transactionType = "구매"
            } else if (order.seller.userId == user.userId) {
                reviewTarget = order.buyer.userName
                profileImage = order.buyer.profileImage
                transactionType = "판매"
            }

            val productImageUrl = order.product.imageUrl
            val mainImage =
                productImageUrl?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(0)
            EvaluationPendingDto.builder()
                .orderId(order.orderId)
                .reviewTarget(reviewTarget)
                .productId(order.product.productId)
                .productName(order.product.name)
                .profileImageUrl(profileImage)
                .productImageUrl(mainImage)
                .orderDate(order.orderDate)
                .transactionType(transactionType)
                .build()
        }
    }

    // 리뷰 제출 처리(baseScore를 서버에서 재계산)
    override fun submitEvaluation(evaluationDto: EvaluationDto, currentUser: Users) {
        val order = ordersRepository!!.findById(evaluationDto.orderId)
            .orElseThrow { RuntimeException("해당 주문 정보가 없습니다.") }

        val reviewee = if (order.buyer.userId == currentUser.userId) order.seller else order.buyer

        // 옵션 값 변환: "positive"이면 true, "negative"이면 false
        val option1 = "positive".equals(evaluationDto.q1, ignoreCase = true)
        val option2 = "positive".equals(evaluationDto.q2, ignoreCase = true)
        val option3 = "positive".equals(evaluationDto.q3, ignoreCase = true)

        // 기본 0.0점에 각 옵션에 대해 positive는 +0.5, negative는 -0.5
        val baseScore = ((if (option1) 0.5 else -0.5)
                + (if (option2) 0.5 else -0.5)
                + (if (option3) 0.5 else -0.5))

        // 상세 후기는 300자 제한 (필요 시 자르기)
        var comment = evaluationDto.reviewContent
        if (comment != null && comment.length > 300) {
            comment = comment.substring(0, 300)
        }

        val review = Review.builder()
            .reviewer(currentUser)
            .reviewee(reviewee)
            .order(order)
            .option1(option1)
            .option2(option2)
            .option3(option3)
            .baseScore(baseScore)
            .comment(comment)
            .createdAt(LocalDateTime.now())
            .build()

        reviewRepository!!.save(review)

        // 리뷰 제출 이벤트 발행
        eventPublisher!!.publishEvent(
            ReviewSubmittedEvent(this, currentUser.userId, reviewee.userId, baseScore, review.createdAt)
        )
    }

    // 리뷰 페이지에 필요한 정보 조회
    override fun getEvaluationData(orderId: Long, currentUser: Users): EvaluationDto {
        val order = ordersRepository!!.findById(orderId)
            .orElseThrow { RuntimeException("해당 주문 정보를 찾을 수 없습니다.") }

        val transactionType: String
        val otherUserName: String
        // 현재 사용자가 buyer이면 거래는 '구매'이며, 리뷰 대상은 seller
        if (order.buyer.userId == currentUser.userId) {
            transactionType = "구매"
            otherUserName = order.seller.userName
        } else if (order.seller.userId == currentUser.userId) {
            transactionType = "판매"
            otherUserName = order.buyer.userName
        } else {
            throw RuntimeException("리뷰 작성 대상 정보가 올바르지 않습니다.")
        }

        // EvaluationDto에 주문/상품 식별자와 거래 유형을 세팅하고, 화면 표시용 상품명과 상대방 이름도 추가
        val evaluationDto = EvaluationDto.builder()
            .orderId(order.orderId)
            .productId(order.product.productId)
            .transactionType(transactionType)
            .build()
        evaluationDto.productName = order.product.name
        evaluationDto.otherUserName = otherUserName
        return evaluationDto
    }

    // 리뷰 상세 정보 조회
    override fun getReviewById(reviewId: Long, currentUserId: Long?): EvaluatedDto {
        val review = reviewRepository!!.findById(reviewId)
            .orElseThrow { RuntimeException("해당 리뷰를 찾을 수 없습니다.") }

        return EvaluatedDto.builder()
            .reviewId(review.reviewId)
            .orderId(review.order.orderId)
            .reviewerName(review.reviewer.userName)
            .revieweeName(review.reviewee.userName)
            .profileImageUrl(review.reviewee.profileImage)
            .productId(review.order.product.productId)
            .productName(review.order.product.name)
            .productImageUrl(review.order.product.imageUrl!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0])
            .createdAt(review.createdAt)
            .transactionType(getTransactionType(review, currentUserId))
            .option1(review.option1)
            .option2(review.option2)
            .option3(review.option3)
            .comment(review.comment)
            .build()
    }
}



