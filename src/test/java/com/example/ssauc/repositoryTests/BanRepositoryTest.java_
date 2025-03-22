package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.chat.entity.Ban;
import com.example.ssauc.user.chat.repository.BanRepository;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // MySQL 등 외부 DB 사용
class BanRepositoryTest {

    @Autowired
    private BanRepository banRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    @DisplayName("Ban 엔티티 저장 및 조회 테스트")
    void testSaveAndFindBan() {
        // Given
        // 1) 차단을 하는 유저(user)
        Users user = Users.builder()
                .userName("UserA")
                .email("userA@example.com")
                .password("passA")
                .createdAt(LocalDateTime.now())
                .build();

        // 2) 차단당하는 유저(blockedUser)
        Users blockedUser = Users.builder()
                .userName("UserB")
                .email("userB@example.com")
                .password("passB")
                .createdAt(LocalDateTime.now())
                .build();

        // DB에 먼저 저장
        Users savedUser = usersRepository.save(user);
        Users savedBlockedUser = usersRepository.save(blockedUser);

        // Ban 엔티티 생성
        Ban ban = Ban.builder()
                .user(savedUser)
                .blockedUser(savedBlockedUser)
                .blockedAt(LocalDateTime.now())
                .build();

        // When
        Ban savedBan = banRepository.save(ban);

        // Then
        assertThat(savedBan.getBanId()).isNotNull();
        assertThat(savedBan.getUser().getEmail()).isEqualTo("userA@example.com");
        assertThat(savedBan.getBlockedUser().getEmail()).isEqualTo("userB@example.com");

        // When 2: ban 조회
        Ban foundBan = banRepository.findById(savedBan.getBanId())
                .orElseThrow(() -> new IllegalArgumentException("Ban not found"));

        // Then 2
        assertThat(foundBan.getUser().getUserName()).isEqualTo("UserA");
        assertThat(foundBan.getBlockedUser().getUserName()).isEqualTo("UserB");
    }
}
