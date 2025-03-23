package com.example.ssauc.user.pay.service

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.order.dto.OrderRequestDto
import com.example.ssauc.user.order.entity.Orders
import com.example.ssauc.user.order.repository.OrdersRepository
import com.example.ssauc.user.pay.entity.Payment
import com.example.ssauc.user.pay.repository.PaymentRepository
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.ProductRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@RequiredArgsConstructor
@Service
class PaymentService {
    private val usersRepository: UsersRepository? = null
    private val productRepository: ProductRepository? = null
    private val ordersRepository: OrdersRepository? = null
    private val paymentRepository: PaymentRepository? = null


    fun getProductInfo(productId: Long): Product? {
        return productRepository!!.findById(productId).orElse(null)
    }

    fun getUsersInfo(usersId: Long): Users? {
        return usersRepository!!.findById(usersId).orElse(null)
    }


    fun processPayment(dto: OrderRequestDto, date: LocalDateTime?, number: String?): Boolean {
        try {
            val product = productRepository!!.findById(dto.getProductId()).orElse(null)
            val buyer = usersRepository!!.findById(dto.getBuyerId()).orElse(null)
            val seller = usersRepository.findById(dto.getSellerId()).orElse(null)


            val fee: Long = dto.getTotalPayment() / 21
            val realMoney = fee * 20


            // 금액 업데이트
            usersRepository.minusCash(dto.getTotalPayment() as Long, dto.getBuyerId())
            usersRepository.addCash(realMoney, dto.getSellerId())

            // orders 테이블 저장
            val orders: Orders = Orders.builder()
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .totalPrice(dto.getTotalPayment() as Long)
                .recipientName(buyer!!.userName)
                .recipientPhone(buyer.phone)
                .postalCode(dto.getPostalCode())
                .deliveryAddress(dto.getDeliveryAddress())
                .deliveryStatus("배송전")
                .orderStatus("성공")
                .orderDate(LocalDateTime.now())
                .completedDate(null)
                .build()

            ordersRepository!!.save(orders)

            // payment 테이블 저장
            val payment = Payment.builder()
                .order(orders)
                .payer(buyer)
                .amount(dto.getTotalPayment() as Long)
                .paymentMethod(dto.getSelectedOption())
                .paymentStatus("성공")
                .paymentDate(date)
                .paymentNumber(number)
                .build()

            paymentRepository!!.save(payment)

            // product 테이블 상태 변경
            productRepository.completeSell(product!!.productId)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}
