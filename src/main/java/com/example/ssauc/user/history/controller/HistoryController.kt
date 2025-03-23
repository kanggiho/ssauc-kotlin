package com.example.ssauc.user.history.controller;

import com.example.ssauc.user.bid.dto.CarouselImage;
import com.example.ssauc.user.history.dto.*;
import com.example.ssauc.user.history.service.HistoryService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    private final TokenExtractor tokenExtractor;

    @Value("${smarttracker.apiKey}")
    private String smartTrackerApiKey;

    // ===================== 차단 관리 =====================
    // 차단 내역 페이지: 로그인한 사용자의 차단 내역 조회
    @GetMapping("/ban")
    public String banPage(@RequestParam(defaultValue = "1") int page,
                          HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        // 페이지 번호는 0부터 시작하므로 (page - 1)로 변환
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<BanHistoryDto> banPage = historyService.getBanListForUser(latestUser.getUserId(), pageable);
        model.addAttribute("banList", banPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", banPage.getTotalPages());
        return "history/ban";
    }

    // 차단 해제 요청 처리
    @PostMapping("/ban/unban")
    public String unbanUser(@RequestParam("banId") Long banId,
                            HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        historyService.unbanUser(banId, latestUser.getUserId());
        return "redirect:/history/ban";
    }

    // ===================== 신고 내역 =====================
    // 신고 내역 리스트
    @GetMapping("/report")
    public String reportPage(@RequestParam(value = "filter", required = false, defaultValue = "product") String filter,
                             @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                             HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        int pageSize = 10;
        if ("product".equals(filter)) {
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "reportDate"));
            Page<ProductReportDto> productReports = historyService.getProductReportHistoryPage(latestUser, pageable);
            model.addAttribute("list", productReports.getContent());
            model.addAttribute("totalPages", productReports.getTotalPages());
        } else if ("user".equals(filter)) {
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "reportDate"));
            Page<UserReportDto> userReports = historyService.getUserReportHistoryPage(latestUser, pageable);
            model.addAttribute("list", userReports.getContent());
            model.addAttribute("totalPages", userReports.getTotalPages());
        }
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        return "history/report";
    }

    // 신고 상세 내역
    @GetMapping("/reported")
    public String reportedPage(@RequestParam("filter") String filter, // 신고 유형
                               @RequestParam("id") Long id,
                               HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        // 신고 상세 내역 조회 (신고 유형에 따라 다르게 조회)
        ReportDetailDto reportDetail = historyService.getReportDetail(filter, id);
        model.addAttribute("reportDetail", reportDetail);

        return "history/reported";
    }

    // ===================== 판매 내역 =====================
    // 판매 현황 리스트 (판매중, 만료, 완료)
    @GetMapping("/sell")
    public String sellPage(@RequestParam(value = "filter", required = false, defaultValue = "ongoing") String filter,
                           @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                           HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        int pageSize = 10;
        if ("ongoing".equals(filter)) {
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<SellHistoryOngoingDto> ongoingPageData = historyService.getOngoingSellHistoryPage(latestUser, pageable);
            model.addAttribute("list", ongoingPageData.getContent());
            model.addAttribute("totalPages", ongoingPageData.getTotalPages());
        } else if ("ended".equals(filter)) {
            // 판매 마감 리스트: 판매중 상태에서 (경매 마감 시간 + 30분) < 현재 시간인 상품
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<SellHistoryOngoingDto> endedPageData = historyService.getEndedSellHistoryPage(latestUser, pageable);
            model.addAttribute("list", endedPageData.getContent());
            model.addAttribute("totalPages", endedPageData.getTotalPages());
        } else if ("completed".equals(filter)) {
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<SellHistoryCompletedDto> completedPageData = historyService.getCompletedSellHistoryPage(latestUser, pageable);
            model.addAttribute("list", completedPageData.getContent());
            model.addAttribute("totalPages", completedPageData.getTotalPages());
        }
        model.addAttribute("currentPage", page);
        model.addAttribute("filter", filter);
        return "history/sell";
    }
    // 판매 내역 상세 페이지
    @GetMapping("/sold")
    public String soldPage(@RequestParam("id") Long productId,
                           HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        // 상품 및 주문 상세 정보 조회 (SoldDetailDto를 이용)
        SoldDetailDto soldDetail = historyService.getSoldDetailByProductId(productId);
        model.addAttribute("soldDetail", soldDetail);

        // imageUrl을 쉼표로 분리하여 CarouselImage 리스트 생성 (BidService와 유사한 로직)
        String imageUrlStr = soldDetail.getImageUrl();  // SoldDetailDto의 imageUrl 필드 (쉼표로 구분된 문자열)
        String[] urls = imageUrlStr.split(",");
        List<CarouselImage> carouselImages = new ArrayList<>();
        int i = 1;
        for (String url : urls) {
            CarouselImage image = new CarouselImage();
            image.setUrl(url.trim());
            image.setAlt("Slide " + i);
            i++;
            carouselImages.add(image);
        }
        model.addAttribute("carouselImages", carouselImages);

        return "history/sold";
    }

    // 운송장 번호 등록
    @PostMapping("/sold/update-tracking")
    @ResponseBody
    public Map<String, Object> updateDeliveryStatus(@RequestParam("orderId") Long orderId,
                                                    @RequestParam("newTracking") String newTracking) {
        boolean updated = historyService.updateDeliveryStatus(orderId, newTracking);
        Map<String, Object> result = new HashMap<>();
        result.put("updated", updated);
        return result;
    }

    // ===================== 구매 내역 =====================
    // 구매 내역 리스트 (완료)
    @GetMapping("/buy")
    public String buyPage(@RequestParam(value = "filter", required = false, defaultValue = "bidding") String filter,
                          @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                          HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        int pageSize = 10;

        if ("complete".equals(filter)) {
            // 구매 완료 필터 (default)
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"));
            Page<BuyHistoryDto> purchasePage = historyService.getPurchaseHistoryPage(latestUser, pageable);
            model.addAttribute("list", purchasePage.getContent());
            model.addAttribute("totalPages", purchasePage.getTotalPages());
        } else {
            // 입찰중 필터: Bid 테이블 기준
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "product.endAt"));
            Page<BuyBidHistoryDto> biddingPage = historyService.getBiddingHistoryPage(latestUser, pageable);
            model.addAttribute("list", biddingPage.getContent());
            model.addAttribute("totalPages", biddingPage.getTotalPages());
        }
        model.addAttribute("currentPage", page);
        model.addAttribute("filter", filter);
        return "history/buy";
    }

    // 구매 내역 상세 페이지
    @GetMapping("/bought")
    public String boughtPage(@RequestParam("id") Long productId,
                             HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        // 구매 내역 상세 조회 (구매자 기준)
        BoughtDetailDto boughtDetail = historyService.getBoughtDetailByProductId(productId, latestUser);
        model.addAttribute("boughtDetail", boughtDetail);

        // 이미지 Carousel 생성 (BidService와 유사한 로직)
        String imageUrlStr = boughtDetail.getImageUrl();  // 쉼표로 구분된 문자열
        String[] urls = imageUrlStr.split(",");
        List<CarouselImage> carouselImages = new ArrayList<>();
        int i = 1;
        for (String url : urls) {
            CarouselImage image = new CarouselImage();
            image.setUrl(url.trim());
            image.setAlt("Slide " + i);
            i++;
            carouselImages.add(image);
        }
        model.addAttribute("carouselImages", carouselImages);
        model.addAttribute("apiKey", smartTrackerApiKey);
        return "history/bought";
    }

    // 배송 조회 프록시 (클라이언트에서 호출)
    @PostMapping("/tracking-proxy")
    @ResponseBody
    public String trackingProxy(@RequestParam("t_code") String t_code,
                                @RequestParam("t_invoice") String t_invoice) {
        // (사용할 템플릿의 코드(1: Cyan, 2: Pink, 3: Gray, 4: Tropical, 5: Sky)를 URL 경로에 추가)
        String url = "https://info.sweettracker.co.kr/tracking/5";
        // 요청 파라미터 구성 (API KEY는 서버에서 주입)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("t_key", smartTrackerApiKey);
        formData.add("t_code", t_code);
        formData.add("t_invoice", t_invoice);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, formData, String.class);
        String body = response.getBody();

        // <head> 태그가 있다면 <base> 태그 삽입
        if (body != null && body.contains("<head>")) {
            body = body.replace("<head>", "<head><base href=\"https://info.sweettracker.co.kr/\">");
        }
        return body;
    }

    // 거래 완료 요청 처리
    @PostMapping("/bought/complete")
    @ResponseBody
    public String completeOrder(@RequestParam("orderId") Long orderId,
                                HttpServletRequest request) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = historyService.getCurrentUser(user.getEmail());
        historyService.completeOrder(orderId, latestUser);
        return "완료";
    }

}
