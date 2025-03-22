package com.example.ssauc.user.cash.controller;

import com.example.ssauc.exception.PortoneVerificationException;
import com.example.ssauc.user.cash.dto.*;
import com.example.ssauc.user.cash.entity.Charge;
import com.example.ssauc.user.cash.entity.Withdraw;
import com.example.ssauc.user.cash.service.CashService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    private final TokenExtractor tokenExtractor;

    @GetMapping("/cash")
    public String cashPage(@RequestParam(value = "filter", required = false, defaultValue = "payment") String filter,
                           @RequestParam(value = "startDate", required = false) String startDateStr,
                           @RequestParam(value = "endDate", required = false) String endDateStr,
                           @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                           HttpServletRequest request,
                           Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = cashService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);

        if (latestUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("filter", filter);

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (startDateStr != null && !startDateStr.isEmpty() && endDateStr != null && !endDateStr.isEmpty()) {
            // 변환 예시: 시작일은 00:00, 종료일은 23:59:59로 설정
            startDate = LocalDate.parse(startDateStr).atStartOfDay();
            endDate = LocalDate.parse(endDateStr).atTime(23, 59, 59);
        }

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 이번 달 환급 신청 횟수를 requested_at 기준으로 계산
        // 이번 달 환급 신청 횟수를 서비스에서 가져옴
        int currentWithdrawCount = cashService.getCurrentWithdrawCount(latestUser);
        model.addAttribute("currentWithdrawCount", currentWithdrawCount);

        // 날짜 필터가 있는 경우와 없는 경우를 분기해서 처리
        if ("charge".equals(filter)) {
            // 충전 내역
            pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "chargeId"));
            Page<ChargeDto> chargePage;
            long totalAmount = 0;
            if (startDate != null && endDate != null) { // 날짜 필터 적용 o
                chargePage = cashService.getChargesByUser(latestUser, startDate, endDate, pageable);
                totalAmount = cashService.getTotalChargeAmount(latestUser, startDate, endDate);
            } else { // 날짜 필터 적용 x
                chargePage = cashService.getChargesByUser(latestUser, pageable);
                totalAmount = cashService.getTotalChargeAmount(latestUser);
            }
            model.addAttribute("chargeList", chargePage.getContent());
            model.addAttribute("totalPages", chargePage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalAmount", totalAmount);
        } else if ("withdraw".equals(filter)) {
            // 환급 내역
            pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "withdrawId"));
            Page<WithdrawDto> withdrawPage;
            long totalAmount = 0;
            if (startDate != null && endDate != null) {
                withdrawPage = cashService.getWithdrawsByUser(latestUser, startDate, endDate, pageable);
                totalAmount = cashService.getTotalWithdrawAmount(latestUser, startDate, endDate);
            } else {
                withdrawPage = cashService.getWithdrawsByUser(latestUser, pageable);
                totalAmount = cashService.getTotalWithdrawAmount(latestUser);
            }
            model.addAttribute("withdrawList", withdrawPage.getContent());
            model.addAttribute("totalPages", withdrawPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalAmount", totalAmount);
        } else if ("payment".equals(filter)) {
            // 결제 내역 (구매한 주문만)
            pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "orderId"));
            Page<CalculateDto> calculatePage;
            long totalAmount = 0;
            if (startDate != null && endDate != null) {
                calculatePage = cashService.getPaymentCalculatesByUser(latestUser, startDate, endDate, pageable);
                totalAmount = cashService.getTotalPaymentAmount(latestUser, startDate, endDate);
            } else {
                calculatePage = cashService.getPaymentCalculatesByUser(latestUser, pageable);
                totalAmount = cashService.getTotalPaymentAmount(latestUser);
            }
            model.addAttribute("calculateList", calculatePage.getContent());
            model.addAttribute("totalPages", calculatePage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalAmount", totalAmount);
        } else if ("settlement".equals(filter)) {
            // 정산 내역 (판매한 주문만)
            pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "orderId"));
            Page<CalculateDto> calculatePage;
            long totalAmount = 0;
            if (startDate != null && endDate != null) {
                calculatePage = cashService.getSettlementCalculatesByUser(latestUser, startDate, endDate, pageable);
                totalAmount = cashService.getTotalSettlementAmount(latestUser, startDate, endDate);
            } else {
                calculatePage = cashService.getSettlementCalculatesByUser(latestUser, pageable);
                totalAmount = cashService.getTotalSettlementAmount(latestUser);
            }
            model.addAttribute("calculateList", calculatePage.getContent());
            model.addAttribute("totalPages", calculatePage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalAmount", totalAmount);
        }

        // startDateStr와 endDateStr도 모델에 담으면 페이지 링크에 유지할 수 있음
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        return "cash/cash";
    }


    // 충전 옵션 정보 (예: 기본 충전 옵션)
    @GetMapping("/api/info")
    @ResponseBody
    public ChargingDto getChargingInfo() {
        // 필요에 따라 동적으로 변경 가능
        return ChargingDto.builder()
                .id("charge")
                .name("쏙머니 충전")
                .price(10000)
                .currency("KRW")
                .build();
    }

    // 결제 완료 처리 엔드포인트
    @PostMapping("/api/complete")
    @ResponseBody
    public ResponseEntity<?> completePayment(@RequestBody ChargeRequestDto request,
                                             HttpServletRequest request2) {
        Users user = tokenExtractor.getUserFromToken(request2);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Users latestUser = cashService.getCurrentUser(user.getEmail());
            // 결제 검증 및 완료 처리 (사용자 충전 기록 업데이트)
            Charge charge = cashService.verifyAndCompletePayment(request.getPaymentId(), request.getAmount(), latestUser);
            return ResponseEntity.ok(new ChargeResponseDto("PAID", charge.getChargeId()));
        } catch (PortoneVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

//    // 웹훅 처리 엔드포인트 (선택사항)
//    @PostMapping("/api/webhook")
//    @ResponseBody
//    public ResponseEntity<?> handleWebhook(@RequestHeader("webhook-id") String webhookId,
//                                           @RequestHeader("webhook-timestamp") String webhookTimestamp,
//                                           @RequestHeader("webhook-signature") String webhookSignature,
//                                           @RequestBody String body) {
//        try {
//            cashService.handleWebhook(body, webhookId, webhookTimestamp, webhookSignature);
//            return ResponseEntity.ok("Webhook processed");
//        } catch (PortoneVerificationException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }

    // 환급 신청 처리 엔드포인트 추가
    @PostMapping("/api/withdraw")
    @ResponseBody
    public ResponseEntity<?> requestWithdraw(@RequestBody WithdrawRequestDto request,
                                             HttpServletRequest request2) {
        Users user = tokenExtractor.getUserFromToken(request2);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Users latestUser = cashService.getCurrentUser(user.getEmail());
            cashService.requestWithdraw(latestUser, request.getAmount(), request.getBank(), request.getAccount());
            return ResponseEntity.ok("환급 요청이 접수되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("환급 요청 처리 중 오류 발생");
        }
    }

}
