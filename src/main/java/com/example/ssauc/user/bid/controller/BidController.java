package com.example.ssauc.user.bid.controller;


import com.example.ssauc.user.bid.dto.*;
import com.example.ssauc.user.bid.service.BidService;
import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.main.service.RecentlyViewedService;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.recommendation.dto.RecommendationDto;
import com.example.ssauc.user.recommendation.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bid")
@RequiredArgsConstructor
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private RecentlyViewedService recentlyViewedService;

    private final TokenExtractor tokenExtractor;

    @GetMapping("/bid")
    public String bidPage(@RequestParam("productId") Long productId, Model model, HttpServletRequest request) {

        Users user = tokenExtractor.getUserFromToken(request);

        if (user  != null) {
            model.addAttribute("tokenId", user.getUserId());
            model.addAttribute("tokenName", user.getUserName());
            Boolean isLikeProduct = bidService.isProductLike(productId, user.getUserId());
            model.addAttribute("isLikeProduct", isLikeProduct);

            List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);
            model.addAttribute("recentViews", recentlyVieweds);

            recentlyViewedService.saveViewedProduct(user, productId);
        } else {
            model.addAttribute("tokenId", "guest");
        }


        ProductInformDto dto = bidService.getBidInform(productId);

        List<CarouselImage> carouselImages = bidService.getCarouselImages(productId);

        Product product = bidService.getProduct(productId);

        String tempMaxBidUser = bidService.getHighestBidUser(productId);





        model.addAttribute("sellerId", product.getSeller().getUserId());

        // 표시할 정보 추가
        model.addAttribute("inform", dto);

        // 상품 정보 추가
        model.addAttribute("productId", productId);

        // 캐러셀 이미지 추가
        model.addAttribute("carouselImages", carouselImages);

        // 현재 최고가 유저 추가
        model.addAttribute("tempMaxBidUser", tempMaxBidUser);

        model.addAttribute("product", product);

        List<RecommendationDto> similarProducts = recommendationService.getSimilarProducts(productId);
        model.addAttribute("similarProducts", similarProducts);


        return "bid/bid"; // 해당 페이지로 이동
    }


    @GetMapping("report")
    public String report(@RequestParam("reported") Long productId, Model model) {
        Product product = bidService.getProduct(productId);
        model.addAttribute("productId", productId);
        model.addAttribute("product", product);

        return "bid/report";
    }

    @PostMapping("report")
    public ResponseEntity<String> reportPost(@RequestBody ReportDto reportDto, HttpServletRequest request) {

        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        reportDto.setReporterId(user.getUserId());

        bidService.insertReportData(reportDto);

        return ResponseEntity.ok("신고가 등록되었습니다.");
    }

    // 1. 입찰 기능: 일반 입찰 요청 처리
    @PostMapping("/place")
    public ResponseEntity<?> placeBid(@RequestBody BidRequestDto bidRequestDto) {
        // 예시: 서비스에서 입찰 금액 반영, 입찰 수 증가 등의 로직 처리
        boolean success = bidService.placeBid(bidRequestDto);
        if (success) {
            return ResponseEntity.ok("입찰이 성공적으로 처리되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("입찰 처리 중 오류가 발생했습니다.");
        }
    }

    // 2. 자동입찰 기능: 최대 자동 입찰 금액까지 입찰하는 로직 처리
    @PostMapping("/auto")
    public ResponseEntity<?> autoBid(@RequestBody AutoBidRequestDto autoBidRequestDto) {
        boolean success = bidService.autoBid(autoBidRequestDto);
        if (success) {
            return ResponseEntity.ok("자동 입찰이 성공적으로 처리되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("자동 입찰 처리 중 오류가 발생했습니다.");
        }
    }


}
