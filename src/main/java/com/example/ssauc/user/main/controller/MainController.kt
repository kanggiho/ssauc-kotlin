package com.example.ssauc.user.main.controller

import com.example.ssauc.common.algorithm.RecommendationAlgorithm
import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.main.repository.ProductLikeRepository
import com.example.ssauc.user.main.service.RecentlyViewedService
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.CategoryRepository
import com.example.ssauc.user.product.repository.ProductRepository
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
@RequiredArgsConstructor
class MainController {
    private val productLikeRepository: ProductLikeRepository? = null
    private val productRepository: ProductRepository? = null
    private val categoryRepository: CategoryRepository? = null
    private val recentlyViewedService: RecentlyViewedService? = null

    private val tokenExtractor: TokenExtractor? = null


    @GetMapping("/")
    fun index(model: Model, request: HttpServletRequest): String {
        // RecommendationAlgorithm 인스턴스 생성 (빈 관리하는게 좋지만, 예제에서는 수동 생성)
        val ra = RecommendationAlgorithm(
            productLikeRepository!!,
            productRepository!!, categoryRepository!!
        )

        // 요청에서 사용자 정보 추출 (없으면 null 반환)
        val user = tokenExtractor!!.getUserFromToken(request)
        var picks: MutableList<Product?> = ArrayList()

        if (user != null) {
            val userId = user.userId!!
            // 추천된 상품의 productId 리스트 조회
            val pickId = ra.recommendAlgorithm(userId)

            val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)

            model.addAttribute("recentViews", recentlyVieweds)

            // 추천된 각 productId에 대해 Product 객체 조회하여 picks 리스트에 추가
            for (id in pickId) {
                productRepository.findById(id).ifPresent { e: Product? ->
                    picks.add(
                        e
                    )
                }
            }
        } else {
            // 사용자 정보가 없으면 productRepository에서 임의의 10개 상품 조회
            // 예시로 Pageable을 사용 (productRepository에 findAll(Pageable pageable) 메서드가 있어야 합니다)
            picks = productRepository.findAll(PageRequest.of(0, 10)).content
        }

        // 모델에 ssaucsPickProducts 이름으로 리스트 추가
        model.addAttribute("ssaucsPickProducts", picks)


        val hots = productRepository.findAllByOrderByLikeCountDesc()
        if (hots.size > 10) {
            hots.subList(10, hots.size).clear() // 인덱스 10부터 마지막까지 삭제
        }

        model.addAttribute("hotsProducts", hots)

        return "index"
    }


    @GetMapping("community")
    fun community(): String {
        return "community/community"
    }

    @GetMapping("cart")
    fun cart(): String {
        return "product/cart"
    }
}
