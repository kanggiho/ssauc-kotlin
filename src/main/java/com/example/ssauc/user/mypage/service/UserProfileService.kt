package com.example.ssauc.user.mypage.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.mypage.dto.UserUpdateDto;
import com.example.ssauc.user.login.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public Users getCurrentUser(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
    }

    public void updateUserProfile(Users user, UserUpdateDto dto) {
        // 닉네임 업데이트 (변경 값이 있을 경우)
        if (dto.getUserName() != null && !dto.getUserName().isEmpty() &&
                !dto.getUserName().equals(user.getUserName())) {
//            if (usersRepository.existsByUserName(dto.getUserName())) {
//                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
//            }
            user.setUserName(dto.getUserName());
        }

        // 비밀번호 업데이트
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
            if (!PasswordValidator.isValid(dto.getPassword())) {
                throw new RuntimeException("비밀번호 형식이 올바르지 않습니다. (최소 8자, 영문, 숫자, 특수문자 포함)");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 휴대폰 번호 업데이트
        if (dto.getPhone() != null && !dto.getPhone().isEmpty() &&
                !dto.getPhone().equals(user.getPhone())) {
            if (!dto.getPhone().matches("^\\d{10,11}$")) {
                throw new RuntimeException("휴대폰 번호 형식이 올바르지 않습니다.");
            }
            user.setPhone(dto.getPhone());
        }

        // 개별 주소 업데이트: 모든 필드가 채워져 있을 경우에만 업데이트 (빈 값이면 기존 값 유지)
        if (dto.getZipcode() != null && !dto.getZipcode().isEmpty() &&
                dto.getAddress() != null && !dto.getAddress().isEmpty() &&
                dto.getAddressDetail() != null && !dto.getAddressDetail().isEmpty()) {
            String newLocation = dto.getZipcode() + " " + dto.getAddress() + " " + dto.getAddressDetail();
            if (!newLocation.equals(user.getLocation())) {
                user.setLocation(newLocation);
            }
        }

        // 프로필 이미지 업데이트
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty() &&
                !dto.getProfileImage().equals(user.getProfileImage())) {
            user.setProfileImage(dto.getProfileImage());
        }

        // 최종 수정 시간 업데이트
        user.setUpdatedAt(LocalDateTime.now());

        usersRepository.save(user);
    }

    //회원 탈퇴 로직

    public void withdrawUser(Users user, String password) {
        // 비밀번호 매칭 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        // inactive로 전환
        user.setStatus("inactive");
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
    }
}

