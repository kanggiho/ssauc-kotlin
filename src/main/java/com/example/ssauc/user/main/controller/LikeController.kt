package com.example.ssauc.user.main.controller

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.main.service.LikeService
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
class LikeController {
    private val likeService: LikeService? = null
    private val tokenExtractor: TokenExtractor? = null

    @PostMapping("/like")
    fun toggleLike(
        request: HttpServletRequest,
        @RequestBody requestData: Map<String?, Any>
    ): ResponseEntity<Map<String, Any>> {
        var user: Users? = null

        try {
            user = tokenExtractor!!.getUserFromToken(request)
        } catch (e: Exception) {
            LikeController.log.error(e.message)
        }

        val userId = user!!.userId
            ?: return ResponseEntity.status(401).body(
                java.util.Map.of<String, Any>(
                    "success",
                    false,
                    "message",
                    "로그인이 필요합니다."
                )
            )

        val productId = requestData["productId"].toString().toLong()
        val isLiked = likeService!!.toggleLike(userId, productId)

        return ResponseEntity.ok().body(
            java.util.Map.of<String, Any>(
                "success", true,
                "liked", isLiked,
                "message", if (isLiked) "좋아요 추가됨" else "좋아요 취소됨"
            )
        )
    }
}