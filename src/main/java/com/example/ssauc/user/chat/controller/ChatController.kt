package com.example.ssauc.user.chat.controller;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ProductRepository productRepository;
    private final TokenExtractor tokenExtractor;

    /**
     * /chat/chat?productId=15
     * -> chat.html 뷰로 이동 (Thymeleaf)
     */
    @GetMapping("/chat")
    public String openChatPage(@RequestParam(name="productId") Long productId, Model model, HttpServletRequest request) {

        // 1) productId로 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. productId=" + productId));

        // 2) 뷰에서 사용할 데이터 세팅
        model.addAttribute("productId", product.getProductId());
        model.addAttribute("productName", product.getName());


        Users user = tokenExtractor.getUserFromToken(request);
        model.addAttribute("buyerId",user.getUserId());





        // 이외에도 필요하면 판매자 이름, 이미지 등 추가 가능
        // model.addAttribute("sellerName", product.getSeller().getUserName());

        // 3) chat.html(또는 chat/chat.html) 템플릿으로 이동
        //   만약 resources/templates/chat/chat.html 파일이라면 "chat/chat" 식으로 반환
        //   만약 templates 폴더 직속에 chat.html이 있다면 "chat"만 반환
        return "chat/chat";
        // 또는 "chat/chat" (본인 프로젝트 구조에 맞춰서)
    }

    @GetMapping("/mychat")
    public String openMyChatPage(@RequestParam Long userId, Model model) {
        // userId를 Thymeleaf로 넘겨도 되지만, JS에서 URL로 받는다고 했으므로 생략 가능
        model.addAttribute("userId", userId);
        return "chat/mychat"; // resources/templates/mychat.html
    }
}