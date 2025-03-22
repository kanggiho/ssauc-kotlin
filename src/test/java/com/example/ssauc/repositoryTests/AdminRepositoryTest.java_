package com.example.ssauc.repositoryTests;

import com.example.ssauc.admin.entity.Admin;
import com.example.ssauc.admin.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void testSaveAndFindAdmin() {
        // Given
        Admin admin = Admin.builder()
                .adminName("superadmin")
                .email("admin@example.com")
                .password("securepassword")
                .build();

        // When
        Admin savedAdmin = adminRepository.save(admin);
        Optional<Admin> foundAdmin = adminRepository.findByEmail("admin@example.com");

        // Then
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getAdminId()).isEqualTo(savedAdmin.getAdminId());
        assertThat(foundAdmin.get().getAdminName()).isEqualTo("superadmin");
    }
}
