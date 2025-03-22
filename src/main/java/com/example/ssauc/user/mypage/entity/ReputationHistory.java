package com.example.ssauc.user.mypage.entity;



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
@Table(name = "reputation_history")
public class ReputationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    // 사용자 평판
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "change_type", length = 50)
    private String changeType;

    private Double changeAmount;
    private Double newScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}