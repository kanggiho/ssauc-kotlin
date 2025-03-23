package com.example.ssauc.user.main.controller;

import com.example.ssauc.common.algorithm.RecommendationAlgorithm;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.main.repository.ProductLikeRepository;
import com.example.ssauc.user.main.service.RecentlyViewedService;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.CategoryRepository;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RecentlyViewedService recentlyViewedService;

    private final TokenExtractor tokenExtractor;



    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        // RecommendationAlgorithm 인스턴스 생성 (빈 관리하는게 좋지만, 예제에서는 수동 생성)
        RecommendationAlgorithm ra = new RecommendationAlgorithm(productLikeRepository, productRepository, categoryRepository);

        // 요청에서 사용자 정보 추출 (없으면 null 반환)
        Users user = tokenExtractor.getUserFromToken(request);
        List<Product> picks = new ArrayList<>();

        if (user != null) {
            long userId = user.getUserId();
            // 추천된 상품의 productId 리스트 조회
            List<Long> pickId = ra.recommendAlgorithm(userId);

            List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);

            model.addAttribute("recentViews", recentlyVieweds);

            // 추천된 각 productId에 대해 Product 객체 조회하여 picks 리스트에 추가
            for (Long id : pickId) {
                productRepository.findById(id).ifPresent(picks::add);
            }
        } else {
            // 사용자 정보가 없으면 productRepository에서 임의의 10개 상품 조회
            // 예시로 Pageable을 사용 (productRepository에 findAll(Pageable pageable) 메서드가 있어야 합니다)
            picks = productRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        }

        // 모델에 ssaucsPickProducts 이름으로 리스트 추가
        model.addAttribute("ssaucsPickProducts", picks);


        List<Product> hots = productRepository.findAllByOrderByLikeCountDesc();
        if(hots.size() > 10) {
            hots.subList(10, hots.size()).clear(); // 인덱스 10부터 마지막까지 삭제
        }

        model.addAttribute("hotsProducts", hots);

        return "index";
    }



    @GetMapping("community")
    public String community() {
        return "community/community";
    }

    @GetMapping("cart")
    public String cart() {
        return "product/cart";
    }
}
