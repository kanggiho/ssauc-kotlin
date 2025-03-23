package com.example.ssauc.admin.entity

import com.warrenstrange.googleauth.GoogleAuthenticatorKey
import jakarta.persistence.*
import lombok.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Entity
@Builder
@Table(name = "admin")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    public var adminId: Long? = null

    @Column(name = "admin_name", length = 50, nullable = false)
    public var adminName: String? = null

    @Column(name = "email", length = 100, nullable = false, unique = true)
    public var email: String? = null

    @Column(name = "password", length = 255, nullable = false)
    public var password: String? = null

    @Column(name = "google_secret", length = 32, unique = true)
    public var googleSecret: String? = null // Google Authenticator Secret Key 저장

    // 임시로 생성된 GoogleAuthenticatorKey 객체 (DB에 저장되지 않음)
    @Transient
    public val tempKey: GoogleAuthenticatorKey? = null

    // 관계 설정
    @OneToMany(mappedBy = "admin", cascade = [CascadeType.ALL])
    public val replies: List<Reply> = ArrayList()

    // 비밀번호 암호화 저장 (BCrypt)
    fun encodePassword(rawPassword: String?, passwordEncoder: BCryptPasswordEncoder) {
        this.password = passwordEncoder.encode(rawPassword)
    }
}
