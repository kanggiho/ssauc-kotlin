package com.example.ssauc.user.bid.controller

import com.example.ssauc.user.bid.dto.AutoBidRequestDto
import com.example.ssauc.user.bid.dto.BidRequestDto
import com.example.ssauc.user.bid.dto.ReportDto
import com.example.ssauc.user.bid.service.BidService
import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.main.service.RecentlyViewedService
import com.example.ssauc.user.recommendation.service.RecommendationService
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/bid")
@RequiredArgsConstructor
class BidController {
    @Autowired
    private val bidService: BidService? = null

    @Autowired
    private val recommendationService: RecommendationService? = null

    @Autowired
    private val recentlyViewedService: RecentlyViewedService? = null

    private val tokenExtractor: TokenExtractor? = null

    @GetMapping("/bid")
    fun bidPage(@RequestParam("productId") productId: Long?, model: Model, request: HttpServletRequest): String {
        val user = tokenExtractor!!.getUserFromToken(request)

        if (user != null) {
            model.addAttribute("tokenId", user.userId)
            model.addAttribute("tokenName", user.userName)
            val isLikeProduct = bidService!!.isProductLike(productId, user.userId)
            model.addAttribute("isLikeProduct", isLikeProduct)

            val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)
            model.addAttribute("recentViews", recentlyVieweds)

            recentlyViewedService.saveViewedProduct(user, productId)
        } else {
            model.addAttribute("tokenId", "guest")
        }


        val dto = bidService!!.getBidInform(productId)

        val carouselImages = bidService.getCarouselImages(productId)

        val product = bidService.getProduct(productId)

        val tempMaxBidUser = bidService.getHighestBidUser(productId)





        model.addAttribute("sellerId", product.seller.userId)

        // 표시할 정보 추가
        model.addAttribute("inform", dto)

        // 상품 정보 추가
        model.addAttribute("productId", productId)

        // 캐러셀 이미지 추가
        model.addAttribute("carouselImages", carouselImages)

        // 현재 최고가 유저 추가
        model.addAttribute("tempMaxBidUser", tempMaxBidUser)

        model.addAttribute("product", product)

        val similarProducts = recommendationService!!.getSimilarProducts(productId)
        model.addAttribute("similarProducts", similarProducts)


        return "bid/bid" // 해당 페이지로 이동
    }


    @GetMapping("report")
    fun report(@RequestParam("reported") productId: Long?, model: Model): String {
        val product = bidService!!.getProduct(productId)
        model.addAttribute("productId", productId)
        model.addAttribute("product", product)

        return "bid/report"
    }

    @PostMapping("report")
    fun reportPost(@RequestBody reportDto: ReportDto, request: HttpServletRequest): ResponseEntity<String> {
        val user = tokenExtractor!!.getUserFromToken(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("로그인이 필요합니다.")

        reportDto.reporterId = user.userId

        bidService!!.insertReportData(reportDto)

        return ResponseEntity.ok("신고가 등록되었습니다.")
    }

    // 1. 입찰 기능: 일반 입찰 요청 처리
    @PostMapping("/place")
    fun placeBid(@RequestBody bidRequestDto: BidRequestDto): ResponseEntity<*> {
        // 예시: 서비스에서 입찰 금액 반영, 입찰 수 증가 등의 로직 처리
        val success = bidService!!.placeBid(bidRequestDto)
        return if (success) {
            ResponseEntity.ok("입찰이 성공적으로 처리되었습니다.")
        } else {
            ResponseEntity.badRequest().body("입찰 처리 중 오류가 발생했습니다.")
        }
    }

    // 2. 자동입찰 기능: 최대 자동 입찰 금액까지 입찰하는 로직 처리
    @PostMapping("/auto")
    fun autoBid(@RequestBody autoBidRequestDto: AutoBidRequestDto): ResponseEntity<*> {
        val success = bidService!!.autoBid(autoBidRequestDto)
        return if (success) {
            ResponseEntity.ok("자동 입찰이 성공적으로 처리되었습니다.")
        } else {
            ResponseEntity.badRequest().body("자동 입찰 처리 중 오류가 발생했습니다.")
        }
    }
}
