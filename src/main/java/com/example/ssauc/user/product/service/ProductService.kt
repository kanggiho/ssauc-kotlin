package com.example.ssauc.user.product.service

import com.example.ssauc.common.service.CommonUserService
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.dto.ProductInsertDto
import com.example.ssauc.user.product.dto.ProductUpdateDto
import com.example.ssauc.user.product.entity.Category
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.entity.Product.description
import com.example.ssauc.user.product.entity.Product.endAt
import com.example.ssauc.user.product.entity.Product.imageUrl
import com.example.ssauc.user.product.entity.Product.minIncrement
import com.example.ssauc.user.product.entity.Product.name
import com.example.ssauc.user.product.entity.Product.price
import com.example.ssauc.user.product.entity.Product.seller
import com.example.ssauc.user.product.entity.Product.startPrice
import com.example.ssauc.user.product.entity.Product.tempPrice
import com.example.ssauc.user.product.repository.CategoryRepository
import com.example.ssauc.user.product.repository.ProductRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@RequiredArgsConstructor
class ProductService {
    private val commonUserService: CommonUserService? = null
    private val productRepository: ProductRepository? = null
    private val categoryRepository: CategoryRepository? = null


    // JWT 현재 이메일을 기반으로 사용자 정보를 조회
    fun getCurrentUser(email: String?): Users {
        return commonUserService!!.getCurrentUser(email)
    }

    // 새로운 상품을 등록
    fun insertProduct(dto: ProductInsertDto, seller: Users?): Product {
        // 카테고리 검증: categoryName으로 조회
        val category = categoryRepository!!.findByName(dto.categoryName)
            .orElseThrow { IllegalArgumentException("유효하지 않은 카테고리입니다.") }

        // 마감 시간(LocalDateTime) 계산: auctionDate, auctionHour, auctionMinute 사용
        val closingDate = LocalDate.parse(dto.auctionDate)
        val closingTime = LocalTime.of(dto.auctionHour, dto.auctionMinute)
        val auctionClosingDateTime = LocalDateTime.of(closingDate, closingTime)

        // Product 엔티티 빌더 사용
        val product: Product = Product.builder()
            .seller(seller)
            .category(category)
            .name(dto.name)
            .description(dto.description)
            .price(dto.price)
            .tempPrice(dto.startPrice) // 이거 tempPrice 라고 안하고 startPrice로 한게 잘못한게 아님 초기값은 현재입찰가 == 초기입찰가 같아서 이렇게 함
            .startPrice(dto.startPrice)
            .imageUrl(dto.imageUrl)
            .status("판매중")
            .createdAt(LocalDateTime.now())
            .endAt(auctionClosingDateTime)
            .viewCount(0L)
            .minIncrement(dto.minIncrement)
            .dealType(dto.dealType)
            .build()
        return productRepository!!.save(product)
    }

    // 상품 ID를 기반으로 상품 정보를 조회
    fun getProductById(productId: Long): Product {
        return productRepository!!.findById(productId)
            .orElseThrow { RuntimeException("상품을 찾을 수 없습니다.") }!!
    }

    val allCategories: List<Category?>?
        // 모든 상품 카테고리 목록을 조회
        get() = categoryRepository!!.findAll()

    // 특정 상품에 입찰이 존재하는지 확인
    fun hasBids(productId: Long): Boolean {
        // Product 엔티티의 bidCount 필드를 이용하거나 bidRepository를 이용하여 해당 상품의 입찰 건수가 있는지 체크
        val product = getProductById(productId)
        return product.bidCount > 0
    }

    // 상품 정보 수정
    fun updateProduct(dto: ProductUpdateDto, seller: Users) {
        val product = getProductById(dto.productId)
        if (product.seller!!.userId != seller.userId) {
            throw RuntimeException("수정 권한이 없습니다.")
        }
        // 필요에 따라 카테고리 조회
        val category = categoryRepository!!.findByName(dto.categoryName)
            .orElseThrow { IllegalArgumentException("유효하지 않은 카테고리입니다.") }

        // 상품 정보 업데이트
        product.name = dto.name
        product.description = dto.description
        product.price = dto.price
        product.startPrice = dto.startPrice
        product.tempPrice = dto.startPrice // 초기 입찰가와 동일하게
        product.imageUrl = dto.imageUrl
        product.minIncrement = dto.minIncrement
        product.setDealType(dto.dealType)

        // 경매 종료 시간 업데이트 (auctionDate, auctionHour, auctionMinute 이용)
        val closingDate = LocalDate.parse(dto.auctionDate)
        val closingTime = LocalTime.of(dto.auctionHour, dto.auctionMinute)
        product.endAt = LocalDateTime.of(closingDate, closingTime)

        product.setCategory(category)
        productRepository!!.save(product)
    }

    // 상품 삭제
    fun deleteProduct(productId: Long, seller: Users) {
        val product = getProductById(productId)
        if (product.seller!!.userId != seller.userId) {
            throw RuntimeException("삭제 권한이 없습니다.")
        }
        productRepository!!.delete(product)
    }
}
