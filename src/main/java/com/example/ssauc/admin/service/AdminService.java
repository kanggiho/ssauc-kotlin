package com.example.ssauc.admin.service;


import com.example.ssauc.admin.entity.Admin;
import com.example.ssauc.admin.repository.AdminRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Admin checkAddress(String email, String password) {
        return adminRepository.findByEmailAndPassword(email, password).orElse(null);
    }

    //Google Authenticator 시크릿 키(google_secret) 생성
    public Admin generateGoogleSecret(Admin admin) {
        // 라이브러리를 통해 Google Authenticator Key 생성
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();

        admin.setGoogleSecret(key.getKey());
        admin.setTempKey(key);
        return adminRepository.save(admin);
    }

    //2차 인증 코드 검증
    public boolean verifyCode(Admin admin, int code) {
        if (admin.getGoogleSecret() == null) {
            log.info("Google secret is null for admin: {}", admin.getAdminId());
            return false;
        }

        // 현재 서버 시간을 로그에 찍음 (ms 단위)
        long currentTimeMillis = System.currentTimeMillis();
        log.info("Server current time (ms): {}", currentTimeMillis);

        // GoogleAuthenticatorConfig를 사용하여 윈도우 사이즈 설정 (예: ±3 슬롯 허용)
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setWindowSize(3)
                .build();
        GoogleAuthenticator gAuth = new GoogleAuthenticator(config);

        // 인증 코드 검증 결과
        boolean result = gAuth.authorize(admin.getGoogleSecret(), code);
        log.info("Code verification result: {} for code: {}", result, code);
        return result;
    }

    //email로 관리자 조회
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
}

