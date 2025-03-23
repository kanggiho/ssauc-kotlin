package com.example.ssauc.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Table(name = "admin")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_name", length = 50, nullable = false)
    private String adminName;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "google_secret", length = 32, unique = true)
    private String googleSecret; // Google Authenticator Secret Key 저장

    // 임시로 생성된 GoogleAuthenticatorKey 객체 (DB에 저장되지 않음)
    @Transient
    private com.warrenstrange.googleauth.GoogleAuthenticatorKey tempKey;

    // 관계 설정
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Reply> replies = new ArrayList<>();

    // 비밀번호 암호화 저장 (BCrypt)
    public void encodePassword(String rawPassword, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(rawPassword);
    }
}
