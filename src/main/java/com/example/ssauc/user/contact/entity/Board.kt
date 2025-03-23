package com.example.ssauc.user.contact.entity;

import com.example.ssauc.admin.entity.Reply;
import com.example.ssauc.user.login.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;            // PK

    // 작성자 아이디 (FK) - Users 엔티티와 매핑
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;              // 실제 Users 엔티티를 참조

    @Column(nullable = false, length = 200)
    private String subject;          // 문의 제목

    @Column(columnDefinition = "TEXT")
    private String message;          // 문의 내용

    private LocalDateTime createdAt; // 문의 시간

    @Column(length = 50)
    private String status;           // 답변 상태

    // 관계 설정
    @OneToOne(mappedBy = "board", cascade = CascadeType.ALL)
    private Reply reply;
}
