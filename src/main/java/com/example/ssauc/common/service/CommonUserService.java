package com.example.ssauc.common.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonUserService {
    private final UsersRepository usersRepository;

    public Users getCurrentUser(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다.2"));
    }
}
