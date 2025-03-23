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
@Table(name = "charge")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Long chargeId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "imp_uid", length = 255)
    private String impUid;

    @Column(name = "charge_type", length = 50)
    private String chargeType;

    private Long amount;

    @Column(name = "status", length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "receipt_url", length = 255)
    private String receiptUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

