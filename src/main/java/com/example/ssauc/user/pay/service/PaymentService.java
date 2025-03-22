package com.example.ssauc.user.pay.service;


import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.order.dto.OrderRequestDto;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.order.entity.Orders;
import com.example.ssauc.user.order.repository.OrdersRepository;
import com.example.ssauc.user.pay.entity.Payment;
import com.example.ssauc.user.pay.repository.PaymentRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final UsersRepository usersRepository;
    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;


    public Product getProductInfo(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public Users getUsersInfo(Long usersId) {
        return usersRepository.findById(usersId).orElse(null);
    }


    public boolean processPayment(OrderRequestDto dto, LocalDateTime date, String number){

        try {

            Product product = productRepository.findById(dto.getProductId()).orElse(null);
            Users buyer = usersRepository.findById(dto.getBuyerId()).orElse(null);
            Users seller = usersRepository.findById(dto.getSellerId()).orElse(null);


            long fee = dto.getTotalPayment()/21;
            long realMoney = fee*20;


            // 금액 업데이트
            usersRepository.minusCash((long) dto.getTotalPayment(),dto.getBuyerId());
            usersRepository.addCash(realMoney,dto.getSellerId());

            // orders 테이블 저장
            Orders orders = Orders.builder()
                    .product(product)
                    .buyer(buyer)
                    .seller(seller)
                    .totalPrice((long) dto.getTotalPayment())
                    .recipientName(buyer.getUserName())
                    .recipientPhone(buyer.getPhone())
                    .postalCode(dto.getPostalCode())
                    .deliveryAddress(dto.getDeliveryAddress())
                    .deliveryStatus("배송전")
                    .orderStatus("성공")
                    .orderDate(LocalDateTime.now())
                    .completedDate(null)
                    .build();

            ordersRepository.save(orders);

            // payment 테이블 저장
            Payment payment = Payment.builder()
                    .order(orders)
                    .payer(buyer)
                    .amount((long) dto.getTotalPayment())
                    .paymentMethod(dto.getSelectedOption())
                    .paymentStatus("성공")
                    .paymentDate(date)
                    .paymentNumber(number)
                    .build();

            paymentRepository.save(payment);

            // product 테이블 상태 변경

            productRepository.completeSell(product.getProductId());
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
