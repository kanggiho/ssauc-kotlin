package com.example.ssauc.user.search.controller;


import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.service.JwtService;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.search.service.ProductSearchService;
import com.example.ssauc.user.search.service.SearchLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchpageController {

    private final ProductSearchService productSearchService;
    private final SearchLogService searchLogService;
    private final JwtService jwtService;

    @GetMapping("/search")
    public String searchPage() {
        return "search/search"; // templates/search.html
    }

        @GetMapping("/plp")
    public String showProductListing(@RequestParam String keyword,
                                     Model model,
                                     HttpServletRequest request) {
        Users user = jwtService.extractUser(request);


        // 🔥 디버깅 로그 추가 (사용자 정보 확인)
        System.out.println("🔍 검색어: " + keyword);
        // PLP 페이지에서 검색할 때도 최근 검색어 기록
        if (user != null) {
            System.out.println("✅ 사용자 ID: " + user.getUserId());
        } else {
            System.out.println("⚠️ 사용자 정보 없음 (비로그인 상태)");
        }
        // 검색어 저장은 프론트엔드에서 /api/save-search 호출로 처리하므로 여기서는 호출하지 않음.
        List<Product> products = productSearchService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "search/plp"; // templates/plp.html
    }

}
