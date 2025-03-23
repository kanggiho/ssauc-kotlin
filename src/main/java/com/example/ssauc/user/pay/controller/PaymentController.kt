package com.example.ssauc.user.pay.controller;

import com.example.ssauc.user.order.dto.OrderRequestDto;
import com.example.ssauc.user.pay.service.PaymentService;
import com.example.ssauc.user.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/pay")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    // POST 요청 처리: 결제 처리 로직 수행
    @PostMapping("/pay")
    public String processPayment(OrderRequestDto orderRequestDto, RedirectAttributes redirectAttributes) {
        // 결제 처리 로직 실행

        // 현재 날짜와 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime now1 = LocalDateTime.now();
        LocalDateTime now2 = LocalDateTime.now();
        // 원하는 형식의 포매터 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // LocalDateTime을 문자열로 포맷팅
        String formattedDateTime = now.format(formatter);
        String confirmPaymentNumber = now2.format(formatter2)+orderRequestDto.getProductId().toString();


        boolean success = paymentService.processPayment(orderRequestDto, now1, confirmPaymentNumber);

        if (success) {
            Product product = paymentService.getProductInfo(orderRequestDto.getProductId());

            String tempImgUrl = product.getImageUrl();
            String[] imgUrlArr = tempImgUrl.split(",");
            String imgUrl = imgUrlArr[0];

            String productName = product.getName();
            String totalAddress = "("+orderRequestDto.getPostalCode()+") "+orderRequestDto.getDeliveryAddress();
            String seller = paymentService.getUsersInfo(orderRequestDto.getSellerId()).getUserName();


            int totalPayment = orderRequestDto.getTotalPayment();
            DecimalFormat deciFormat = new DecimalFormat("#,###");
            String totalPrice = deciFormat.format(totalPayment)+"P";
            String selectedOption = orderRequestDto.getSelectedOption();


            // Flash attribute 설정: POST에서 받은 데이터를 GET 요청 시 전달할 수 있음
            redirectAttributes.addFlashAttribute("seller", seller);
            redirectAttributes.addFlashAttribute("timestamp", formattedDateTime);
            redirectAttributes.addFlashAttribute("confirmNumber", confirmPaymentNumber);
            redirectAttributes.addFlashAttribute("imgUrl", imgUrl);
            redirectAttributes.addFlashAttribute("productName", productName);
            redirectAttributes.addFlashAttribute("totalAddress", totalAddress);
            redirectAttributes.addFlashAttribute("totalPrice", totalPrice);
            redirectAttributes.addFlashAttribute("selectedOption", selectedOption);

            // 리다이렉트: flash attribute는 여기서 GET 요청의 모델에 포함됨
            return "redirect:/pay/pay";
        } else {
            // 실패 처리: 필요에 따라 에러 페이지나 다른 처리 방법 사용
            return "redirect:/error";
        }
    }

    // GET 요청 처리: 결제 완료 후 보여줄 페이지 반환
    @GetMapping("/pay")
    public String pay(Model model) {

        // 필요한 데이터를 model에 추가하고, 뷰 이름 반환
        return "pay/pay";
    }
}
