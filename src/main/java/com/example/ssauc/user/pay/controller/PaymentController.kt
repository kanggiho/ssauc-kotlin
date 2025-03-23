package com.example.ssauc.user.pay.controller

import com.example.ssauc.user.order.dto.OrderRequestDto
import com.example.ssauc.user.pay.service.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/pay")
class PaymentController {
    @Autowired
    var paymentService: PaymentService? = null

    // POST 요청 처리: 결제 처리 로직 수행
    @PostMapping("/pay")
    fun processPayment(orderRequestDto: OrderRequestDto, redirectAttributes: RedirectAttributes): String {
        // 결제 처리 로직 실행

        // 현재 날짜와 시간 가져오기

        val now = LocalDateTime.now()
        val now1 = LocalDateTime.now()
        val now2 = LocalDateTime.now()
        // 원하는 형식의 포매터 생성
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")
        val formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        // LocalDateTime을 문자열로 포맷팅
        val formattedDateTime = now.format(formatter)
        val confirmPaymentNumber = now2.format(formatter2) + orderRequestDto.getProductId().toString()


        val success = paymentService!!.processPayment(orderRequestDto, now1, confirmPaymentNumber)

        if (success) {
            val product = paymentService!!.getProductInfo(orderRequestDto.getProductId())

            val tempImgUrl = product!!.imageUrl
            val imgUrlArr = tempImgUrl!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val imgUrl = imgUrlArr[0]

            val productName = product.name
            val totalAddress =
                ("(" + orderRequestDto.getPostalCode()).toString() + ") " + orderRequestDto.getDeliveryAddress()
            val seller = paymentService!!.getUsersInfo(orderRequestDto.getSellerId())!!.userName


            val totalPayment: Int = orderRequestDto.getTotalPayment()
            val deciFormat = DecimalFormat("#,###")
            val totalPrice = deciFormat.format(totalPayment.toLong()) + "P"
            val selectedOption: String = orderRequestDto.getSelectedOption()


            // Flash attribute 설정: POST에서 받은 데이터를 GET 요청 시 전달할 수 있음
            redirectAttributes.addFlashAttribute("seller", seller)
            redirectAttributes.addFlashAttribute("timestamp", formattedDateTime)
            redirectAttributes.addFlashAttribute("confirmNumber", confirmPaymentNumber)
            redirectAttributes.addFlashAttribute("imgUrl", imgUrl)
            redirectAttributes.addFlashAttribute("productName", productName)
            redirectAttributes.addFlashAttribute("totalAddress", totalAddress)
            redirectAttributes.addFlashAttribute("totalPrice", totalPrice)
            redirectAttributes.addFlashAttribute("selectedOption", selectedOption)

            // 리다이렉트: flash attribute는 여기서 GET 요청의 모델에 포함됨
            return "redirect:/pay/pay"
        } else {
            // 실패 처리: 필요에 따라 에러 페이지나 다른 처리 방법 사용
            return "redirect:/error"
        }
    }

    // GET 요청 처리: 결제 완료 후 보여줄 페이지 반환
    @GetMapping("/pay")
    fun pay(model: Model?): String {
        // 필요한 데이터를 model에 추가하고, 뷰 이름 반환

        return "pay/pay"
    }
}
