package com.example.ssauc.user.order.controller;

import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.order.dto.OrderRequestDto;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.order.service.OrderService;
import com.example.ssauc.user.product.entity.Product;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final TokenExtractor tokenExtractor;

    @GetMapping("/order")
    public String order(@RequestParam("productId")Long ProductId, Model model, HttpServletRequest request) {
        Product product = orderService.getProductById(ProductId);
        Users buyer = tokenExtractor.getUserFromToken(request);
        Users seller = orderService.getUserById(product.getSeller().getUserId());

        // 대표 이미지 처리
        String tempImg = product.getImageUrl();
        String[] tempArr = tempImg.split(",");
        product.setImageUrl(tempArr[0]);



        OrderRequestDto dto = new OrderRequestDto();
        dto.setProductId(ProductId);
        dto.setBuyerId(buyer.getUserId());
        dto.setSellerId(seller.getUserId());

        Long price = product.getPrice();
        Long fee = (long) (product.getPrice() * 0.05);
        Long total = price + fee;

        model.addAttribute("orderRequestDto", dto);
        model.addAttribute("product", product);

        model.addAttribute("price", price);
        model.addAttribute("fee", fee);
        model.addAttribute("total", total);

        model.addAttribute("userCash", buyer.getCash());

        return "order/order";
    }
}
