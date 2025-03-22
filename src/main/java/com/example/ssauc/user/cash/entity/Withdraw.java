package com.example.ssauc.user.cash.entity;

import com.example.ssauc.user.login.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "withdraw")
public class Withdraw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdraw_id")
    private Long withdrawId;

    // 환급 신청 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private Long amount;
    private Long commission;

    @Column(length = 255)
    private String bank;

    @Column(length = 255)
    private String account;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "withdraw_at")
    private LocalDateTime withdrawAt;
}
