package com.example.ssauc.user.login.service

import com.example.ssauc.user.login.dto.LoginResponseDTO
import com.example.ssauc.user.login.dto.UserRegistrationDTO
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.entity.Users.email
import com.example.ssauc.user.login.entity.Users.location
import com.example.ssauc.user.login.entity.Users.password
import com.example.ssauc.user.login.entity.Users.phone
import com.example.ssauc.user.login.entity.Users.userName
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.util.JwtUtil
import com.example.ssauc.user.login.util.PasswordValidator
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Slf4j
@Service
@RequiredArgsConstructor
class UserService {
    private val userRepository: UsersRepository? = null
    private val refreshTokenService: RefreshTokenService? = null
    private val jwtUtil: JwtUtil? = null
    private val passwordEncoder: PasswordEncoder? = null

    fun getCurrentUser(email: String?): Users? {
        return userRepository!!.findByEmail(email).orElse(null)
    }

    fun register(dto: UserRegistrationDTO): String {
        // 1. 이메일, 비밀번호, 확인 비밀번호 유효성 검사
        if (!dto.email.matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex())) {
            return "유효한 이메일 입력"
        }
        if (!PasswordValidator.isValid(dto.password)) {
            return "비밀번호가 형식에 맞지 않음"
        }
        if (dto.password != dto.confirmPassword) {
            return "비밀번호 불일치"
        }
        // 2. 주소 정보 결합: 우편번호, 기본주소, 상세주소
        val fullLocation = dto.zipcode + " " + dto.address + " " + dto.addressDetail

        // 3. 이메일로 기존 사용자가 있는지 조회
        val existingUserOpt = userRepository!!.findByEmail(dto.email)
        if (existingUserOpt!!.isPresent) {
            val existingUser = existingUserOpt.get()
            // 3-1. blocked 상태인 경우 -> 재가입 불가
            if ("blocked".equals(existingUser.status, ignoreCase = true)) {
                return "해당 이메일은 가입이 불가합니다."
            } else if ("inactive".equals(existingUser.status, ignoreCase = true)) {
                // ※ 재가입의 경우에는 이메일이 일치하면 기존 레코드 업데이트로 처리하므로,
                // 닉네임과 휴대폰 번호 중복 여부는 체크하지 않고 그대로 업데이트합니다.
                existingUser.userName = dto.userName
                existingUser.password = passwordEncoder!!.encode(dto.password)
                existingUser.phone = dto.phone
                existingUser.location = fullLocation
                existingUser.status = "ACTIVE" // 상태를 active로 전환
                existingUser.setUpdatedAt(LocalDateTime.now())
                userRepository.save(existingUser)
                return "회원가입 성공"
            } else {
                return "이미 가입된 이메일"
            }
        } else {
            // 4. 신규 가입인 경우: 이메일이 DB에 없으므로 닉네임, 전화번호 중복 체크 수행
            if (userRepository.existsByUserName(dto.userName)) {
                return "이미 존재하는 닉네임"
            }
            if (userRepository.existsByPhone(dto.phone)) {
                return "이미 사용 중인 전화번호"
            }
            // 5. 신규 사용자 생성 및 DB 저장
            val user: Users = Users.builder()
                .userName(dto.userName)
                .email(dto.email)
                .phone(dto.phone)
                .password(passwordEncoder!!.encode(dto.password))
                .location(fullLocation)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
            userRepository.save(user)
            return "회원가입 성공"
        }
    }


    /**
     * 로그인 로직
     */
    fun login(email: String, password: String?): Optional<LoginResponseDTO> {
        val normalizedEmail = email.trim { it <= ' ' }.lowercase(Locale.getDefault())
        UserService.log.info("로그인 시도: {}", normalizedEmail)
        val userOpt = userRepository!!.findByEmail(normalizedEmail)
        if (userOpt!!.isPresent) {
            val user = userOpt.get()
            // inactive 상태면 로그인 불가
            if (!"active".equals(user.status, ignoreCase = true)) {
                UserService.log.warn("로그인 실패 - 사용자 상태가 active가 아님: {}", normalizedEmail)
                return Optional.empty()
            }
            // 비밀번호 체크
            if (passwordEncoder!!.matches(password, user.password)) {
                UserService.log.info("비밀번호 일치함: {}", normalizedEmail)
                // lastLogin 업데이트
                user.updateLastLogin()
                userRepository.save(user)

                val accessToken = jwtUtil!!.generateAccessToken(normalizedEmail)
                val refreshToken = jwtUtil.generateRefreshToken(normalizedEmail)
                UserService.log.info("생성된 Access Token: {}", accessToken)
                UserService.log.info("생성된 Refresh Token: {}", refreshToken)

                refreshTokenService!!.saveRefreshToken(normalizedEmail, refreshToken!!)
                return Optional.of(LoginResponseDTO(accessToken, refreshToken))
            } else {
                UserService.log.warn("비밀번호 불일치: 입력={}, DB={}", password, user.password)
            }
        } else {
            UserService.log.warn("사용자 조회 실패: {}", normalizedEmail)
        }
        return Optional.empty()
    }
}
