package com.example.ssauc.admin.service

import com.example.ssauc.admin.entity.Admin
import com.example.ssauc.admin.repository.AdminRepository
import com.warrenstrange.googleauth.GoogleAuthenticator
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*


@Slf4j
@Service
class AdminService {
    @Autowired
    private val adminRepository: AdminRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null


    fun checkAddress(email: String?, password: String?): Admin? {
        return adminRepository!!.findByEmailAndPassword(email, password)!!.orElse(null)
    }

    //Google Authenticator 시크릿 키(google_secret) 생성
    fun generateGoogleSecret(admin: Admin): Admin {
        // 라이브러리를 통해 Google Authenticator Key 생성
        val gAuth = GoogleAuthenticator()
        val key = gAuth.createCredentials()

        admin.setGoogleSecret(key.key)
        admin.setTempKey(key)
        return adminRepository!!.save(admin)
    }

    //2차 인증 코드 검증
    fun verifyCode(admin: Admin, code: Int): Boolean {
        if (admin.getGoogleSecret() == null) {
            AdminService.log.info("Google secret is null for admin: {}", admin.getAdminId())
            return false
        }

        // 현재 서버 시간을 로그에 찍음 (ms 단위)
        val currentTimeMillis = System.currentTimeMillis()
        AdminService.log.info("Server current time (ms): {}", currentTimeMillis)

        // GoogleAuthenticatorConfig를 사용하여 윈도우 사이즈 설정 (예: ±3 슬롯 허용)
        val config = GoogleAuthenticatorConfigBuilder()
            .setWindowSize(3)
            .build()
        val gAuth = GoogleAuthenticator(config)

        // 인증 코드 검증 결과
        val result = gAuth.authorize(admin.getGoogleSecret(), code)
        AdminService.log.info("Code verification result: {} for code: {}", result, code)
        return result
    }

    //email로 관리자 조회
    fun findByEmail(email: String?): Optional<Admin?>? {
        return adminRepository!!.findByEmail(email)
    }
}

