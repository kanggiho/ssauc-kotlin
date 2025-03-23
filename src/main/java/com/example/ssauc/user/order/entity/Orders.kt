package com.example.ssauc.user.order.entity

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.pay.entity.Payment
import com.example.ssauc.user.pay.entity.Review
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private var orderId: Long? = null

    // 주문 한 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private val product: Product? = null

    // 주문 한 구매자
    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private val buyer: Users? = null

    // 주문 받은 판매자
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private val seller: Users? = null

    @Column(name = "total_price", nullable = false)
    private var totalPrice: Long? = null

    @Column(name = "recipient_name", length = 100)
    private var recipientName: String? = null

    @Column(name = "recipient_phone", length = 20)
    private var recipientPhone: String? = null

    @Column(name = "delivery_address", length = 255)
    private var deliveryAddress: String? = null

    @Column(name = "postal_code", length = 20)
    private var postalCode: String? = null

    @Column(name = "delivery_status", length = 50)
    private var deliveryStatus: String? = null

    @Column(name = "order_status", length = 50)
    private var orderStatus: String? = null

    @Column(name = "order_date")
    private var orderDate: LocalDateTime? = null

    @Column(name = "completed_date")
    private var completedDate: LocalDateTime? = null


    // 연결 관계 설정
    @OneToMany(mappedBy = "order")
    private val reviews: List<Review>? = null

    @OneToMany(mappedBy = "order")
    private val payments: List<Payment>? = null
}
