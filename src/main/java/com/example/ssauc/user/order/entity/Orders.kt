package com.example.ssauc.user.order.entity;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.pay.entity.Payment;
import com.example.ssauc.user.pay.entity.Review;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // 주문 한 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 한 구매자
    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private Users buyer;

    // 주문 받은 판매자
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Users seller;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;

    @Column(name = "order_status", length = 50)
    private String orderStatus;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;


    // 연결 관계 설정

    @OneToMany(mappedBy = "order")
    private List<Review> reviews;

    @OneToMany(mappedBy = "order")
    private List<Payment> payments;
}
