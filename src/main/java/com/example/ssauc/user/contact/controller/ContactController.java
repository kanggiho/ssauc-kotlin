package com.example.ssauc.user.contact.controller;

import com.example.ssauc.user.contact.service.BoardService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("contact")
public class ContactController {

    private final BoardService boardService;
    private final TokenExtractor tokenExtractor;

    @GetMapping("faq")
    public String faq(Model model) {
        model.addAttribute("currentPage", "faq");
        return "contact/faq"; // faq.html을 렌더링
    }


    @GetMapping("qna")
    public String qna(HttpServletRequest request,// JWT 인증으로부터 가져옴
                      Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        //로그인 확인
        if (user == null) {
            // 로그인 안 된 경우 처리
            return "redirect:/login";
        }

        Users latestUser = boardService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser.getUserName());
        return "contact/qna";
    }

    @GetMapping("chatbot")
    public String chatbot(Model model) {
        model.addAttribute("currentPage", "chatbot");
        return "contact/chatbot";
    }
}
