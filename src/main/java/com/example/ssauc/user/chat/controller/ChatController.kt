package com.example.ssauc.user.chat.controller

import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.product.repository.ProductRepository
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
class ChatController {
    private val productRepository: ProductRepository? = null
    private val tokenExtractor: TokenExtractor? = null

    /**
     * /chat/chat?productId=15
     * -> chat.html 뷰로 이동 (Thymeleaf)
     */
    @GetMapping("/chat")
    fun openChatPage(
        @RequestParam(name = "productId") productId: Long,
        model: Model,
        request: HttpServletRequest
    ): String {
        // 1) productId로 상품 조회

        val product = productRepository!!.findById(productId)
            .orElseThrow {
                IllegalArgumentException(
                    "해당 상품이 존재하지 않습니다. productId=$productId"
                )
            }

        // 2) 뷰에서 사용할 데이터 세팅
        model.addAttribute("productId", product.productId)
        model.addAttribute("productName", product.name)


        val user = tokenExtractor!!.getUserFromToken(request)
        model.addAttribute("buyerId", user.userId)


        // 이외에도 필요하면 판매자 이름, 이미지 등 추가 가능
        // model.addAttribute("sellerName", product.getSeller().getUserName());

        // 3) chat.html(또는 chat/chat.html) 템플릿으로 이동
        //   만약 resources/templates/chat/chat.html 파일이라면 "chat/chat" 식으로 반환
        //   만약 templates 폴더 직속에 chat.html이 있다면 "chat"만 반환
        return "chat/chat"
        // 또는 "chat/chat" (본인 프로젝트 구조에 맞춰서)
    }

    @GetMapping("/mychat")
    fun openMyChatPage(@RequestParam userId: Long?, model: Model): String {
        // userId를 Thymeleaf로 넘겨도 되지만, JS에서 URL로 받는다고 했으므로 생략 가능
        model.addAttribute("userId", userId)
        return "chat/mychat" // resources/templates/mychat.html
    }
}