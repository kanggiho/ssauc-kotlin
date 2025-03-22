package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.cash.entity.Charge;
import com.example.ssauc.user.cash.repository.ChargeRepository;
import com.example.ssauc.user.login.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChargeRepositoryTest {

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveCharge() {
        Users user = createTestUser("chargeUser", "chargeUser@example.com");
        Users savedUser = usersRepository.save(user);

        Charge charge = Charge.builder()
                .user(savedUser)
                .impUid("imp_test")
                .chargeType("Credit")
                .amount(5000L)
                .status("Completed")
                .details("Charge details")
                .receiptUrl("http://example.com/receipt")
                .createdAt(LocalDateTime.now())
                .build();

        Charge savedCharge = chargeRepository.save(charge);
        assertThat(savedCharge.getChargeId()).isNotNull();
    }

    private Users createTestUser(String name, String email) {
        return Users.builder()
                .userName(name)
                .email(email)
                .password("password")
                .phone("010-0000-0000")
                .profileImage("http://example.com/img.png")
                .reputation(4.2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
    }
}
