package com.example.ssauc.user.bid.service

import com.example.ssauc.user.bid.dto.*
import com.example.ssauc.user.bid.dto.AutoBidRequestDto.maxBidAmount
import com.example.ssauc.user.bid.dto.AutoBidRequestDto.productId
import com.example.ssauc.user.bid.dto.BidRequestDto.productId
import com.example.ssauc.user.bid.entity.AutoBid
import com.example.ssauc.user.bid.entity.AutoBid.maxBidAmount
import com.example.ssauc.user.bid.entity.AutoBid.user
import com.example.ssauc.user.bid.entity.Bid
import com.example.ssauc.user.bid.entity.Bid.user
import com.example.ssauc.user.bid.entity.ProductReport
import com.example.ssauc.user.bid.repository.AutoBidRepository
import com.example.ssauc.user.bid.repository.BidRepository
import com.example.ssauc.user.bid.repository.PdpRepository
import com.example.ssauc.user.bid.repository.ProductReportRepository
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.main.entity.Notification
import com.example.ssauc.user.main.repository.NotificationRepository
import com.example.ssauc.user.main.repository.ProductLikeRepository
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.ProductRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.function.Supplier
import java.util.stream.Collectors
import kotlin.math.max

@Service
class BidService {
    @Autowired
    private val pdpRepository: PdpRepository? = null

    @Autowired
    private val bidRepository: BidRepository? = null

    @Autowired
    private val productReportRepository: ProductReportRepository? = null

    @Autowired
    private val usersRepository: UsersRepository? = null

    @Autowired
    private val autoBidRepository: AutoBidRepository? = null

    @Autowired
    private val productLikeRepository: ProductLikeRepository? = null

    @Autowired
    private val productRepository: ProductRepository? = null

    @Autowired
    private val notificationRepository: NotificationRepository? = null


    fun getBidInform(productId: Long?): ProductInformDto {
        // pdp에 들어갈 정보 가져오기
        val informDto = pdpRepository!!.getPdpInform(productId)

        // 전체 시간 구하기
        val totalTime = Duration.between(LocalDateTime.now(), informDto!!.endAt).seconds
        informDto.totalTime = totalTime
        return informDto
    }

    fun getCarouselImages(productId: Long?): List<CarouselImage> {
        // pdp에 들어갈 정보 가져오기
        val informDto = pdpRepository!!.getPdpInform(productId)

        val CarouselImages: MutableList<CarouselImage> = ArrayList()
        val urls = informDto!!.imageUrl!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var i = 1
        for (url in urls) {
            val image = CarouselImage()
            image.url = url
            image.alt = "Slide $i"
            i++
            CarouselImages.add(image)
        }
        return CarouselImages
    }

    fun insertReportData(dto: ReportDto) {
        val reportedUserId = pdpRepository!!.findById(dto.getProductId()).get().seller.userId
        dto.setReportedUserId(reportedUserId)

        val productReport: ProductReport = ProductReport.builder()
            .product(pdpRepository.findById(dto.getProductId()).orElseThrow())
            .reporter(usersRepository!!.findById(dto.getReporterId()).orElseThrow())
            .reportedUser(usersRepository.findById(dto.getReportedUserId()).orElseThrow())
            .reportReason(dto.getReportReason())
            .status("처리전")
            .details(dto.getDetails())
            .reportDate(LocalDateTime.now())
            .processedAt(null)
            .build()

        productReportRepository!!.save(productReport)
    }

    fun getProduct(productId: Long): Product {
        return pdpRepository!!.findById(productId).orElseThrow()!!
    }

    fun getUser(userId: Long): Users {
        return usersRepository!!.findById(userId).orElseThrow()
    }


    // 일반 입찰 기능 구현
    @Transactional
    fun placeBid(bidRequestDto: BidRequestDto): Boolean {
        val bidProduct = pdpRepository!!.findProductForUpdate(bidRequestDto.productId)
            .orElseThrow(Supplier { EntityNotFoundException("Product not found") })!!
        val bidUser = getUser(bidRequestDto.userId!!.toLong())
        val bidAmount = bidRequestDto.bidAmount.toLong()

        // 일반 입찰이 유효한지 검증 및 현재가 업데이트
        val updatedRows = pdpRepository.updateProductField(bidAmount, bidRequestDto.productId)
        if (updatedRows == 0) {
            // 입찰 조건이 맞지 않아 업데이트가 이루어지지 않았을 경우 false 반환
            return false
        }

        // Bid 객체 만들고 저장
        val bid: Bid = Bid.builder()
            .product(bidProduct)
            .user(bidUser)
            .bidPrice(bidAmount)
            .bidTime(LocalDateTime.now())
            .build()
        bidRepository!!.save(bid)
        setAdditionalTime(bidRequestDto.productId!!)

        overBidNotice(bidProduct.productId, bidUser.userId)


        return true
    }


    // 자동 입찰 기능 구현
    @Transactional
    fun autoBid(autoBidRequestDto: AutoBidRequestDto): Boolean {
        val productId = autoBidRequestDto.productId
        val userIdStr = autoBidRequestDto.userId
        val maxBidAmount = autoBidRequestDto.maxBidAmount.toLong()

        val product = pdpRepository!!.findProductForUpdate(autoBidRequestDto.productId)
            .orElseThrow(Supplier { EntityNotFoundException("Product not found") })!!
        val user = getUser(userIdStr!!.toLong())

        // 이미 같은 (user, product)에 대한 자동입찰이 있는지 확인
        val existingAuto = autoBidRepository
            .findByProductAndActiveIsTrue(product)
            .stream()
            .filter { ab: AutoBid -> ab.user!!.userId == user.userId }
            .findFirst()
            .orElse(null)

        if (existingAuto != null) {
            // 기존에 등록한 자동입찰이 있을 경우 maxBidAmount 갱신
            existingAuto.setMaxBidAmount(maxBidAmount)
            autoBidRepository!!.save(existingAuto)
        } else {
            // 없으면 새로 생성
            val newAutoBidding: AutoBid = AutoBid.builder()
                .product(product)
                .user(user)
                .maxBidAmount(maxBidAmount)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build()
            autoBidRepository!!.save(newAutoBidding)
        }

        // 이제 등록만 하는 것이 아니라,
        // 혹시 새로 등록된/갱신된 maxBidAmount가 현재가보다 높다면 자동으로 입찰 동작을 실행한다.
        //processAutoBidding(productId);
        return true
    }


    @Transactional
    fun processAutoBidding(productId: Long) {
        // 1) 해당 상품을 비관적 락으로 조회 (다른 트랜잭션에서 동시에 변경하지 못하게)
        val product = pdpRepository!!.findProductForUpdate(productId)
            .orElseThrow(Supplier { EntityNotFoundException("Product not found") })!!
        val currentPrice = product.tempPrice // 현재가
        val minIncrement = product.minIncrement.toLong() // 최소 증가 금액

        // 2) 해당 상품의 active=true인 자동입찰 목록 조회
        val autoBidders = autoBidRepository!!.findByProductAndActiveIsTrue(product)
        if (autoBidders!!.isEmpty()) {
            return  // 자동입찰자가 없으면 로직 종료
        }

        // 3) 자동입찰자들을 maxBidAmount 기준 내림차순 정렬
        autoBidders.sort(java.util.Comparator { a: AutoBid, b: AutoBid -> b.maxBidAmount!!.compareTo(a.maxBidAmount!!) })

        // 4) 최고 자동입찰자(1등)와 해당 사용자가 설정한 최대 자동입찰 금액
        val topBidder = autoBidders[0]
        val topMax = topBidder!!.maxBidAmount

        // 5) 가장 최근의 입찰 내역 조회
        val lastBid = bidRepository!!.findTopByProductOrderByBidTimeDesc(product)!!.orElse(null)

        // ★ 추가된 부분: 단일 자동입찰자인 경우,
        // 만약 이미 입찰 내역이 있다면(즉, lastBid가 null이 아니라면) 추가 입찰을 진행하지 않음.
        if (autoBidders.size == 1 && lastBid != null) {
            // 단일 자동입찰자인데 이미 입찰한 상태라면 더 이상 입찰하지 않음
            return
        }

        // 6) 이미 최고입찰자인 경우에는 자동입찰 진행하지 않음 (경쟁 입찰자가 있는 경우만 진행)
        if (lastBid != null && lastBid.user!!.userId == topBidder.user!!.userId) {
            return
        }

        val secondMax: Long
        // 7) 경쟁자가 두 명 이상일 경우,
        // 두 번째 최고 자동입찰 금액과 현재가 중 더 큰 값을 2등으로 간주
        if (autoBidders.size >= 2) {
            // 상품의 최신 현재가를 다시 조회 (변경되었을 가능성 고려)
            val updatedCurrentPrice = pdpRepository.findById(productId)
                .orElseThrow {
                    EntityNotFoundException(
                        "Product not found"
                    )
                }!!.tempPrice
            secondMax =
                max(autoBidders[1]!!.maxBidAmount!!.toDouble(), updatedCurrentPrice.toDouble()).toLong()
        } else {
            // 단일 자동입찰자일 경우, 경쟁자가 없으므로 현재가를 2등으로 간주
            secondMax = currentPrice
        }

        // 8) 새 입찰 가격 계산: 2등 가격에 최소 증가분을 더함
        var desiredPrice = secondMax + minIncrement
        // 1등의 최대 자동입찰 금액을 초과하면 1등의 한도까지만 입찰
        if (desiredPrice > topMax!!) {
            desiredPrice = topMax
        }

        // 9) 계산된 새 가격이 현재가보다 높을 경우에만 입찰 진행
        if (desiredPrice > currentPrice) {
            // 상품의 현재가를 새 입찰가로 업데이트
            pdpRepository.updateProductField(desiredPrice, productId)

            // 새 입찰 내역 생성 및 저장
            val newBid: Bid = Bid.builder()
                .product(product)
                .user(topBidder.user)
                .bidPrice(desiredPrice)
                .bidTime(LocalDateTime.now())
                .build()
            bidRepository.save(newBid)

            // 입찰 시 추가 시간을 부여하는 로직 실행
            setAdditionalTime(productId)

            // 초과 입찰 알림 실행
            overBidNotice(productId, topBidder.user!!.userId)
        }
    }


    // 스케줄러 메서드: 매 분 0초마다 자동입찰 로직 실행
    @Scheduled(cron = "0 * * * * *") // 초, 분, 시, 일, 월, 요일 순 (매 분 0초 실행)
    @Transactional
    fun scheduledAutoBidding() {
        //System.out.println("실행됨");
        //System.out.println(pdpRepository.findById(56L).orElseThrow().getTempPrice());


        // active 상태인 자동입찰 데이터들에서 상품 목록 추출


        val activeAutoBids: List<AutoBid> = autoBidRepository!!.findAll().stream()
            .filter(AutoBid::isActive)
            .toList()

        val productSet = activeAutoBids.stream()
            .map<Any>(AutoBid::getProduct)
            .collect(Collectors.toSet<Any>())

        // 각 상품에 대해 자동입찰 로직 실행
        for (product in productSet) {
            processAutoBidding(product.productId)
        }
        deadlineBidNotice()
    }


    fun deadlineBidNotice() {
        val products = productRepository!!.findAll()

        for (product in products) {
            val gap = Duration.between(LocalDateTime.now(), product.endAt).toSeconds().toInt()
            if (1770 < gap && gap < 1830) {
                val users = bidRepository!!.findUserIdsByProductId(product.productId)

                val message = product.name + " 상품의 입찰마감시간이 30분 남았습니다."

                for (user in users!!) {
                    val notification = Notification
                        .builder()
                        .user(user)
                        .type("입찰마감")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .readStatus(1)
                        .build()
                    notificationRepository!!.save(notification)
                }
            }
        }
    }


    fun getHighestBidUser(productId: Long?): String {
        val tempUserId = bidRepository!!.findUserIdWithHighestBidPrice(productId)

        if (tempUserId == null) {
            return "입찰자 없음"
        } else {
            val user = usersRepository!!.findById(tempUserId).orElseThrow()
            return user.userName
        }
    }

    fun isProductLike(productId: Long?, userId: Long?): Boolean {
        return productLikeRepository!!.countByProductIdAndUserId(productId, userId) > 0
    }

    // 종료 5분전 입찰 시 10분 추가
    fun setAdditionalTime(productId: Long) {
        val now = LocalDateTime.now()
        val product = pdpRepository!!.findById(productId).orElseThrow()!!
        val seconds = Duration.between(now, product.endAt).seconds
        if (seconds <= 300) {
            extendProductEndAt(productId, 600)
        }
    }

    fun extendProductEndAt(productId: Long, addTime: Long) {
        val product = pdpRepository!!.findById(productId)
            .orElseThrow { IllegalArgumentException("상품을 찾을 수 없습니다.") }!!

        val newEndAt = product.endAt.plusSeconds(addTime)

        // JPQL을 통해 업데이트
        pdpRepository.updateEndAt(productId, newEndAt)
    }

    fun overBidNotice(productId: Long, userId: Long?) {
        // 해당 productId와 현재 입찰한 userId가 아닌 사용자들에게 알림 생성

        val overwhelmedUser = bidRepository!!.findDistinctUserIdByProductIdAndNotUserId(productId, userId)

        // 상품 이름 조회
        val productName = productRepository!!.findById(productId)
            .orElseThrow { RuntimeException("Product not found") }.name

        for (over in overwhelmedUser!!) {
            val message = "$productName 상품이 상회 입찰 되었습니다."

            // 먼저, 해당 사용자(over)에 대해 동일 조건의 알림이 존재하는지 확인
            val existingNotificationOpt = notificationRepository
                .findNotification(over, message, "상회입찰", 1)

            if (existingNotificationOpt.isPresent) {
                // 이미 존재하면 readStatus 업데이트만 수행
                val existingNotification = existingNotificationOpt.get()
                existingNotification.readStatus = 0
                notificationRepository!!.save(existingNotification)
            } else {
                // 존재하지 않으면 새 알림 생성
                val targetUser = usersRepository!!.findById(over)
                    .orElseThrow { RuntimeException("User not found") }

                val notification = Notification.builder()
                    .user(targetUser)
                    .type("상회입찰")
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .readStatus(1)
                    .build()
                notificationRepository!!.save(notification)
            }
        }
    }
}
