package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.main.entity.Notification;
import com.example.ssauc.user.main.repository.NotificationRepository;
import com.example.ssauc.user.login.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveAndFindNotification() {
        // 유저 생성 및 저장
        Users user = Users.builder()
                .userName("user989")
                .email("user2423@example.com")
                .password("pass")
                .createdAt(LocalDateTime.now())
                .build();
        user = usersRepository.save(user);

        // Notification 엔티티 생성 및 저장
        Notification notification = Notification.builder()
                .user(user)
                .type("SYSTEM")
                .message("새로운 알림입니다.")
                .createdAt(LocalDateTime.now())
                .readStatus(0)
                .build();

        Notification saved = notificationRepository.save(notification);

        // 저장된 Notification 조회 및 검증
        Notification found = notificationRepository.findById(saved.getNotificationId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo("SYSTEM");
        assertThat(found.getMessage()).isEqualTo("새로운 알림입니다.");
    }
}
