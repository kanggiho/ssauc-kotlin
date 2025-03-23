package com.example.ssauc.user.order.controller

import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.order.dto.OrderRequestDto
import com.example.ssauc.user.order.service.OrderService
import jakarta.servlet.http.HttpServletRequest
import lombok.AllArgsConstructor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/order")
@AllArgsConstructor
class OrderController {
    private val orderService: OrderService? = null
    private val tokenExtractor: TokenExtractor? = null

    @GetMapping("/order")
    fun order(@RequestParam("productId") ProductId: Long, model: Model, request: HttpServletRequest): String {
        val product = orderService!!.getProductById(ProductId)
        val buyer = tokenExtractor!!.getUserFromToken(request)
        val seller = orderService.getUserById(product.seller!!.userId!!)

        // 대표 이미지 처리
        val tempImg = product.imageUrl
        val tempArr = tempImg!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        product.imageUrl = tempArr[0]


        val dto = OrderRequestDto()
        dto.productId = ProductId
        dto.buyerId = buyer!!.userId
        dto.sellerId = seller.userId

        val price = product.price
        val fee = (product.price!! * 0.05).toLong()
        val total = price!! + fee

        model.addAttribute("orderRequestDto", dto)
        model.addAttribute("product", product)

        model.addAttribute("price", price)
        model.addAttribute("fee", fee)
        model.addAttribute("total", total)

        model.addAttribute("userCash", buyer.cash)

        return "order/order"
    }
}
