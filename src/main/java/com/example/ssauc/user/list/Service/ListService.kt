package com.example.ssauc.user.list.Service

import com.example.ssauc.user.list.dto.ListDto
import com.example.ssauc.user.list.dto.TempDto
import com.example.ssauc.user.list.dto.WithLikeDto
import com.example.ssauc.user.list.repository.ListRepository
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Slf4j
@Service
class ListService {
    @Autowired
    private val listRepository: ListRepository? = null
    private val usersRepository: UsersRepository? = null

    // JWT 현재 이메일을 기반으로 사용자 정보를 조회
    fun getCurrentUser(email: String?): Users {
        return usersRepository!!.findByEmail(email)
            .orElseThrow { RuntimeException("사용자 정보가 없습니다.3") }
    }

    fun list(pageable: Pageable, user: Users?): Page<TempDto> {
        val list = if (user != null) {
            listRepository!!.getWithLikeProductList(user.userId, pageable)
        } else {
            listRepository!!.getAllProductsWithoutUser(pageable) // 로그인 안 한 경우 전체 상품 목록
        }

        val tempList = list!!.content.stream().map { listDto: WithLikeDto? ->
            val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
            val days = duration.toDays().toInt()
            val hours = duration.toHours().toInt() % 24
            val bidCount = "입찰 %d회".formatted(listDto.getBidCount())
            var inform = "⏳ %d일 %d시간".formatted(days, hours)
            val like = addCommas(listDto.getLikeCount().toString())
            val price = addCommas(listDto.getPrice().toString())
            val mainImage = listDto.getImageUrl().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val status = listDto.getStatus()

            if (days < 0 || hours < 0 || status == "판매완료") { // 마감된 경우
                inform = "⏳ 입찰 마감"
            }
            TempDto.builder()
                .productId(listDto.getProductId())
                .imageUrl(mainImage[0])
                .name(listDto.getName())
                .price(price)
                .bidCount(bidCount)
                .gap(inform)
                .location(listDto.getLocation())
                .likeCount(like)
                .liked(user != null && listDto.isLiked()) // 로그인 안 하면 liked는 false로 설정
                .status(listDto.getStatus())
                .build()
        }.toList()

        return PageImpl(tempList, pageable, list.totalElements)
    }

    fun likelist(pageable: Pageable, user: Users): Page<TempDto> {
        val list = listRepository!!.getLikeList(user.userId, pageable)


        val tempList = list!!.content.stream().map { listDto: WithLikeDto? ->
            val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
            val days = duration.toDays().toInt()
            val hours = duration.toHours().toInt() % 24
            val bidCount = "입찰 %d회".formatted(listDto.getBidCount())
            var inform = "⏳ %d일 %d시간".formatted(days, hours)
            val like = addCommas(listDto.getLikeCount().toString())
            val price = addCommas(listDto.getPrice().toString())
            val mainImage = listDto.getImageUrl().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val status = listDto.getStatus()

            if (days < 0 || hours < 0 || status == "판매완료") { // 마감된 경우
                inform = "⏳ 입찰 마감"
            }
            TempDto.builder()
                .productId(listDto.getProductId())
                .imageUrl(mainImage[0])
                .name(listDto.getName())
                .price(price)
                .bidCount(bidCount)
                .gap(inform)
                .location(listDto.getLocation())
                .likeCount(like)
                .liked(listDto.isLiked())
                .status(listDto.getStatus())
                .build()
        }.toList()

        return PageImpl(tempList, pageable, list.totalElements)
    }

    fun categoryList(pageable: Pageable, user: Users?, categoryId: Long?): Page<TempDto> {
        val list: Page<WithLikeDto?>?

        if (user != null) {
            val id = user.userId
            list = listRepository!!.getCategoryList(id, categoryId, pageable)
        } else {
            list = listRepository!!.getCategoryListWithoutUser(categoryId, pageable)
        }

        val tempList = list!!.content.stream().map { listDto: WithLikeDto? ->
            val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
            val days = duration.toDays().toInt()
            val hours = duration.toHours().toInt() % 24
            val bidCount = "입찰 %d회".formatted(listDto.getBidCount())
            var inform = "⏳ %d일 %d시간".formatted(days, hours)
            val like = addCommas(listDto.getLikeCount().toString())
            val price = addCommas(listDto.getPrice().toString())
            val mainImage = listDto.getImageUrl().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val status = listDto.getStatus()

            if (days < 0 || hours < 0 || status == "판매완료") { // 마감된 경우
                inform = "⏳ 입찰 마감"
            }
            TempDto.builder()
                .productId(listDto.getProductId())
                .imageUrl(mainImage[0])
                .name(listDto.getName())
                .price(price)
                .bidCount(bidCount)
                .gap(inform)
                .location(listDto.getLocation())
                .likeCount(like)
                .liked(listDto.isLiked())
                .status(listDto.getStatus())
                .build()
        }.toList()

        return PageImpl(tempList, pageable, list.totalElements)
    }

    fun getProductsByPrice(pageable: Pageable, user: Users?, minPrice: Int, maxPrice: Int): Page<TempDto> {
        val list: Page<WithLikeDto?>?

        if (user != null) {
            val userId = user.userId
            list = listRepository!!.findByPriceRange(userId, minPrice, maxPrice, pageable)
        } else {
            list = listRepository!!.findByPriceRangeWithUserId(minPrice, maxPrice, pageable)
        }

        val tempList = list!!.content.stream().map { listDto: WithLikeDto? ->
            val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
            val days = duration.toDays().toInt()
            val hours = duration.toHours().toInt() % 24
            val bidCount = "입찰 %d회".formatted(listDto.getBidCount())
            var inform = "⏳ %d일 %d시간".formatted(days, hours)
            val like = addCommas(listDto.getLikeCount().toString())
            val price = addCommas(listDto.getPrice().toString())
            val mainImage = listDto.getImageUrl().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val status = listDto.getStatus()

            if (days < 0 || hours < 0 || status == "판매완료") { // 마감된 경우
                inform = "⏳ 입찰 마감"
            }
            TempDto.builder()
                .productId(listDto.getProductId())
                .imageUrl(mainImage[0])
                .name(listDto.getName())
                .price(price)
                .bidCount(bidCount)
                .gap(inform)
                .location(listDto.getLocation())
                .likeCount(like)
                .liked(listDto.isLiked())
                .status(listDto.getStatus())
                .build()
        }.toList()

        return PageImpl(tempList, pageable, list.totalElements)
    }

    fun getAvailableBidWithLike(pageable: Pageable, user: Users): Page<TempDto> {
        val list = listRepository!!.getAvailableProductListWithLike(user.userId, pageable)

        val tempList = list!!.content.stream()
            .filter { listDto: WithLikeDto? ->  // 마감되지 않은 상품만 필터링
                val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
                val days = duration.toDays().toInt()
                val hours = duration.toHours().toInt() % 24
                days > 0 || (days == 0 && hours > 0)
            }
            .map { listDto: WithLikeDto? ->
                val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
                val days = duration.toDays().toInt()
                val hours = duration.toHours().toInt() % 24
                val bidCount = "입찰 %d회".formatted(listDto.getBidCount())
                val inform = "⏳ %d일 %d시간".formatted(days, hours)
                val like = addCommas(listDto.getLikeCount().toString())
                val price = addCommas(listDto.getPrice().toString())
                val mainImage = listDto.getImageUrl().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                TempDto.builder()
                    .productId(listDto.getProductId())
                    .imageUrl(mainImage[0])
                    .name(listDto.getName())
                    .price(price)
                    .bidCount(bidCount)
                    .gap(inform)
                    .location(listDto.getLocation())
                    .likeCount(like)
                    .liked(listDto.isLiked())
                    .status(listDto.getStatus())
                    .build()
            }.toList()

        return PageImpl(tempList, pageable, list.totalElements)
    }

    fun getAvailableBid(pageable: Pageable): Page<TempDto> {
        val list = listRepository!!.getAvailableProductList(pageable)

        val tempList = list!!.content.stream()
            .map { listDto: ListDto? ->
                val duration = Duration.between(LocalDateTime.now(), listDto.getEndAt())
                val days = duration.toDays().toInt()
                val hours = duration.toHours().toInt() % 24
                val bidCount = "입찰 %d회".formatted(listDto.getBidCount())
                val inform = "⏳ %d일 %d시간".formatted(days, hours)
                val like = addCommas(listDto.getLikeCount().toString())
                val price = addCommas(listDto.getPrice().toString())
                val mainImage = listDto.getImageUrl().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                TempDto.builder()
                    .productId(listDto.getProductId())
                    .imageUrl(mainImage[0])
                    .name(listDto.getName())
                    .price(price)
                    .bidCount(bidCount)
                    .gap(inform)
                    .location(listDto.getLocation())
                    .likeCount(like)
                    .status(listDto.getStatus())
                    .build()
            }.toList()

        return PageImpl(tempList, pageable, list.totalElements)
    }

    companion object {
        fun addCommas(number: String): String {
            try {
                val value = number.toDouble() // 실수도 처리 가능
                return String.format("%,.0f", value) // 천 단위마다 콤마 추가
            } catch (e: NumberFormatException) {
                return "Invalid number format"
            }
        }
    }
}