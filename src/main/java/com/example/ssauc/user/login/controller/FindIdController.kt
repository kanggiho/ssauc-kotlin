package com.example.ssauc.user.login.controller

import com.example.ssauc.common.service.RedisService
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.service.FirebaseService
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.TimeUnit


@Slf4j
@RestController
@RequestMapping("/api/find-id")
@RequiredArgsConstructor
class FindIdController {
    private val usersRepository: UsersRepository? = null
    private val firebaseService: FirebaseService? = null
    private val redisService: RedisService? = null

    /**
     * 1) 인증번호 전송
     */
    @PostMapping("/send-code")
    fun sendCode(
        @RequestParam userName: String,
        @RequestParam phone: String
    ): ResponseEntity<String> {
        // 사용자 확인 (닉네임 + 전화번호)
        val userOpt = usersRepository!!.findByUserNameAndPhoneAndStatus(userName, phone, "active")
        if (userOpt!!.isEmpty) {
            return ResponseEntity.badRequest().body("해당 정보의 (active) 회원이 없습니다.")
        }
        // 6자리 SMS 인증번호 생성
        val code = String.format("%06d", Random().nextInt(1000000))
        val redisKey = "FINDID:$userName:$phone"
        // Redis에 5분 TTL로 저장
        redisService!!.saveValueWithExpire(redisKey, code, 5, TimeUnit.MINUTES)
        // 실제 SMS 전송 로직은 생략 (외부 API 연동)
        return ResponseEntity.ok("인증번호가 전송되었습니다.")
    }

    /**
     * 2) 인증번호 검증 후 아이디(이메일) 반환
     */
    @PostMapping("/verify-token")
    fun verifyToken(
        @RequestParam idToken: String?,
        @RequestParam phone: String,
        @RequestParam userName: String?
    ): ResponseEntity<String> {
        try {
            val token = firebaseService!!.verifyIdToken(idToken)
            // Firebase의 phone_number 클레임은 보통 국제 형식(+82...)입니다.
            var verifiedPhone = token!!.claims["phone_number"] as String?
            // 국제 형식(+82)을 로컬 형식(0으로 시작)으로 변환
            if (verifiedPhone != null && verifiedPhone.startsWith("+82")) {
                verifiedPhone = "0" + verifiedPhone.substring(3)
            }
            if (verifiedPhone == null || verifiedPhone != phone) {
                return ResponseEntity.badRequest().body("전화번호 불일치")
            }
            // 닉네임과 전화번호로 사용자 조회 (DB에 저장된 전화번호는 로컬 형식)
            // 닉네임/전화번호/active로 사용자 조회
            val userOpt = usersRepository!!.findByUserNameAndPhoneAndStatus(userName, phone, "active")
            if (userOpt!!.isEmpty) {
                return ResponseEntity.badRequest().body("닉네임 혹은 휴대폰 번호를 확인해주세요.")
            }
            val user = userOpt.get()
            return ResponseEntity.ok(user.email)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.badRequest().body("토큰 검증 실패: " + e.message)
        }
    }
}
