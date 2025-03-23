package com.example.ssauc.user.contact.controller

import com.example.ssauc.user.contact.service.BoardService
import com.example.ssauc.user.login.util.TokenExtractor
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/contact/qna")
@RequiredArgsConstructor
class BoardController {
    private val boardService: BoardService? = null
    private val tokenExtractor: TokenExtractor? = null

    // [POST] 문의 등록 처리
    @PostMapping("/create")
    fun createQna(
        @RequestParam(required = false) category: String?,  //카테고리 필드검증
        @RequestParam subject: String?,
        @RequestParam message: String?,
        request: HttpServletRequest,  // JWT 인증으로부터 가져옴
        model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request)
            ?: // 로그인 안 된 경우 처리
            return "redirect:/login"
        //로그인 확인
        val latestUser = boardService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser.userName)

        // 2) 필수 값(제목, 내용,카테고리) 검증
        if (subject == null || subject.trim { it <= ' ' }.isEmpty()
            || category == null || category.trim { it <= ' ' }.isEmpty()
            || message == null || message.trim { it <= ' ' }.isEmpty()
        ) {
            // 비어있으면 등록 실패 → 에러 파라미터와 함께 redirect


            return "redirect:/contact/qna?error=emptyFields"
        }


        try {
            // 3) DB 저장
            val saved = boardService.createBoard(user, subject, message)
            // 4) 성공 시 success 파라미터
            return "redirect:/contact/qna?success=true"
        } catch (e: Exception) {
            // 5) 예외 발생 시 error 파라미터
            return "redirect:/contact/qna?error=exception"
        }
    }
}



