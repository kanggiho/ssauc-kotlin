package com.example.ssauc.user.main.service

import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.main.entity.ProductLike
import com.example.ssauc.user.main.repository.ProductLikeRepository
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.ProductRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@RequiredArgsConstructor
class LikeService {
    private val productLikeRepository: ProductLikeRepository? = null
    private val productRepository: ProductRepository? = null
    private val usersRepository: UsersRepository? = null

    @Transactional // ?
    fun toggleLike(userId: Long, productId: Long): Boolean {
        val users = usersRepository!!.findById(userId)
            .orElseThrow { IllegalArgumentException("유저가 존재하지 않습니다.") }!!

        val product = productRepository!!.findById(productId)
            .orElseThrow { IllegalArgumentException("상품이 존재하지 않습니다.") }

        val existingLike = productLikeRepository!!.findByUserAndProduct(users, product) // 좋아요 여부 체크

        if (existingLike!!.isPresent) {
            // 이미 좋아요를 눌렀다면 취소
            productLikeRepository.delete(existingLike.get())
            productRepository.findById(productId).ifPresent { item: Product ->
                item.setLikeCount(item.likeCount - 1)
                productRepository.save(item)
            }
            return false
        } else {
            val newLike: ProductLike = ProductLike.builder().user
            (users)
                .product(product)
                .likedAt(LocalDateTime.now())
                .build()

            productLikeRepository.save(newLike)
            productRepository.findById(productId).ifPresent { item: Product ->
                item.setLikeCount(item.likeCount + 1)
                productRepository.save(item)
            }

            return true
        }
    }
}