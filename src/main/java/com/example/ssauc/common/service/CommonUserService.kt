package com.example.ssauc.common.service

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class CommonUserService {
    private val usersRepository: UsersRepository? = null

    fun getCurrentUser(email: String?): Users {
        return usersRepository!!.findByEmail(email)
            .orElseThrow { RuntimeException("사용자 정보가 없습니다.2") }
    }
}
