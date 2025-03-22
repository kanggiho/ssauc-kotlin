package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.cash.entity.Withdraw;
import com.example.ssauc.user.cash.repository.WithdrawRepository;
import com.example.ssauc.user.login.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WithdrawRepositoryTest {

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveWithdraw() {
        Users user = createTestUser("withdrawUser", "withdrawUser@example.com");
        Users savedUser = usersRepository.save(user);

        Withdraw withdraw = Withdraw.builder()
                .user(savedUser)
                .amount(1000L)
                .commission(50L)
                .bank("Test Bank")
                .account("1234567890")
                .requestedAt(LocalDateTime.now())
                .withdrawAt(LocalDateTime.now())
                .build();

        Withdraw savedWithdraw = withdrawRepository.save(withdraw);
        assertThat(savedWithdraw.getWithdrawId()).isNotNull();
    }

    private Users createTestUser(String name, String email) {
        return Users.builder()
                .userName(name)
                .email(email)
                .password("password")
                .phone("010-0000-0000")
                .profileImage("http://example.com/img.png")
                .reputation(4.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
    }
}
