package com.example.ssauc.user.contact.controller;

import com.example.ssauc.user.contact.entity.Board;
import com.example.ssauc.user.contact.service.BoardService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contact/qna")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final TokenExtractor tokenExtractor;

    // [POST] 문의 등록 처리
    @PostMapping("/create")
    public String createQna(
            @RequestParam(required = false) String category, //카테고리 필드검증
            @RequestParam String subject,
            @RequestParam String message,
            HttpServletRequest request,// JWT 인증으로부터 가져옴
            Model model
    ) {
        Users user = tokenExtractor.getUserFromToken(request);
        //로그인 확인
        if (user == null) {
            // 로그인 안 된 경우 처리
            return "redirect:/login";
        }
        Users latestUser = boardService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser.getUserName());

        // 2) 필수 값(제목, 내용,카테고리) 검증
        if (subject == null || subject.trim().isEmpty()
                || category == null || category.trim().isEmpty()
                || message == null || message.trim().isEmpty()) {


            // 비어있으면 등록 실패 → 에러 파라미터와 함께 redirect
            return "redirect:/contact/qna?error=emptyFields";
        }


        try {
            // 3) DB 저장
            Board saved = boardService.createBoard(user, subject, message);
            // 4) 성공 시 success 파라미터
            return "redirect:/contact/qna?success=true";
        } catch (Exception e) {
            // 5) 예외 발생 시 error 파라미터
            return "redirect:/contact/qna?error=exception";
        }
    }
}



