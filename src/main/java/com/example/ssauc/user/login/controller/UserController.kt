package com.example.ssauc.user.login.controller

import com.example.ssauc.user.login.dto.UserRegistrationDTO
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.service.UserService
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


/**
 * 회원가입/중복검사 REST API
 * signup.js에서 fetch()로 호출
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {
    private val userService: UserService? = null
    private val userRepository: UsersRepository? = null

    /**
     * (1) 회원가입 처리
     */
    @PostMapping("/register")
    fun registerUser(@RequestBody dto: UserRegistrationDTO): ResponseEntity<String> {
        val result = userService!!.register(dto)
        return if ("회원가입 성공" == result) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }

    /**
     * (2) 이메일 중복 확인
     */
    @GetMapping("/check-email")
    fun checkEmail(@RequestParam("email") email: String?): ResponseEntity<String> {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex())) {
            return ResponseEntity.badRequest().body("유효한 이메일 주소를 입력하세요.")
        }
        val existingUserOpt = userRepository!!.findByEmail(email)
        if (existingUserOpt!!.isPresent) {
            val user = existingUserOpt.get()
            return if ("blocked".equals(user.status, ignoreCase = true)) {
                ResponseEntity.badRequest().body("해당 이메일은 가입이 불가합니다.")
            } else if ("inactive".equals(user.status, ignoreCase = true)) {
                ResponseEntity.ok("사용 가능한 이메일입니다.")
            } else {
                ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.")
            }
        }
        return ResponseEntity.ok("사용 가능한 이메일입니다.")
    }

    /**
     * (3) 닉네임 중복 확인
     */
    @GetMapping("/check-username")
    fun checkUsername(
        @RequestParam("username") username: String?,
        @RequestParam(value = "email", required = false) email: String?
    ): ResponseEntity<String> {
        if (username == null || username.trim { it <= ' ' }.length < 2) {
            return ResponseEntity.badRequest().body("닉네임은 최소 2글자 이상이어야 합니다.")
        }
        val userOpt = userRepository!!.findByUserName(username)
        if (userOpt!!.isPresent) {
            val user = userOpt.get()
            // 만약 추가 파라미터로 전달된 이메일이 DB에 있는 사용자와 동일하다면 재가입 대상이므로 사용 가능
            return if (email != null && email.equals(user.email, ignoreCase = true)) {
                ResponseEntity.ok("사용 가능한 닉네임입니다.")
            } else if ("inactive".equals(user.status, ignoreCase = true)) {
                ResponseEntity.ok("사용 가능한 닉네임입니다.")
            } else {
                ResponseEntity.badRequest().body("이미 사용 중인 닉네임입니다.")
            }
        }
        return ResponseEntity.ok("사용 가능한 닉네임입니다.")
    }

    @GetMapping("/check-phone")
    fun checkPhone(
        @RequestParam("phone") phone: String?,
        @RequestParam(value = "email", required = false) email: String?
    ): ResponseEntity<String> {
        if (phone == null || phone.trim { it <= ' ' }.isEmpty()) {
            return ResponseEntity.badRequest().body("유효한 휴대폰 번호를 입력하세요.")
        }
        val userOpt = userRepository!!.findByPhone(phone)
        if (userOpt!!.isPresent) {
            val user = userOpt.get()
            return if (email != null && email.equals(user.email, ignoreCase = true)) {
                ResponseEntity.ok("사용 가능한 휴대폰 번호입니다.")
            } else if ("inactive".equals(user.status, ignoreCase = true)) {
                ResponseEntity.ok("사용 가능한 휴대폰 번호입니다.")
            } else {
                ResponseEntity.badRequest().body("이미 사용 중인 휴대폰 번호입니다.")
            }
        }
        return ResponseEntity.ok("사용 가능한 휴대폰 번호입니다.")
    }
}