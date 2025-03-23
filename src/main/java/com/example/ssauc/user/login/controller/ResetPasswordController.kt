package com.example.ssauc.user.login.controller

import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.service.FirebaseService
import com.example.ssauc.user.login.util.PasswordValidator
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@Slf4j
@RestController
@RequestMapping("/api/reset-password")
@RequiredArgsConstructor
class ResetPasswordController {
    private val usersRepository: UsersRepository? = null
    private val firebaseService: FirebaseService? = null

    // BCryptPasswordEncoder는 실제 환경에서는 Bean으로 주입하는 것이 좋습니다.
    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * 1) Firebase ID 토큰 검증
     * 클라이언트에서 전달한 idToken과 입력된 email, phone 정보를 사용해
     * Firebase SMS 인증이 유효한지 확인합니다.
     */
    @PostMapping("/verify-token")
    fun verifyToken(
        @RequestParam idToken: String?,
        @RequestParam email: String?,
        @RequestParam phone: String
    ): ResponseEntity<String> {
        try {
            val token = firebaseService!!.verifyIdToken(idToken)
            // Firebase의 phone_number 클레임은 일반적으로 국제 형식(+82...)입니다.
            var verifiedPhone = token!!.claims["phone_number"] as String?
            // verifiedPhone을 로컬 형식으로 변환: 예를 들어, "+82" 로 시작하면 "0"으로 변환
            if (verifiedPhone != null && verifiedPhone.startsWith("+82")) {
                verifiedPhone = "0" + verifiedPhone.substring(3)
            }
            // 변환 후 비교
            if (verifiedPhone == null || verifiedPhone != phone) {
                return ResponseEntity.badRequest().body("전화번호 불일치")
            }
            // 이메일과 전화번호가 일치하는 사용자인지 확인 (DB에는 로컬형식 저장)
            val userOpt = usersRepository!!.findByEmailAndStatus(email, "active")
            if (userOpt!!.isEmpty) {
                return ResponseEntity.badRequest().body("이메일 혹은 휴대폰 번호를 확인해주세요.")
            }
            val user = userOpt.get()
            // phone 확인
            if (user.phone != phone) {
                return ResponseEntity.badRequest().body("회원 정보가 일치하지 않습니다.")
            }
            return ResponseEntity.ok("새 비밀번호를 설정합니다.")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("토큰 검증 실패: " + e.message)
        }
    }

    /**
     * 2) 새 비밀번호 재설정
     * 입력된 새 비밀번호가 유효하면 암호화 후 사용자 정보에 저장합니다.
     */
    @PostMapping("/new-password")
    fun setNewPassword(
        @RequestParam email: String?,
        @RequestParam newPassword: String?
    ): ResponseEntity<String> {
        val user = usersRepository!!.findByEmail(email).orElse(null)
            ?: return ResponseEntity.badRequest().body("사용자 없음")
        // 만약 새 비밀번호가 기존 비밀번호와 동일하다면 (예: passwordEncoder.matches)
        if (passwordEncoder.matches(newPassword, user.password)) {
            return ResponseEntity.badRequest().body("새 비밀번호는 기존 비밀번호와 달라야 합니다.")
        }
        if (!PasswordValidator.isValid(newPassword)) {
            return ResponseEntity.badRequest().body("비밀번호 형식 오류")
        }
        user.password = passwordEncoder.encode(newPassword)
        usersRepository.save(user)
        return ResponseEntity.ok("비밀번호가 재설정되었습니다.")
    }
}
