package com.example.ssauc.user.chat.entity;

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
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;    // 신고 아이디 (PK)

    // 신고 한 사용자
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private Users reporter;     // 신고자 아이디 (FK)

    // 신고 당한 사용자
    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private Users reportedUser;     // 피신고자 아이디

    @Column(nullable = false, length = 255)
    private String reportReason;     // 신고 사유

    @Column(length = 50)
    private String status;   // 처리 상태

    @Column(columnDefinition = "TEXT" , nullable = false)
    private String details;  // 신고 내용 (TEXT)


    private LocalDateTime reportDate;     // 신고 시간
    private LocalDateTime processedAt;  // 처리 시간



}
