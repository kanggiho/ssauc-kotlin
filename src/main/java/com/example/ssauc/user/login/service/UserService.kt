package com.example.ssauc.user.login.service;

import com.example.ssauc.user.login.dto.LoginResponseDTO;
import com.example.ssauc.user.login.dto.UserRegistrationDTO;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.util.JwtUtil;
import com.example.ssauc.user.login.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public Users getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public String register(UserRegistrationDTO dto) {
        // 1. 이메일, 비밀번호, 확인 비밀번호 유효성 검사
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "유효한 이메일 입력";
        }
        if (!PasswordValidator.isValid(dto.getPassword())) {
            return "비밀번호가 형식에 맞지 않음";
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return "비밀번호 불일치";
        }
        // 2. 주소 정보 결합: 우편번호, 기본주소, 상세주소
        String fullLocation = dto.getZipcode() + " " + dto.getAddress() + " " + dto.getAddressDetail();

        // 3. 이메일로 기존 사용자가 있는지 조회
        Optional<Users> existingUserOpt = userRepository.findByEmail(dto.getEmail());
        if(existingUserOpt.isPresent()){
            Users existingUser = existingUserOpt.get();
            // 3-1. blocked 상태인 경우 -> 재가입 불가
            if("blocked".equalsIgnoreCase(existingUser.getStatus())){
                return "해당 이메일은 가입이 불가합니다.";
            }
            // 3-2. inactive 상태인 경우 -> 재가입(reactivation) 처리
            else if("inactive".equalsIgnoreCase(existingUser.getStatus())){
                // ※ 재가입의 경우에는 이메일이 일치하면 기존 레코드 업데이트로 처리하므로,
                // 닉네임과 휴대폰 번호 중복 여부는 체크하지 않고 그대로 업데이트합니다.
                existingUser.setUserName(dto.getUserName());
                existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                existingUser.setPhone(dto.getPhone());
                existingUser.setLocation(fullLocation);
                existingUser.setStatus("ACTIVE"); // 상태를 active로 전환
                existingUser.setUpdatedAt(LocalDateTime.now());
                userRepository.save(existingUser);
                return "회원가입 성공";
            }
            // 3-3. 이미 active 상태이면 중복 가입으로 처리
            else {
                return "이미 가입된 이메일";
            }
        } else {
            // 4. 신규 가입인 경우: 이메일이 DB에 없으므로 닉네임, 전화번호 중복 체크 수행
            if(userRepository.existsByUserName(dto.getUserName())){
                return "이미 존재하는 닉네임";
            }
            if(userRepository.existsByPhone(dto.getPhone())){
                return "이미 사용 중인 전화번호";
            }
            // 5. 신규 사용자 생성 및 DB 저장
            Users user = Users.builder()
                    .userName(dto.getUserName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .location(fullLocation)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            return "회원가입 성공";
        }
    }


    /**
     * 로그인 로직
     */
    public Optional<LoginResponseDTO> login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        log.info("로그인 시도: {}", normalizedEmail);
        Optional<Users> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            // inactive 상태면 로그인 불가
            if (!"active".equalsIgnoreCase(user.getStatus())) {
                log.warn("로그인 실패 - 사용자 상태가 active가 아님: {}", normalizedEmail);
                return Optional.empty();
            }
            // 비밀번호 체크
            if (passwordEncoder.matches(password, user.getPassword())) {
                log.info("비밀번호 일치함: {}", normalizedEmail);
                // lastLogin 업데이트
                user.updateLastLogin();
                userRepository.save(user);

                String accessToken = jwtUtil.generateAccessToken(normalizedEmail);
                String refreshToken = jwtUtil.generateRefreshToken(normalizedEmail);
                log.info("생성된 Access Token: {}", accessToken);
                log.info("생성된 Refresh Token: {}", refreshToken);

                refreshTokenService.saveRefreshToken(normalizedEmail, refreshToken);
                return Optional.of(new LoginResponseDTO(accessToken, refreshToken));
            } else {
                log.warn("비밀번호 불일치: 입력={}, DB={}", password, user.getPassword());
            }
        } else {
            log.warn("사용자 조회 실패: {}", normalizedEmail);
        }
        return Optional.empty();
    }
}
