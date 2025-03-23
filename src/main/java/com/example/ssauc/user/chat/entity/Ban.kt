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
@Table(name = "ban")
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id")
    private Long banId; // 차단 PK

    // 차단한 사용자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // FK: user_id

    // 차단 당한 사용자
    @ManyToOne
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private Users blockedUser; // FK: blocked_user_id


    //차단시간
    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    //상태 (1이면 차단중, 0이면 차단해제중)
    @Column(name = "status")
    private int status;


    // user, blockedUser, blockedAt 을 받는 생성자
    public Ban(Users user, Users blockedUser, LocalDateTime blockedAt) {
        this.user = user;
        this.blockedUser = blockedUser;
        this.blockedAt = blockedAt;
    }


}
