package com.example.ssauc.user.mypage.service

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.util.PasswordValidator.isValid
import com.example.ssauc.user.mypage.dto.UserUpdateDto
import lombok.RequiredArgsConstructor
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@RequiredArgsConstructor
class UserProfileService {
    private val usersRepository: UsersRepository? = null
    private val passwordEncoder: PasswordEncoder? = null

    fun getCurrentUser(email: String?): Users {
        return usersRepository!!.findByEmail(email)
            .orElseThrow { RuntimeException("사용자 정보를 찾을 수 없습니다.") }!!
    }

    fun updateUserProfile(user: Users, dto: UserUpdateDto) {
        // 닉네임 업데이트 (변경 값이 있을 경우)
        if (dto.userName != null && !dto.userName.isEmpty() && dto.userName != user.userName) {
//            if (usersRepository.existsByUserName(dto.getUserName())) {
//                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
//            }
            user.userName = dto.userName
        }

        // 비밀번호 업데이트
        if (dto.password != null && !dto.password.isEmpty()) {
            if (dto.password != dto.confirmPassword) {
                throw RuntimeException("비밀번호가 일치하지 않습니다.")
            }
            if (!isValid(dto.password)) {
                throw RuntimeException("비밀번호 형식이 올바르지 않습니다. (최소 8자, 영문, 숫자, 특수문자 포함)")
            }
            user.password = passwordEncoder!!.encode(dto.password)
        }

        // 휴대폰 번호 업데이트
        if (dto.phone != null && !dto.phone.isEmpty() && dto.phone != user.phone) {
            if (!dto.phone.matches("^\\d{10,11}$".toRegex())) {
                throw RuntimeException("휴대폰 번호 형식이 올바르지 않습니다.")
            }
            user.phone = dto.phone
        }

        // 개별 주소 업데이트: 모든 필드가 채워져 있을 경우에만 업데이트 (빈 값이면 기존 값 유지)
        if (dto.zipcode != null && !dto.zipcode.isEmpty() && dto.address != null && !dto.address.isEmpty() && dto.addressDetail != null && !dto.addressDetail.isEmpty()) {
            val newLocation = dto.zipcode + " " + dto.address + " " + dto.addressDetail
            if (newLocation != user.location) {
                user.location = newLocation
            }
        }

        // 프로필 이미지 업데이트
        if (dto.profileImage != null && !dto.profileImage.isEmpty() && dto.profileImage != user.profileImage) {
            user.profileImage = dto.profileImage
        }

        // 최종 수정 시간 업데이트
        user.setUpdatedAt(LocalDateTime.now())

        usersRepository!!.save(user)
    }

    //회원 탈퇴 로직
    fun withdrawUser(user: Users, password: String?) {
        // 비밀번호 매칭 확인
        if (!passwordEncoder!!.matches(password, user.password)) {
            throw RuntimeException("비밀번호가 일치하지 않습니다.")
        }
        // inactive로 전환
        user.status = "inactive"
        user.setUpdatedAt(LocalDateTime.now())
        usersRepository!!.save(user)
    }
}

