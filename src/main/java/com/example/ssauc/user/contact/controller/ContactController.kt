package com.example.ssauc.user.contact.controller

import com.example.ssauc.user.contact.service.BoardService
import com.example.ssauc.user.login.util.TokenExtractor
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequiredArgsConstructor
@RequestMapping("contact")
class ContactController {
    private val boardService: BoardService? = null
    private val tokenExtractor: TokenExtractor? = null

    @GetMapping("faq")
    fun faq(model: Model): String {
        model.addAttribute("currentPage", "faq")
        return "contact/faq" // faq.html을 렌더링
    }


    @GetMapping("qna")
    fun qna(
        request: HttpServletRequest,  // JWT 인증으로부터 가져옴
        model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request)
            ?: // 로그인 안 된 경우 처리
            return "redirect:/login"
        //로그인 확인

        val latestUser = boardService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser.userName)
        return "contact/qna"
    }

    @GetMapping("chatbot")
    fun chatbot(model: Model): String {
        model.addAttribute("currentPage", "chatbot")
        return "contact/chatbot"
    }
}
