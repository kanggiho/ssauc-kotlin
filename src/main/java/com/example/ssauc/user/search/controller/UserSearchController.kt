package com.example.ssauc.user.search.controller

import com.example.ssauc.user.product.entity.Category
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.CategoryRepository
import com.example.ssauc.user.product.repository.ProductRepository
import com.example.ssauc.user.search.dto.ProductDTO
import com.example.ssauc.user.search.service.ProductSearchService
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors
import kotlin.math.ceil


/**
 * 실제 서비스에서 사용하는 상품 컨트롤러 (검색/카테고리/목록 조회)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class UserSearchController {
    private val productRepository: ProductRepository? = null
    private val categoryRepository: CategoryRepository? = null
    private val productSearchService: ProductSearchService? = null

    /**
     * [사용자용] 간단한 검색 (ES에서 Product를 조회)
     */
    @GetMapping("/search")
    fun searchProducts(
        @RequestParam keyword: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "30") size: Int
    ): List<Product?> {
        return productSearchService!!.searchProducts(keyword)
    }

    @get:GetMapping("/categories")
    val categories: ResponseEntity<List<String?>>
        /**
         * (중복 제거) 카테고리 조회는 여기서 제공
         */
        get() {
            val list = categoryRepository!!.findAll().stream()
                .map { cat: Category -> cat.name }
                .collect(Collectors.toList())
            return ResponseEntity.ok(list)
        }

    /**
     * 필터 / 정렬 / 페이지네이션 플로우
     * 실제 프론트엔드(PLP)에서 호출하는 API
     */
    @GetMapping("/plp")
    fun getFilteredProducts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "30") size: Int,
        @RequestParam(defaultValue = "VIEW_DESC") sort: String,
        @RequestParam(required = false) auctionOnly: Boolean?,
        @RequestParam(required = false) categories: List<String?>?,
        @RequestParam(required = false) minPrice: Long?,
        @RequestParam(required = false) maxPrice: Long?
    ): ResponseEntity<Map<String, Any>> {
        // 페이징 & 정렬

        val pageable: Pageable = PageRequest.of(page - 1, size, getSortOrder(sort))

        // 다양한 필터를 종합적으로 처리하는 JPA 쿼리
        val found = productRepository!!.findByFilters(keyword, auctionOnly, categories, minPrice, maxPrice, pageable)

        // DTO 변환
        val products = found!!.stream()
            .map<ProductDTO> { p: Product? ->
                ProductDTO(
                    p!!.productId,
                    p.name,
                    p.description,
                    if (p.getCategory() != null) p.getCategory().getName() else "카테고리 없음",
                    p.price,
                    p.startPrice,
                    p.tempPrice,
                    p.minIncrement,
                    p.bidCount,
                    p.likeCount,
                    if ((p.imageUrl != null)) p.imageUrl else "/img/noimage.png",
                    p.viewCount,
                    p.status,
                    if ((p.seller != null && p.seller!!.location != null)) p.seller!!.location else "위치정보 없음"
                )
            }
            .collect(Collectors.toList<ProductDTO>())

        // 페이지네이션 계산
        val totalCount = products.size
        val totalPages = ceil(totalCount.toDouble() / size) as Int

        val result: MutableMap<String, Any> = HashMap()
        result["products"] = products
        result["totalCount"] = totalCount
        result["page"] = page
        result["totalPages"] = totalPages

        return ResponseEntity.ok(result)
    }

    /**
     * 상품 클릭 시 조회수 증가 (ES 문서도 업데이트)
     */
    @PostMapping("/click/{productId}")
    fun incrementClick(@PathVariable productId: Long) {
        productSearchService!!.incrementProductClick(productId)
    }

    /**
     * 정렬 옵션 헬퍼 메서드
     */
    private fun getSortOrder(sort: String): Sort {
        return when (sort) {
            "PRICE_ASC" -> Sort.by(
                Sort.Direction.ASC,
                "price"
            )

            "PRICE_DESC" -> Sort.by(
                Sort.Direction.DESC,
                "price"
            )

            "BID_DESC" -> Sort.by(
                Sort.Direction.DESC,
                "bidCount"
            )

            "LIKE_DESC" -> Sort.by(
                Sort.Direction.DESC,
                "likeCount"
            )

            "RECENT" -> Sort.by(
                Sort.Direction.DESC,
                "createdAt"
            )

            else -> Sort.by(
                Sort.Direction.DESC,
                "viewCount"
            )
        }
    }
}
