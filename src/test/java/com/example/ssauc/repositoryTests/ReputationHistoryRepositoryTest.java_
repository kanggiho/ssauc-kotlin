package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.mypage.entity.ReputationHistory;
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReputationHistoryRepositoryTest {

    @Autowired
    private ReputationHistoryRepository reputationHistoryRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveReputationHistory() {
        Users user = createTestUser("repUser", "repUser@example.com");
        Users savedUser = usersRepository.save(user);

        ReputationHistory history = ReputationHistory.builder()
                .user(savedUser)
                .changeType("Increase")
                .changeAmount(0.5)
                .newScore(5.0)
                .createdAt(LocalDateTime.now())
                .build();

        ReputationHistory savedHistory = reputationHistoryRepository.save(history);
        assertThat(savedHistory.getHistoryId()).isNotNull();
    }

    private Users createTestUser(String name, String email) {
        return Users.builder()
                .userName(name)
                .email(email)
                .password("password")
                .phone("010-0000-0000")
                .profileImage("http://example.com/img.png")
                .reputation(4.3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
    }
}
