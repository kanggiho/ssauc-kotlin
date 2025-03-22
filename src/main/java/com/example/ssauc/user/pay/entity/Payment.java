package com.example.ssauc.user.pay.entity;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.order.entity.Orders;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    // 결제 요청 주문
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    // 결제 하는 사용자
    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private Users payer;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_number")
    private String paymentNumber;
}
